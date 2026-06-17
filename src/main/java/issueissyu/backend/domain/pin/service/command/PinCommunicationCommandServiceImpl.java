package issueissyu.backend.domain.pin.service.command;

import issueissyu.backend.domain.location.dto.res.CoordinateLocationResolveResDTO;
import issueissyu.backend.domain.map.cache.PinGeoRedisService;
import issueissyu.backend.domain.location.dto.res.UserLocationResDTO;
import issueissyu.backend.domain.location.entity.Location;
import issueissyu.backend.domain.location.entity.PinLocation;
import issueissyu.backend.domain.location.repository.LocationRepository;
import issueissyu.backend.domain.location.repository.PinLocationRepository;
import issueissyu.backend.domain.location.service.LocationService;
import issueissyu.backend.domain.pin.dto.req.CommunicationPinEditReqDTO;
import issueissyu.backend.domain.pin.dto.req.CommunicationPinEditMultipartReqDTO;
import issueissyu.backend.domain.pin.dto.req.CommunicationPinImportMultipartImageReqDTO;
import issueissyu.backend.domain.pin.dto.req.CommunicationPinImportMultipartReqDTO;
import issueissyu.backend.domain.pin.dto.req.CommunicationPinImportReqDTO;
import issueissyu.backend.domain.pin.dto.req.PinImageItemReqDTO;
import issueissyu.backend.domain.pin.dto.res.CommunicationPinEditResDTO;
import issueissyu.backend.domain.pin.dto.res.CommunicationPinImportResDTO;
import issueissyu.backend.domain.pin.dto.res.PinImageWithIdResDTO;
import issueissyu.backend.domain.pin.entity.CommunicationPin;
import issueissyu.backend.domain.pin.entity.Pin;
import issueissyu.backend.domain.pin.entity.PinImage;
import issueissyu.backend.domain.pin.enums.PinType;
import issueissyu.backend.domain.pin.enums.ToneType;
import issueissyu.backend.domain.pin.exception.PinException;
import issueissyu.backend.domain.pin.exception.code.PinErrorCode;
import issueissyu.backend.domain.pin.repository.CommunicationPinRepository;
import issueissyu.backend.domain.pin.repository.PinImageRepository;
import issueissyu.backend.domain.pin.repository.PinRepository;
import issueissyu.backend.domain.pin.util.PinS3UrlSupport;
import issueissyu.backend.domain.user.entity.User;
import issueissyu.backend.domain.user.repository.UserRepository;
import issueissyu.backend.global.api.code.GeneralErrorCode;
import issueissyu.backend.global.config.AmazonConfig;
import issueissyu.backend.global.exception.GeneralException;
import issueissyu.backend.utils.S3.S3Utils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.postgresql.geometric.PGpoint;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class PinCommunicationCommandServiceImpl implements PinCommunicationCommandService {

    private final UserRepository userRepository;
    private final PinRepository pinRepository;
    private final CommunicationPinRepository communicationPinRepository;
    private final PinLocationRepository pinLocationRepository;
    private final PinImageRepository pinImageRepository;
    private final LocationRepository locationRepository;
    private final LocationService locationService;
    private final AmazonConfig amazonConfig;
    private final PinImageUploadCommandService pinImageUploadCommandService;
    private final S3Utils s3Utils;
    private final PinGeoRedisService pinGeoRedisService;

    private static final GeometryFactory GEOMETRY_FACTORY =
            new GeometryFactory(new PrecisionModel(), 4326);

    @Override
    public CommunicationPinImportResDTO importCommunication(String uid, CommunicationPinImportReqDTO req) {
        User user = userRepository.findById(uid).orElseThrow(() -> GeneralException.of(GeneralErrorCode.USER_NOT_FOUND));

        try {
            PGpoint pgp = toPgPoint(req.lat(), req.lng());
            CoordinateLocationResolveResDTO resolved = locationService.resolveAddressAndLocationId(pgp);
            Location location =
                    locationRepository
                            .findById(resolved.locationId())
                            .orElseThrow(() -> PinException.of(PinErrorCode.PIN_IMPORT_COMMUNICATION_400_2));

            // GET /api/location/address 와 동일: 도로명 주소 문자열을 pin_location.detail_address 로 저장합니다.
            UserLocationResDTO roadAddr = locationService.getRoadAddress(pgp);
            String detailAddress =
                    roadAddr.address() == null ? "" : roadAddr.address().trim();
            if (detailAddress.isBlank()) {
                throw PinException.of(PinErrorCode.PIN_IMPORT_COMMUNICATION_400_2);
            }

            validateMainFlags(req.pinImageUrls(), PinErrorCode.PIN_IMPORT_COMMUNICATION_400_1);

            Pin pin =
                    Pin.builder()
                            .pinType(PinType.COMMUNICATION)
                            .pinTitle(req.pinTitle())
                            .pinContent(req.pinContent())
                            .toneType(ToneType.NONE)
                            .likeCount(0)
                            .user(user)
                            .build();

            for (PinImageItemReqDTO item : req.pinImageUrls()) {
                String key = PinS3UrlSupport.extractKey(item.pinImageUrl(), amazonConfig);
                pin.addPinImage(
                        PinImage.builder()
                                .pinS3Key(key)
                                .pinS3Url(item.pinImageUrl())
                                .mainImage(item.isMain())
                                .build());
            }

            pinRepository.saveAndFlush(pin);

            Point pinPoint = createPoint(req.lng(), req.lat());
            pinLocationRepository.save(
                    PinLocation.builder()
                            .pin(pin)
                            .location(location)
                            .pinPoint(pinPoint)
                            .detailAddress(detailAddress)
                            .build());

            communicationPinRepository.save(CommunicationPin.builder().pin(pin).build());

            // Redis GEO 캐시에 적재 (실패해도 DB 저장에 영향 없음)
            pinGeoRedisService.addPin(
                    pin.getPinId(), PinType.COMMUNICATION.name(),
                    req.lat(), req.lng(),
                    detailAddress, location.getRegion(), null);

            return toImportRes(pin, location.getRegion(), detailAddress);
        } catch (PinException e) {
            throw e;
        } catch (Exception e) {
            throw PinException.of(PinErrorCode.PIN_IMPORT_COMMUNICATION_400_2);
        }
    }

    @Override
    public CommunicationPinImportResDTO importCommunicationV1(
            String uid, CommunicationPinImportMultipartReqDTO req, List<MultipartFile> photos) {
        List<MultipartFile> photoParts = photos == null ? List.of() : photos;

        validateMultipartImportRequest(req, photoParts);

        if (photoParts.isEmpty()) {
            CommunicationPinImportReqDTO mappedReq =
                    new CommunicationPinImportReqDTO(
                            req.lat(), req.lng(), List.of(), req.pinTitle(), req.pinContent());
            return importCommunication(uid, mappedReq);
        }

        List<String> uploadedUrls = pinImageUploadCommandService.uploadPinImages(photoParts);
        try {
            List<PinImageItemReqDTO> pinImageUrls =
                    buildPinImageItemRequests(uploadedUrls, req.pinImages());
            CommunicationPinImportReqDTO mappedReq =
                    new CommunicationPinImportReqDTO(
                            req.lat(),
                            req.lng(),
                            pinImageUrls,
                            req.pinTitle(),
                            req.pinContent());
            return importCommunication(uid, mappedReq);
        } catch (RuntimeException e) {
            rollbackUploadedUrls(uploadedUrls);
            throw e;
        }
    }

    @Override
    public CommunicationPinEditResDTO editCommunicationV1(
            String uid, Long pinId, CommunicationPinEditMultipartReqDTO req, List<MultipartFile> photos) {
        List<MultipartFile> photoParts = photos == null ? List.of() : photos;
        List<PinImageItemReqDTO> existingItems = normalizePinImageUrls(req.pinImageUrls());
        List<CommunicationPinImportMultipartImageReqDTO> newImageMeta =
                req.pinImages() == null ? List.of() : req.pinImages();

        validateMultipartEditRequest(existingItems, newImageMeta, photoParts);

        if (photoParts.isEmpty()) {
            return editCommunication(
                    uid,
                    pinId,
                    new CommunicationPinEditReqDTO(existingItems, req.pinTitle(), req.pinContent()));
        }

        List<String> uploadedUrls = pinImageUploadCommandService.uploadPinImages(photoParts);
        try {
            List<PinImageItemReqDTO> newItems = buildPinImageItemRequests(uploadedUrls, newImageMeta);
            List<PinImageItemReqDTO> merged = mergePinImageItems(existingItems, newItems);
            validateCombinedEditPinImages(merged);
            return editCommunication(
                    uid,
                    pinId,
                    new CommunicationPinEditReqDTO(merged, req.pinTitle(), req.pinContent()));
        } catch (RuntimeException e) {
            rollbackUploadedUrls(uploadedUrls);
            throw e;
        }
    }

    @Override
    public CommunicationPinEditResDTO editCommunication(String uid, Long pinId, CommunicationPinEditReqDTO req) {
        Pin pin =
                pinRepository
                        .fetchDetailWithAuthor(pinId)
                        .orElseThrow(() -> PinException.of(PinErrorCode.PIN_EDIT_COMMUNICATION_400_2));

        if (pin.getPinType() != PinType.COMMUNICATION) {
            throw PinException.of(PinErrorCode.PIN_EDIT_COMMUNICATION_400_4);
        }
        if (!pin.getUser().getUid().equals(uid)) {
            throw PinException.of(PinErrorCode.PIN_EDIT_COMMUNICATION_400_3);
        }

        try {
            boolean pinImageChanged = false;
            if (req.pinImageUrls() != null) {
                assertPinImageUrlsBelongToPin(pinId, req.pinImageUrls());
                validateCommunicationEditPinImages(req.pinImageUrls());
                pinImageChanged = syncPinImages(pin, req.pinImageUrls());
            }

            boolean pinDetailsChanged =
                    !Objects.equals(pin.getPinTitle(), req.pinTitle())
                            || !Objects.equals(pin.getPinContent(), req.pinContent());
            pin.updatePinDetails(req.pinTitle(), req.pinContent());

            LocalDateTime responseUpdatedAt = pin.getUpdatedAt();
            if (pinDetailsChanged || pinImageChanged) {
                responseUpdatedAt = LocalDateTime.now();
                // pin_image 변경만 있는 경우에는 pin row가 갱신되지 않으므로 updated_at을 명시적으로 갱신한다.
                if (!pinDetailsChanged) {
                    pinRepository.bumpUpdatedAt(pinId, responseUpdatedAt);
                }
            }

            PinLocation pinLocation =
                    pinLocationRepository
                            .findByPin_PinId(pinId)
                            .orElseThrow(() -> PinException.of(PinErrorCode.PIN_EDIT_COMMUNICATION_400_5));

            return toEditRes(
                    pin,
                    pinLocation.getLocation().getRegion(),
                    pinLocation.getDetailAddress(),
                    responseUpdatedAt);
        } catch (PinException e) {
            throw e;
        } catch (Exception e) {
            throw PinException.of(PinErrorCode.PIN_EDIT_COMMUNICATION_400_5);
        }
    }

    private void assertPinImageUrlsBelongToPin(Long pinId, List<PinImageItemReqDTO> items) {
        for (PinImageItemReqDTO item : items) {
            pinImageRepository.findAllWithPinByPinS3UrlOrderByPinImageIdAsc(item.pinImageUrl()).stream()
                    .findFirst()
                    .ifPresent(pi -> {
                        if (!Objects.equals(pi.getPin().getPinId(), pinId)) {
                            throw PinException.of(PinErrorCode.PIN_EDIT_COMMUNICATION_400_1);
                        }
                    });
        }
    }

    private static List<PinImageItemReqDTO> normalizePinImageUrls(List<PinImageItemReqDTO> pinImageUrls) {
        return pinImageUrls == null ? null : List.copyOf(pinImageUrls);
    }

    private static List<PinImageItemReqDTO> mergePinImageItems(
            List<PinImageItemReqDTO> existingItems, List<PinImageItemReqDTO> newItems) {
        if (existingItems == null || existingItems.isEmpty()) {
            return List.copyOf(newItems);
        }
        if (newItems.isEmpty()) {
            return List.copyOf(existingItems);
        }
        List<PinImageItemReqDTO> merged = new ArrayList<>(existingItems.size() + newItems.size());
        merged.addAll(existingItems);
        merged.addAll(newItems);
        return merged;
    }

    private static void validateCombinedEditPinImages(List<PinImageItemReqDTO> merged) {
        if (merged.size() > 5) {
            throw PinException.of(PinErrorCode.PIN_IMAGE_400_3);
        }
        validateMainFlags(merged, PinErrorCode.PIN_EDIT_COMMUNICATION_400_1);
    }

    private boolean syncPinImages(Pin pin, List<PinImageItemReqDTO> items) {
        boolean changed = false;
        Set<String> keepUrls = items.stream().map(PinImageItemReqDTO::pinImageUrl).collect(Collectors.toSet());

        boolean removedAny = pin.getPinImages().removeIf(pi -> !keepUrls.contains(pi.getPinS3Url()));
        if (removedAny) {
            changed = true;
        }

        for (PinImageItemReqDTO item : items) {
            PinImage row =
                    pin.getPinImages().stream()
                            .filter(pi -> Objects.equals(pi.getPinS3Url(), item.pinImageUrl()))
                            .findFirst()
                            .orElseGet(
                                    () ->
                                            resolveExistingPinImageByUrl(pin.getPinId(), item.pinImageUrl())
                                                    .orElse(null));

            if (row != null) {
                if (attachIfAbsent(pin, row)) {
                    changed = true;
                }
                if (row.isMainImage() != item.isMain()) {
                    row.setMainImage(item.isMain());
                    changed = true;
                }
            } else {
                String key = PinS3UrlSupport.extractKey(item.pinImageUrl(), amazonConfig);
                PinImage created =
                        PinImage.builder()
                                .pinS3Key(key)
                                .pinS3Url(item.pinImageUrl())
                                .mainImage(item.isMain())
                                .build();
                pin.addPinImage(created);
                changed = true;
            }
        }
        return changed;
    }

    // pin_image 테이블에서 동일 URL이 있으면, 현재 수정 중인 핀에 속한 행이면 해당 pin_image(row)를 그대로 사용합니다.
    private Optional<PinImage> resolveExistingPinImageByUrl(Long pinId, String pinS3Url) {
        return pinImageRepository.findAllWithPinByPinS3UrlOrderByPinImageIdAsc(pinS3Url).stream()
                .filter(pi -> Objects.equals(pi.getPin().getPinId(), pinId))
                .findFirst();
    }

    private static boolean attachIfAbsent(Pin pin, PinImage candidate) {
        boolean alreadyAttached =
                pin.getPinImages().stream()
                        .anyMatch(pi -> pinImageRefsEqual(pi, candidate));
        if (alreadyAttached) {
            return false;
        }
        pin.addPinImage(candidate);
        return true;
    }

    private static boolean pinImageRefsEqual(PinImage a, PinImage b) {
        Long idA = a.getPinImageId();
        Long idB = b.getPinImageId();
        if (idA != null && idB != null) {
            return Objects.equals(idA, idB);
        }
        return a == b;
    }

    private CommunicationPinImportResDTO toImportRes(Pin pin, String region, String pinDetailAddress) {
        List<PinImageWithIdResDTO> imgs =
                pin.getPinImages().stream()
                        .sorted(java.util.Comparator.comparing(PinImage::getPinImageId))
                        .map(
                                pi ->
                                        new PinImageWithIdResDTO(
                                                pi.getPinImageId(),
                                                pi.getPinS3Url(),
                                                pi.isMainImage()))
                        .toList();

        return new CommunicationPinImportResDTO(
                pin.getPinId(),
                pin.getPinType().name(),
                region,
                pinDetailAddress,
                imgs,
                pin.getToneType().name(),
                pin.getCreatedAt(),
                pin.getUpdatedAt());
    }

    private CommunicationPinEditResDTO toEditRes(
            Pin pin, String region, String pinDetailAddress, LocalDateTime updatedAt) {
        List<PinImageWithIdResDTO> imgs =
                pin.getPinImages().stream()
                        .sorted(java.util.Comparator.comparing(PinImage::getPinImageId))
                        .map(
                                pi ->
                                        new PinImageWithIdResDTO(
                                                pi.getPinImageId(),
                                                pi.getPinS3Url(),
                                                pi.isMainImage()))
                        .toList();

        return new CommunicationPinEditResDTO(
                pin.getPinId(),
                pin.getPinType().name(),
                region,
                pinDetailAddress,
                imgs,
                pin.getToneType().name(),
                pin.getCreatedAt(),
                updatedAt);
    }

    private static void validateCommunicationEditPinImages(List<PinImageItemReqDTO> items) {
        if (items == null) {
            return;
        }
        validateMainFlags(items, PinErrorCode.PIN_EDIT_COMMUNICATION_400_1);
    }

    private static void validateMainFlags(List<PinImageItemReqDTO> items, PinErrorCode violation) {
        if (items == null || items.isEmpty()) {
            return;
        }
        long mains = items.stream().filter(PinImageItemReqDTO::isMain).count();
        if (mains != 1) {
            throw PinException.of(violation);
        }
    }

    private static void validateMultipartEditRequest(
            List<PinImageItemReqDTO> existingItems,
            List<CommunicationPinImportMultipartImageReqDTO> newImageMeta,
            List<MultipartFile> photos) {
        if (photos.isEmpty()) {
            if (!newImageMeta.isEmpty()) {
                throw PinException.of(PinErrorCode.PIN_EDIT_COMMUNICATION_400_1);
            }
            return;
        }
        if (newImageMeta.size() != photos.size()) {
            throw PinException.of(PinErrorCode.PIN_EDIT_COMMUNICATION_400_1);
        }
        int existingCount = existingItems == null ? 0 : existingItems.size();
        if (existingCount + photos.size() > 5) {
            throw PinException.of(PinErrorCode.PIN_IMAGE_400_3);
        }
    }

    private static void validateMultipartImportRequest(
            CommunicationPinImportMultipartReqDTO req, List<MultipartFile> photos) {
        if (req == null) {
            throw PinException.of(PinErrorCode.PIN_IMPORT_COMMUNICATION_400_1);
        }
        List<CommunicationPinImportMultipartImageReqDTO> pinMeta = req.pinImages();
        if (pinMeta == null) {
            pinMeta = List.of();
        }
        if (photos.isEmpty()) {
            if (!pinMeta.isEmpty()) {
                throw PinException.of(PinErrorCode.PIN_IMPORT_COMMUNICATION_400_1);
            }
            return;
        }
        if (pinMeta.size() != photos.size()) {
            throw PinException.of(PinErrorCode.PIN_IMPORT_COMMUNICATION_400_1);
        }
    }

    private static List<PinImageItemReqDTO> buildPinImageItemRequests(
            List<String> uploadedUrls, List<CommunicationPinImportMultipartImageReqDTO> imageReqs) {
        List<CommunicationPinImportMultipartImageReqDTO> safe = imageReqs == null ? List.of() : imageReqs;
        return java.util.stream.IntStream.range(0, uploadedUrls.size())
                .mapToObj(
                        i ->
                                new PinImageItemReqDTO(
                                        uploadedUrls.get(i), Boolean.TRUE.equals(safe.get(i).isMain())))
                .toList();
    }

    private void rollbackUploadedUrls(List<String> uploadedUrls) {
        for (String url : uploadedUrls) {
            try {
                String key = PinS3UrlSupport.extractKey(url, amazonConfig);
                s3Utils.deleteFile(key);
            } catch (Exception e) {
                log.warn("Failed to rollback (delete) S3 file for url={}: {}", url, e.getMessage());
            }
        }
    }

    private static PGpoint toPgPoint(double lat, double lng) {
        return new PGpoint(lng, lat);
    }

    private static Point createPoint(double lng, double lat) {
        return GEOMETRY_FACTORY.createPoint(new Coordinate(lng, lat));
    }
}
