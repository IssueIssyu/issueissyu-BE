package issueissyu.backend.domain.map.service.query;

import issueissyu.backend.domain.community.entity.Community;
import issueissyu.backend.domain.community.repository.CommunityRepository;
import issueissyu.backend.domain.issue.repository.IssuePinRepository;
import issueissyu.backend.domain.location.entity.PinLocation;
import issueissyu.backend.domain.location.repository.PinLocationRepository;
import issueissyu.backend.domain.map.dto.res.MapPinCardResDTO;
import issueissyu.backend.domain.map.exception.MapException;
import issueissyu.backend.domain.map.exception.code.MapErrorCode;
import issueissyu.backend.domain.pin.entity.EventPin;
import issueissyu.backend.domain.pin.entity.Pin;
import issueissyu.backend.domain.pin.entity.PinImage;
import issueissyu.backend.domain.pin.enums.PinType;
import issueissyu.backend.domain.pin.repository.EventPinRepository;
import issueissyu.backend.domain.pin.repository.PinLikeRepository;
import issueissyu.backend.domain.pin.repository.PinRepository;
import issueissyu.backend.domain.user.entity.User;
import issueissyu.backend.domain.user.service.query.UserProfileImageQueryService;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MapPinCardQueryServiceImpl implements MapPinCardQueryService {

    private final PinRepository pinRepository;
    private final PinLocationRepository pinLocationRepository;
    private final IssuePinRepository issuePinRepository;
    private final EventPinRepository eventPinRepository;
    private final PinLikeRepository pinLikeRepository;
    private final UserProfileImageQueryService userProfileImageQueryService;
    private final CommunityRepository communityRepository;

    private static final String DEFAULT_PROFILE_IMAGE = "default";

    @Override
    @Transactional(readOnly = false)
    public MapPinCardResDTO findPinCard(Long pinId, String currentUserUid) {
        Pin pin =
                pinRepository
                        .fetchDetailWithAuthor(pinId)
                        .orElseThrow(() -> MapException.of(MapErrorCode.MAP_CARD_404));

        pinRepository.incrementViewCountByPinId(pinId);

        PinType type = pin.getPinType();
        User author = pin.getUser();

        Optional<PinLocation> pinLocationOpt =
                pinLocationRepository.findFirstByPin_PinIdOrderByPinLocationIdAsc(pinId);

        String detailAddr = pinLocationOpt.map(PinLocation::getDetailAddress).orElse(null);
        Double latitude =
                pinLocationOpt
                        .map(PinLocation::getPinPoint)
                        .map(Point::getY)
                        .orElse(null);
        Double longitude =
                pinLocationOpt
                        .map(PinLocation::getPinPoint)
                        .map(Point::getX)
                        .orElse(null);

        Optional<String> mainImageUrlOpt = resolveMainImageUrl(pin);

        long likeCountLong = pin.getLikeCount();

        boolean isLike =
                currentUserUid != null
                        && pinLikeRepository.existsByPin_PinIdAndUser_Uid(pinId, currentUserUid);

        boolean isMine =
                currentUserUid != null && Objects.equals(author.getUid(), currentUserUid);

        Optional<String> issueStateOpt =
                type == PinType.ISSUE
                        ? issuePinRepository
                                .findByPin_PinId(pinId)
                                .map(ip -> ip.getIssuePinState().name())
                        : Optional.empty();

        Optional<String> profileImgOpt =
                (type == PinType.ISSUE || type == PinType.COMMUNICATION)
                        ? userProfileImageQueryService.findUrlByUserUid(author.getUid())
                        : Optional.empty();

        Long communityId =
                communityRepository
                        .findByPin_PinId(pinId)
                        .map(Community::getCommunityId)
                        .orElse(null);

        MapPinCardResDTO.MapPinCardResDTOBuilder b =
                MapPinCardResDTO.builder()
                        .pinId(pin.getPinId())
                        .pinType(type.name())
                        .pinTitle(pin.getPinTitle())
                        .pinContent(pin.getPinContent())
                        .issuePinState(type == PinType.ISSUE ? issueStateOpt.orElse(null) : null)
                        .pinDetailAddress(detailAddr)
                        .likeCount(likeCountLong)
                        .likedByMe(isLike)
                        .mine(isMine)
                        .communityId(communityId)
                        .pinImageUrl(mainImageUrlOpt.orElse(null))
                        .discount(null)
                        .storeImageUrl(null)
                        .latitude(latitude)
                        .longitude(longitude);

        switch (type) {
            case ISSUE, COMMUNICATION -> b.pinUserId(author.getUid())
                    .pinUserProfile(profileImgOpt.orElse(DEFAULT_PROFILE_IMAGE))
                    .pinUserNickname(author.getNickname());
            case STORE -> {
                b.pinUserId(author.getUid())
                        .pinUserProfile(null)
                        .pinUserNickname(null);

                Optional<EventPin> ep = eventPinRepository.findWithStoreImageByPinPinId(pinId);
                b.discount(ep.map(EventPin::getDiscount).orElse(null))
                        .storeImageUrl(
                                ep.map(EventPin::getStoreImage)
                                        .map(si -> si.getImageS3Url())
                                        .orElse(null));
            }
            case FESTIVAL -> b.pinUserId(null)
                    .pinUserProfile(null)
                    .pinUserNickname(null)
                    .discount(null)
                    .storeImageUrl(null);
        }

        return b.build();
    }

    private Optional<String> resolveMainImageUrl(Pin pin) {
        List<PinImage> images = pin.getPinImages();
        if (images == null || images.isEmpty()) {
            return Optional.empty();
        }
        return images.stream()
                .filter(PinImage::isMainImage)
                .findFirst()
                .map(PinImage::getPinS3Url)
                .or(
                        () -> images.stream()
                                .min(Comparator.comparing(PinImage::getPinImageId))
                                .map(PinImage::getPinS3Url));
    }
}
