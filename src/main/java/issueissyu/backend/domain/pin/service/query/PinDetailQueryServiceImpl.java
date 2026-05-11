package issueissyu.backend.domain.pin.service.query;

import issueissyu.backend.domain.community.entity.Community;
import issueissyu.backend.domain.community.repository.CommunityRepository;
import issueissyu.backend.domain.issue.repository.IssuePinRepository;
import issueissyu.backend.domain.location.repository.PinLocationRepository;
import issueissyu.backend.domain.pin.dto.res.PinHomeResDTO;
import issueissyu.backend.domain.pin.dto.res.PinImageWithIdResDTO;
import issueissyu.backend.domain.pin.dto.res.PinPostResDTO;
import issueissyu.backend.domain.pin.entity.EventPin;
import issueissyu.backend.domain.pin.entity.Pin;
import issueissyu.backend.domain.pin.entity.PinImage;
import issueissyu.backend.domain.pin.enums.PinType;
import issueissyu.backend.domain.pin.exception.PinException;
import issueissyu.backend.domain.pin.exception.code.PinErrorCode;
import issueissyu.backend.domain.pin.exception.code.PinSuccessCode;
import issueissyu.backend.domain.pin.repository.DeclarationRepository;
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
public class PinDetailQueryServiceImpl implements PinDetailQueryService {

    private final PinRepository pinRepository;
    private final PinLocationRepository pinLocationRepository;
    private final IssuePinRepository issuePinRepository;
    private final EventPinRepository eventPinRepository;
    private final PinLikeRepository pinLikeRepository;
    private final DeclarationRepository declarationRepository;
    private final UserProfileImageQueryService userProfileImageQueryService;
    private final CommunityRepository communityRepository;

    @Override
    @Transactional(readOnly = false)
    public PinHomeResult getPinHome(Long pinId, String uid) {
        try {
            Pin pin =
                    pinRepository
                            .fetchDetailWithAuthor(pinId)
                            .orElseThrow(() -> PinException.of(PinErrorCode.PIN_HOME_404));
            int viewCount = pinRepository.incrementViewCountAndGetCount(pinId);
            PinType type = pin.getPinType();
            PinSuccessCode success =
                    switch (type) {
                        case ISSUE -> PinSuccessCode.PIN_HOME_200_1;
                        case COMMUNICATION -> PinSuccessCode.PIN_HOME_200_2;
                        case STORE -> PinSuccessCode.PIN_HOME_200_3;
                        case FESTIVAL -> PinSuccessCode.PIN_HOME_200_4;
                    };

            String detailAddr =
                    pinLocationRepository
                            .findFirstByPin_PinIdOrderByPinLocationIdAsc(pinId)
                            .map(pl -> pl.getDetailAddress())
                            .orElse(null);

            boolean isLike =
                    uid != null && pinLikeRepository.existsByPin_PinIdAndUser_Uid(pinId, uid);
            boolean isReported =
                    uid != null
                            && declarationRepository.existsByPin_PinIdAndUser_Uid(pinId, uid);

            List<PinImageWithIdResDTO> pinImages = homePinImagesForType(type, pin.getPinImages());

            Optional<EventPin> eventOpt = eventPinRepository.findWithStoreImageByPinPinId(pinId);

            String issueState =
                    type == PinType.ISSUE
                            ? issuePinRepository
                                    .findByPin_PinId(pinId)
                                    .map(ip -> ip.getIssuePinState().name())
                                    .orElse(null)
                            : null;

            String discount =
                    type == PinType.STORE
                            ? eventOpt.map(EventPin::getDiscount).orElse(null)
                            : null;
            String storeUrl =
                    type == PinType.STORE
                            ? eventOpt
                                    .map(EventPin::getStoreImage)
                                    .map(si -> si.getImageS3Url())
                                    .orElse(null)
                            : null;

            User author = pin.getUser();
            String pinUserId = null;
            String pinProfile = null;
            String pinNickname = null;

            switch (type) {
                case ISSUE -> {
                    pinUserId = author.getUid();
                    pinProfile = userProfileImageQueryService.findUrlByUserUid(author.getUid()).orElse(null);
                    pinNickname = author.getNickname();
                }
                case STORE -> {
                    pinUserId = author.getUid();
                }
                case COMMUNICATION -> {
                    pinUserId = author.getUid();
                    pinProfile =
                            userProfileImageQueryService
                                    .findUrlByUserUid(author.getUid())
                                    .orElse(null);
                    pinNickname = author.getNickname();
                }
                case FESTIVAL -> {
                    // 명세: 작성자 노출 필드 null
                }
            }

            boolean isMine = uid != null && Objects.equals(author.getUid(), uid);

            boolean isUpdated =
                    pin.getUpdatedAt() != null
                            && pin.getCreatedAt() != null
                            && pin.getUpdatedAt().isAfter(pin.getCreatedAt());

            Long communityId =
                    type == PinType.ISSUE
                            ? null
                            : communityRepository
                                    .findByPin_PinId(pinId)
                                    .map(Community::getCommunityId)
                                    .orElse(null);

            PinHomeResDTO dto =
                    new PinHomeResDTO(
                            pin.getPinId(),
                            type.name(),
                            pin.getPinTitle(),
                            pin.getPinContent(),
                            issueState,
                            detailAddr,
                            (long) pin.getLikeCount(),
                            isLike,
                            pinUserId,
                            pinProfile,
                            pinNickname,
                            pinImages,
                            discount,
                            storeUrl,
                            isUpdated,
                            pin.getCreatedAt(),
                            pin.getUpdatedAt(),
                            viewCount,
                            isReported,
                            isMine,
                            communityId);

            return new PinHomeResult(success, dto);
        } catch (PinException e) {
            throw e;
        } catch (Exception e) {
            throw PinException.of(PinErrorCode.PIN_HOME_400);
        }
    }

    @Override
    public PinPostResult getPinPost(Long pinId, String uid) {
        try {
            Pin pin =
                    pinRepository
                            .fetchDetailWithAuthor(pinId)
                            .orElseThrow(() -> PinException.of(PinErrorCode.PIN_POST_404));
            PinType type = pin.getPinType();
            PinSuccessCode success =
                    switch (type) {
                        case ISSUE -> PinSuccessCode.PIN_POST_200_1;
                        case COMMUNICATION -> PinSuccessCode.PIN_POST_200_2;
                        case STORE -> PinSuccessCode.PIN_POST_200_3;
                        case FESTIVAL -> PinSuccessCode.PIN_POST_200_4;
                    };

            boolean isLike = pinLikeRepository.existsByPin_PinIdAndUser_Uid(pinId, uid);
            Optional<EventPin> eventOpt = eventPinRepository.findWithStoreImageByPinPinId(pinId);

            String mainPinImageUrl = resolveMainPinImageUrl(type, pin, eventOpt);

            User author = pin.getUser();
            String pinUserId = null;
            String pinProfile = null;
            String pinNickname = null;

            switch (type) {
                case ISSUE, COMMUNICATION -> {
                    pinUserId = author.getUid();
                    pinProfile = userProfileImageQueryService.findUrlByUserUid(author.getUid()).orElse(null);
                    pinNickname = author.getNickname();
                }
                case STORE, FESTIVAL -> {}
            }

            String discount =
                    type == PinType.STORE
                            ? eventOpt.map(EventPin::getDiscount).orElse(null)
                            : null;
            String storeUrl =
                    type == PinType.STORE
                            ? eventOpt
                                    .map(EventPin::getStoreImage)
                                    .map(si -> si.getImageS3Url())
                                    .orElse(null)
                            : null;

            PinPostResDTO dto =
                    new PinPostResDTO(
                            pin.getPinId(),
                            type.name(),
                            pin.getPinTitle(),
                            (long) pin.getLikeCount(),
                            isLike,
                            pinUserId,
                            pinProfile,
                            pinNickname,
                            mainPinImageUrl,
                            discount,
                            storeUrl);

            return new PinPostResult(success, dto);
        } catch (PinException e) {
            throw e;
        } catch (Exception e) {
            throw PinException.of(PinErrorCode.PIN_POST_400);
        }
    }

    private static String resolveMainPinImageUrl(
            PinType type, Pin pin, Optional<EventPin> eventOpt) {
        if (type == PinType.FESTIVAL) {
            return pin.getPinImages().stream()
                    .filter(PinImage::isMainImage)
                    .map(PinImage::getPinS3Url)
                    .findFirst()
                    .orElseGet(
                            () ->
                                    pin.getPinImages().stream()
                                            .min(Comparator.comparing(PinImage::getPinImageId))
                                            .map(PinImage::getPinS3Url)
                                            .orElse(null));
        }
        return null;
    }

    private static List<PinImageWithIdResDTO> homePinImagesForType(PinType type, List<PinImage> images) {
        if (type == PinType.STORE) {
            return toImageDtosMainOnly(images);
        }
        return toImageDtos(images);
    }

    private static List<PinImageWithIdResDTO> toImageDtos(List<PinImage> images) {
        if (images == null || images.isEmpty()) {
            return List.of();
        }
        return images.stream()
                .sorted(Comparator.comparing(PinImage::getPinImageId))
                .map(
                        pi ->
                                new PinImageWithIdResDTO(
                                        pi.getPinImageId(), pi.getPinS3Url(), pi.isMainImage()))
                .toList();
    }

    /** 가게 핀: 명세상 핀 이미지는 대표(isMain)만 노출 */
    private static List<PinImageWithIdResDTO> toImageDtosMainOnly(List<PinImage> images) {
        if (images == null || images.isEmpty()) {
            return List.of();
        }
        return images.stream()
                .filter(PinImage::isMainImage)
                .sorted(Comparator.comparing(PinImage::getPinImageId))
                .map(
                        pi ->
                                new PinImageWithIdResDTO(
                                        pi.getPinImageId(), pi.getPinS3Url(), pi.isMainImage()))
                .toList();
    }
}
