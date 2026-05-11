package issueissyu.backend.domain.map.service.query;

import issueissyu.backend.domain.issue.repository.IssuePinRepository;
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

    @Override
    public MapPinCardResDTO findPinCard(Long pinId, String currentUserUid) {
        Pin pin =
                pinRepository
                        .fetchDetailWithAuthor(pinId)
                        .orElseThrow(() -> MapException.of(MapErrorCode.MAP_CARD_404));

        PinType type = pin.getPinType();
        User author = pin.getUser();

        String detailAddr =
                pinLocationRepository
                        .findFirstByPin_PinIdOrderByPinLocationIdAsc(pinId)
                        .map(pl -> pl.getDetailAddress())
                        .orElse(null);

        Optional<String> mainImageUrlOpt = resolveMainImageUrl(pin);

        long likeCountLong = pin.getLikeCount();

        boolean isLike =
                pinLikeRepository.existsByPin_PinIdAndUser_Uid(pinId, currentUserUid);

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
                        .pinImageUrl(mainImageUrlOpt.orElse(null))
                        .discount(null)
                        .storeImageUrl(null);

        switch (type) {
            case ISSUE -> b.pinUserId(author.getUid())
                    .pinUserProfile(profileImgOpt.orElse(null))
                    .pinUserNickname(author.getNickname());
            case COMMUNICATION ->
                    b.pinUserId(author.getUid())
                            .pinUserProfile(profileImgOpt.orElse("default"))
                            .pinUserNickname(
                                    Optional.ofNullable(author.getNickname()).orElse(""));
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
