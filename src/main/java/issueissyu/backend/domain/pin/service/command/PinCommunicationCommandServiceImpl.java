package issueissyu.backend.domain.pin.service.command;

import issueissyu.backend.domain.location.dto.res.CoordinateLocationResolveResDTO;
import issueissyu.backend.domain.location.dto.res.UserLocationResDTO;
import issueissyu.backend.domain.location.entity.Location;
import issueissyu.backend.domain.location.entity.PinLocation;
import issueissyu.backend.domain.location.repository.LocationRepository;
import issueissyu.backend.domain.location.repository.PinLocationRepository;
import issueissyu.backend.domain.location.service.LocationService;
import issueissyu.backend.domain.pin.dto.req.CommunicationPinEditReqDTO;
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
import issueissyu.backend.domain.pin.repository.PinRepository;
import issueissyu.backend.domain.pin.util.PinS3UrlSupport;
import issueissyu.backend.domain.user.entity.User;
import issueissyu.backend.domain.user.repository.UserRepository;
import issueissyu.backend.global.api.code.GeneralErrorCode;
import issueissyu.backend.global.config.AmazonConfig;
import issueissyu.backend.global.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.postgresql.geometric.PGpoint;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class PinCommunicationCommandServiceImpl implements PinCommunicationCommandService {

    private final UserRepository userRepository;
    private final PinRepository pinRepository;
    private final CommunicationPinRepository communicationPinRepository;
    private final PinLocationRepository pinLocationRepository;
    private final LocationRepository locationRepository;
    private final LocationService locationService;
    private final AmazonConfig amazonConfig;

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

            return toImportRes(pin, location.getRegion(), detailAddress);
        } catch (PinException e) {
            throw e;
        } catch (Exception e) {
            throw PinException.of(PinErrorCode.PIN_IMPORT_COMMUNICATION_400_2);
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
            validateMainFlags(req.pinImageUrls(), PinErrorCode.PIN_EDIT_COMMUNICATION_400_1);
            Location newLocation =
                    locationRepository
                            .findByRegion(trimCompact(req.region()))
                            .orElseThrow(() -> PinException.of(PinErrorCode.PIN_EDIT_COMMUNICATION_400_4));

            pin.updatePinDetails(req.pinTitle(), req.pinContent());

            PinLocation pl =
                    pinLocationRepository
                            .findByPin_PinId(pinId)
                            .orElseThrow(() -> PinException.of(PinErrorCode.PIN_EDIT_COMMUNICATION_400_4));
            pl.changeAdministrativeLocation(newLocation);

            syncPinImages(pin, req.pinImageUrls());

            pinRepository.save(pin);

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
                    imgs, pin.getCreatedAt(), pin.getUpdatedAt());
        } catch (PinException e) {
            throw e;
        } catch (Exception e) {
            throw PinException.of(PinErrorCode.PIN_EDIT_COMMUNICATION_400_4);
        }
    }

    private void syncPinImages(Pin pin, List<PinImageItemReqDTO> items) {
        Map<String, PinImage> byUrl =
                pin.getPinImages().stream()
                        .collect(Collectors.toMap(PinImage::getPinS3Url, Function.identity(), (a, b) -> a));

        Set<String> keepUrls = new HashSet<>();

        for (PinImageItemReqDTO item : items) {
            keepUrls.add(item.pinImageUrl());
            PinImage existing = byUrl.get(item.pinImageUrl());
            if (existing != null) {
                existing.setMainImage(item.isMain());
            } else {
                String key = PinS3UrlSupport.extractKey(item.pinImageUrl(), amazonConfig);
                PinImage created =
                        PinImage.builder()
                                .pinS3Key(key)
                                .pinS3Url(item.pinImageUrl())
                                .mainImage(item.isMain())
                                .build();
                pin.addPinImage(created);
            }
        }

        List<PinImage> toRemove =
                pin.getPinImages().stream()
                        .filter(pi -> !keepUrls.contains(pi.getPinS3Url()))
                        .toList();
        toRemove.forEach(pin.getPinImages()::remove);
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

    private static void validateMainFlags(List<PinImageItemReqDTO> items, PinErrorCode violation) {
        if (items == null || items.isEmpty()) {
            return;
        }
        long mains = items.stream().filter(PinImageItemReqDTO::isMain).count();
        if (mains != 1) {
            throw PinException.of(violation);
        }
    }

    private static PGpoint toPgPoint(double lat, double lng) {
        return new PGpoint(lng, lat);
    }

    private static Point createPoint(double lng, double lat) {
        return GEOMETRY_FACTORY.createPoint(new Coordinate(lng, lat));
    }

    private static String trimCompact(String s) {
        return s.trim().replaceAll("\\s+", " ");
    }
}
