package issueissyu.backend.domain.pin.service.command;

import issueissyu.backend.domain.community.entity.Community;
import issueissyu.backend.domain.community.enums.CommunityType;
import issueissyu.backend.domain.community.repository.CommunityRepository;
import issueissyu.backend.domain.location.dto.res.CoordinateLocationResolveResDTO;
import issueissyu.backend.domain.location.dto.res.UserLocationResDTO;
import issueissyu.backend.domain.location.entity.Location;
import issueissyu.backend.domain.location.entity.PinLocation;
import issueissyu.backend.domain.location.repository.LocationRepository;
import issueissyu.backend.domain.location.repository.PinLocationRepository;
import issueissyu.backend.domain.location.service.LocationService;
import issueissyu.backend.domain.pin.dto.req.CommunicationPinImportMultipartImageReqDTO;
import issueissyu.backend.domain.pin.dto.req.PinImageItemReqDTO;
import issueissyu.backend.domain.pin.dto.req.StorePinImportMultipartReqDTO;
import issueissyu.backend.domain.pin.dto.req.StorePinImportReqDTO;
import issueissyu.backend.domain.pin.dto.res.PinImageWithIdResDTO;
import issueissyu.backend.domain.pin.dto.res.StorePinImportResDTO;
import issueissyu.backend.domain.pin.entity.EventPin;
import issueissyu.backend.domain.pin.entity.Pin;
import issueissyu.backend.domain.pin.entity.PinImage;
import issueissyu.backend.domain.pin.entity.StoreImage;
import issueissyu.backend.domain.pin.enums.PinType;
import issueissyu.backend.domain.pin.enums.ToneType;
import issueissyu.backend.domain.pin.exception.PinException;
import issueissyu.backend.domain.pin.exception.code.PinErrorCode;
import issueissyu.backend.domain.pin.repository.EventPinRepository;
import issueissyu.backend.domain.pin.repository.PinRepository;
import issueissyu.backend.domain.pin.repository.StoreImageRepository;
import issueissyu.backend.domain.pin.util.PinS3UrlSupport;
import issueissyu.backend.domain.user.entity.User;
import issueissyu.backend.domain.user.enums.UserRole;
import issueissyu.backend.domain.user.repository.UserRepository;
import issueissyu.backend.global.api.code.GeneralErrorCode;
import issueissyu.backend.global.config.AmazonConfig;
import issueissyu.backend.global.exception.GeneralException;
import issueissyu.backend.utils.S3.S3Utils;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.postgresql.geometric.PGpoint;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class PinStoreCommandServiceImpl implements PinStoreCommandService {

    private static final GeometryFactory GEOMETRY_FACTORY =
            new GeometryFactory(new PrecisionModel(), 4326);

    private static final DateTimeFormatter EVENT_TIME =
            new DateTimeFormatterBuilder()
                    .appendPattern("yyyy-MM-dd HH:mm:ss")
                    .appendFraction(ChronoField.NANO_OF_SECOND, 0, 9, true)
                    .toFormatter();

    private final UserRepository userRepository;
    private final PinRepository pinRepository;
    private final PinLocationRepository pinLocationRepository;
    private final LocationRepository locationRepository;
    private final LocationService locationService;
    private final EventPinRepository eventPinRepository;
    private final StoreImageRepository storeImageRepository;
    private final CommunityRepository communityRepository;
    private final AmazonConfig amazonConfig;
    private final PinImageUploadCommandService pinImageUploadCommandService;
    private final S3Utils s3Utils;

    @Override
    public StorePinImportResDTO importStore(String uid, StorePinImportReqDTO req) {
        User user =
                userRepository
                        .findById(uid)
                        .orElseThrow(() -> GeneralException.of(GeneralErrorCode.USER_NOT_FOUND));
        assertAdmin(user);

        try {
            PGpoint pgp = toPgPoint(req.lat(), req.lng());
            CoordinateLocationResolveResDTO resolved = locationService.resolveAddressAndLocationId(pgp);
            Location location =
                    locationRepository
                            .findById(resolved.locationId())
                            .orElseThrow(() -> PinException.of(PinErrorCode.PIN_IMPORT_STORE_400_2));

            UserLocationResDTO roadAddr = locationService.getRoadAddress(pgp);
            String detailAddress =
                    roadAddr.address() == null ? "" : roadAddr.address().trim();
            if (detailAddress.isBlank()) {
                throw PinException.of(PinErrorCode.PIN_IMPORT_STORE_400_2);
            }

            validateMainFlags(req.pinImageUrls(), PinErrorCode.PIN_IMPORT_STORE_400_1);

            Pin pin =
                    Pin.builder()
                            .pinType(PinType.STORE)
                            .pinTitle(req.pinTitle())
                            .pinContent(req.pinContent())
                            .toneType(ToneType.NONE)
                            .likeCount(0)
                            .user(user)
                            .build();

            for (PinImageItemReqDTO item : req.pinImageUrls()) {
                String key =
                        PinS3UrlSupport.extractKey(
                                item.pinImageUrl(), amazonConfig, PinErrorCode.PIN_IMPORT_STORE_400_2);
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

            EventPin eventPin =
                    EventPin.builder()
                            .pin(pin)
                            .eventStartTime(req.eventStartTime())
                            .eventEndTime(req.eventEndTime())
                            .discount(req.discount())
                            .build();
            eventPinRepository.save(eventPin);

            String storeProfileImageKey =
                    PinS3UrlSupport.extractKey(
                            req.storeProfileImageUrl(),
                            amazonConfig,
                            PinErrorCode.PIN_IMPORT_STORE_400_1);
            storeImageRepository.save(
                    StoreImage.builder()
                            .eventPin(eventPin)
                            .imageS3Key(storeProfileImageKey)
                            .imageS3Url(req.storeProfileImageUrl())
                            .build());

            communityRepository.save(
                    Community.builder().pin(pin).communityType(CommunityType.STORE).build());

            return toImportRes(pin, location.getRegion(), detailAddress, req.storeProfileImageUrl());
        } catch (PinException e) {
            throw e;
        } catch (Exception e) {
            throw PinException.of(PinErrorCode.PIN_IMPORT_STORE_400_2);
        }
    }

    @Override
    public StorePinImportResDTO importStoreV1(
            String uid,
            StorePinImportMultipartReqDTO req,
            List<MultipartFile> photos,
            MultipartFile storeProfileImage) {
        if (storeProfileImage == null || storeProfileImage.isEmpty()) {
            throw PinException.of(PinErrorCode.PIN_IMPORT_STORE_400_1);
        }

        List<MultipartFile> photoParts = photos == null ? List.of() : photos;
        validateMultipartImportRequest(req, photoParts);

        List<String> uploadedUrls = new ArrayList<>();
        try {
            String storeProfileImageUrl =
                    pinImageUploadCommandService.uploadPinImages(List.of(storeProfileImage)).get(0);
            uploadedUrls.add(storeProfileImageUrl);

            List<PinImageItemReqDTO> pinImageUrls = List.of();
            if (!photoParts.isEmpty()) {
                List<String> photoUrls = pinImageUploadCommandService.uploadPinImages(photoParts);
                uploadedUrls.addAll(photoUrls);
                pinImageUrls = buildPinImageItemRequests(photoUrls, req.pinImages());
            }

            StorePinImportReqDTO mappedReq =
                    mapStoreImportRequest(req, pinImageUrls, storeProfileImageUrl);
            return importStore(uid, mappedReq);
        } catch (RuntimeException e) {
            rollbackUploadedUrls(uploadedUrls);
            throw e;
        }
    }

    private StorePinImportReqDTO mapStoreImportRequest(
            StorePinImportMultipartReqDTO req,
            List<PinImageItemReqDTO> pinImageUrls,
            String storeProfileImageUrl) {
        return new StorePinImportReqDTO(
                req.lat(),
                req.lng(),
                pinImageUrls,
                req.pinTitle(),
                req.pinContent(),
                parseEventTime(req.eventStartTime()),
                parseEventTime(req.eventEndTime()),
                req.discount(),
                storeProfileImageUrl);
    }

    private LocalDateTime parseEventTime(String value) {
        try {
            return LocalDateTime.parse(value.trim(), EVENT_TIME);
        } catch (Exception e) {
            throw PinException.of(PinErrorCode.PIN_IMPORT_STORE_400_1);
        }
    }

    private static void assertAdmin(User user) {
        if (user.getRole() != UserRole.ADMIN) {
            throw PinException.of(PinErrorCode.PIN_IMPORT_STORE_400_2);
        }
    }

    private StorePinImportResDTO toImportRes(
            Pin pin, String region, String pinDetailAddress, String storeProfileImage) {
        List<PinImageWithIdResDTO> imgs =
                pin.getPinImages().stream()
                        .sorted(Comparator.comparing(PinImage::getPinImageId))
                        .map(
                                pi ->
                                        new PinImageWithIdResDTO(
                                                pi.getPinImageId(),
                                                pi.getPinS3Url(),
                                                pi.isMainImage()))
                        .toList();

        return new StorePinImportResDTO(
                pin.getPinId(),
                pin.getPinType().name(),
                region,
                pinDetailAddress,
                imgs,
                storeProfileImage,
                pin.getToneType().name(),
                pin.getCreatedAt(),
                pin.getUpdatedAt());
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

    private static void validateMultipartImportRequest(
            StorePinImportMultipartReqDTO req, List<MultipartFile> photos) {
        if (req == null) {
            throw PinException.of(PinErrorCode.PIN_IMPORT_STORE_400_1);
        }
        List<CommunicationPinImportMultipartImageReqDTO> pinMeta = req.pinImages();
        if (pinMeta == null) {
            pinMeta = List.of();
        }
        if (photos.isEmpty()) {
            if (!pinMeta.isEmpty()) {
                throw PinException.of(PinErrorCode.PIN_IMPORT_STORE_400_1);
            }
            return;
        }
        if (pinMeta.size() != photos.size()) {
            throw PinException.of(PinErrorCode.PIN_IMPORT_STORE_400_1);
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
