package issueissyu.backend.domain.community.service.query;

import issueissyu.backend.domain.community.dto.res.CommunityCursorPageResDTO;
import issueissyu.backend.domain.community.dto.res.CommunityDetailResDTO;
import issueissyu.backend.domain.community.dto.res.CommunityFeedItemResDTO;
import issueissyu.backend.domain.community.entity.CardnewsImageS3;
import issueissyu.backend.domain.community.entity.Community;
import issueissyu.backend.domain.community.enums.CommunityTab;
import issueissyu.backend.domain.community.enums.CommunityType;
import issueissyu.backend.domain.community.exception.CommunityException;
import issueissyu.backend.domain.community.exception.code.CommunityErrorCode;
import issueissyu.backend.domain.community.exception.code.CommunitySuccessCode;
import issueissyu.backend.domain.community.repository.CommunityRepository;
import issueissyu.backend.domain.issue.entity.IssuePin;
import issueissyu.backend.domain.issue.repository.IssuePetitionRepository;
import issueissyu.backend.domain.issue.repository.IssuePinRepository;
import issueissyu.backend.domain.issue.repository.ProblemSolverRepository;
import issueissyu.backend.domain.location.entity.PinLocation;
import issueissyu.backend.domain.location.repository.LocationRepository;
import issueissyu.backend.domain.location.repository.PinLocationRepository;
import issueissyu.backend.domain.pin.entity.EventPin;
import issueissyu.backend.domain.pin.entity.Pin;
import issueissyu.backend.domain.pin.entity.PinImage;
import issueissyu.backend.domain.pin.entity.StoreImage;
import issueissyu.backend.domain.pin.repository.DeclarationRepository;
import issueissyu.backend.domain.pin.repository.EventPinRepository;
import issueissyu.backend.domain.pin.repository.PinImageRepository;
import issueissyu.backend.domain.pin.repository.PinRepository;
import issueissyu.backend.domain.pin.repository.StoreImageRepository;
import issueissyu.backend.domain.user.repository.UserRepository;
import issueissyu.backend.domain.user.service.query.UserProfileImageQueryService;
import issueissyu.backend.global.api.code.GeneralErrorCode;
import issueissyu.backend.global.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommunityQueryServiceImpl implements CommunityQueryService {

    private static final List<CommunityType> FEED_TYPES = List.of(
            CommunityType.ISSUE,
            CommunityType.STORE,
            CommunityType.COMMUNICATION,
            CommunityType.FESTIVAL,
            CommunityType.POLICY,
            CommunityType.CONTEST,
            CommunityType.CARDNEWS
    );

    private static final List<CommunityType> REGION_BASED_FEED_TYPES = List.of(
            CommunityType.ISSUE,
            CommunityType.STORE,
            CommunityType.COMMUNICATION,
            CommunityType.FESTIVAL
    );

    private static final List<CommunityType> GLOBAL_FEED_TYPES = List.of(
            CommunityType.POLICY,
            CommunityType.CONTEST,
            CommunityType.CARDNEWS
    );

    private static final int HOT_DAYS = 7;

    private final CommunityRepository communityRepository;
    private final LocationRepository locationRepository;
    private final PinLocationRepository pinLocationRepository;
    private final EventPinRepository eventPinRepository;
    private final StoreImageRepository storeImageRepository;
    private final PinImageRepository pinImageRepository;
    private final PinRepository pinRepository;
    private final IssuePinRepository issuePinRepository;
    private final IssuePetitionRepository issuePetitionRepository;
    private final ProblemSolverRepository problemSolverRepository;
    private final DeclarationRepository declarationRepository;
    private final UserRepository userRepository;
    private final UserProfileImageQueryService userProfileImageQueryService;

    @Override
    public CommunityCursorPageResDTO getCommunityFeed(
            CommunityTab tab,
            String region,
            String cursor,
            int size
    ) {
        if (!locationRepository.existsByRegion(region)) {
            throw CommunityException.of(CommunityErrorCode.COMMUNITY_400_2);
        }

        if (tab == CommunityTab.HOT) {
            return getHotFeed(region, cursor, size);
        }

        CursorKey cursorKey = CursorKey.parse(cursor, size);
        List<Community> communities = fetchCommunities(tab, region, cursorKey);

        boolean hasNext = communities.size() > size;
        List<Community> pageItems = hasNext ? communities.subList(0, size) : communities;

        List<CommunityFeedItemResDTO> content = pageItems.stream()
                .map(this::toFeedItem)
                .toList();

        String nextCursor = hasNext
                ? CursorKey.from(pageItems.get(pageItems.size() - 1)).encode()
                : null;

        return new CommunityCursorPageResDTO(region, content, nextCursor, hasNext);
    }

    @Override
    @Transactional(readOnly = false)
    public CommunityQueryService.CommunityDetailResult getCommunityDetail(Long communityId, String uid) {
        userRepository.findById(uid)
                .orElseThrow(() -> GeneralException.of(GeneralErrorCode.USER_NOT_FOUND));

        Community community = communityRepository.findDetailById(communityId)
                .orElseThrow(() -> CommunityException.of(CommunityErrorCode.COMMUNITY_404_1));

        CommunityType type = community.getCommunityType();
        Pin pin = community.getPin();

        int viewCount = pinRepository.incrementViewCountAndGetCount(pin.getPinId());

        IssuePin issuePin = type == CommunityType.ISSUE
                ? issuePinRepository.findByPin_PinId(pin.getPinId()).orElse(null)
                : null;

        Boolean isReported = isReportableType(type)
                ? declarationRepository.existsByPin_PinIdAndUser_Uid(pin.getPinId(), uid)
                : null;

        Boolean isPetitioned = type == CommunityType.ISSUE
                ? issuePetitionRepository.existsByIssuePin_Pin_PinIdAndUser_Uid(pin.getPinId(), uid)
                : null;

        Boolean isProblemSolver = type == CommunityType.ISSUE
                ? problemSolverRepository.existsByIssuePin_Pin_PinIdAndUser_Uid(pin.getPinId(), uid)
                : null;

        String issuePinState = issuePin != null
                ? issuePin.getIssuePinState().name()
                : null;

        Integer petitionCount = issuePin != null
                ? issuePin.getPetitionCount()
                : null;

        boolean isMine = Objects.equals(pin.getUser().getUid(), uid);

        CommunityDetailResDTO detail = toDetailRes(
                community,
                viewCount,
                isReported,
                isPetitioned,
                isProblemSolver,
                issuePinState,
                petitionCount,
                isMine
        );

        return new CommunityQueryService.CommunityDetailResult(
                CommunitySuccessCode.forType(type),
                detail
        );
    }

    private List<Community> fetchCommunities(CommunityTab tab, String region, CursorKey cursorKey) {
        Pageable limit = PageRequest.of(0, sizeWithLookahead(cursorKey.requestSize()));

        return switch (tab) {
            case ISSUE -> communityRepository.findFeedByTypeAndRegion(
                    CommunityType.ISSUE,
                    region,
                    cursorKey.createdAt(),
                    cursorKey.communityId(),
                    limit
            );

            case STORE -> communityRepository.findFeedByTypeAndRegion(
                    CommunityType.STORE,
                    region,
                    cursorKey.createdAt(),
                    cursorKey.communityId(),
                    limit
            );

            case COMMUNICATION -> communityRepository.findFeedByTypeAndRegion(
                    CommunityType.COMMUNICATION,
                    region,
                    cursorKey.createdAt(),
                    cursorKey.communityId(),
                    limit
            );

            case FESTIVAL -> communityRepository.findFeedByTypeAndRegion(
                    CommunityType.FESTIVAL,
                    region,
                    cursorKey.createdAt(),
                    cursorKey.communityId(),
                    limit
            );

            case POLICY -> communityRepository.findFeedByTypeAndRegion(
                    CommunityType.POLICY,
                    region,
                    cursorKey.createdAt(),
                    cursorKey.communityId(),
                    limit
            );

            case CONTEST -> communityRepository.findFeedByTypeAndRegion(
                    CommunityType.CONTEST,
                    region,
                    cursorKey.createdAt(),
                    cursorKey.communityId(),
                    limit
            );

            case CARDNEWS -> communityRepository.findFeedByTypeAndRegion(
                    CommunityType.CARDNEWS,
                    region,
                    cursorKey.createdAt(),
                    cursorKey.communityId(),
                    limit
            );

            case ALL -> communityRepository.findFeedByRegionOrGlobalTypes(
                    REGION_BASED_FEED_TYPES,
                    GLOBAL_FEED_TYPES,
                    region,
                    cursorKey.createdAt(),
                    cursorKey.communityId(),
                    limit
            );

            case HOT -> List.of();
        };
    }

    private CommunityFeedItemResDTO toFeedItem(Community community) {
        Pin pin = community.getPin();
        CommunityType type = community.getCommunityType();
        Optional<EventPin> eventPin = resolveEventPin(type, pin.getPinId());

        return new CommunityFeedItemResDTO(
                type,
                community.getCommunityId(),
                pin.getPinId(),
                pin.getPinTitle(),
                pin.getPinContent(),
                resolveThumbnailUrl(community).orElse(null),
                pin.getUser().getNickname(),
                userProfileImageQueryService.findUrlByUserUid(pin.getUser().getUid()).orElse(null),
                resolveAddress(pin.getPinId()),
                pin.getViewCount(),
                pin.getLikeCount(),
                eventPin.map(EventPin::getDiscount).orElse(null),
                eventPin.map(EventPin::getEventStartTime).orElse(null),
                eventPin.map(EventPin::getEventEndTime).orElse(null)
        );
    }

    private CommunityDetailResDTO toDetailRes(
            Community community,
            int viewCount,
            Boolean isReported,
            Boolean isPetitioned,
            Boolean isProblemSolver,
            String issuePinState,
            Integer petitionCount,
            boolean isMine
    ) {
        Pin pin = community.getPin();
        CommunityType type = community.getCommunityType();
        Optional<EventPin> eventPin = resolveEventPin(type, pin.getPinId());

        return new CommunityDetailResDTO(
                type,
                community.getCommunityId(),
                pin.getPinId(),
                pin.getPinTitle(),
                pin.getPinContent(),
                resolveThumbnailUrl(community).orElse(null),
                resolveDetailImageUrls(community),
                pin.getUser().getNickname(),
                userProfileImageQueryService.findUrlByUserUid(pin.getUser().getUid()).orElse(null),
                resolveAddress(pin.getPinId()),
                viewCount,
                pin.getLikeCount(),
                eventPin.map(EventPin::getDiscount).orElse(null),
                eventPin.map(EventPin::getEventStartTime).orElse(null),
                eventPin.map(EventPin::getEventEndTime).orElse(null),
                community.getCreatedAt(),
                community.getUpdatedAt(),
                isReported,
                isPetitioned,
                isProblemSolver,
                issuePinState,
                petitionCount,
                isMine
        );
    }

    private Optional<EventPin> resolveEventPin(CommunityType type, Long pinId) {
        if (type == CommunityType.STORE || type == CommunityType.FESTIVAL) {
            return eventPinRepository.findByPin_PinId(pinId);
        }

        return Optional.empty();
    }

    private Optional<String> resolveThumbnailUrl(Community community) {
        if (community.getCommunityType() == CommunityType.CARDNEWS) {
            return resolveCardnewsThumbnailUrl(community);
        }

        if (community.getCommunityType() == CommunityType.STORE
                || community.getCommunityType() == CommunityType.FESTIVAL) {
            Optional<String> storeImageUrl = storeImageRepository
                    .findByEventPin_Pin_PinId(community.getPin().getPinId())
                    .map(StoreImage::getImageS3Url);

            if (storeImageUrl.isPresent()) {
                return storeImageUrl;
            }
        }

        return resolvePinThumbnailUrl(community.getPin().getPinId());
    }

    private List<String> resolveDetailImageUrls(Community community) {
        if (community.getCommunityType() == CommunityType.CARDNEWS) {
            return community.getCardnewsImages()
                    .stream()
                    .map(CardnewsImageS3::getCardnewsImageS3Url)
                    .toList();
        }

        return pinImageRepository
                .findByPin_PinIdOrderByPinImageIdAsc(community.getPin().getPinId())
                .stream()
                .map(PinImage::getPinS3Url)
                .toList();
    }

    private Optional<String> resolveCardnewsThumbnailUrl(Community community) {
        return community.getCardnewsImages()
                .stream()
                .map(CardnewsImageS3::getCardnewsImageS3Url)
                .findFirst();
    }

    private String resolveAddress(Long pinId) {
        return pinLocationRepository.findByPin_PinId(pinId)
                .map(PinLocation::getDetailAddress)
                .orElse(null);
    }

    private Optional<String> resolvePinThumbnailUrl(Long pinId) {
        Optional<String> mainImage = pinImageRepository.findFirstByPin_PinIdAndMainImageTrue(pinId)
                .map(PinImage::getPinS3Url);

        return mainImage.isPresent()
                ? mainImage
                : pinImageRepository.findFirstByPin_PinIdOrderByPinImageIdAsc(pinId)
                .map(PinImage::getPinS3Url);
    }

    private boolean isReportableType(CommunityType type) {
        return type == CommunityType.ISSUE
                || type == CommunityType.STORE
                || type == CommunityType.FESTIVAL
                || type == CommunityType.COMMUNICATION
                || type == CommunityType.POLICY
                || type == CommunityType.CONTEST
                || type == CommunityType.CARDNEWS;
    }

    private CommunityCursorPageResDTO getHotFeed(String region, String cursor, int size) {
        HotCursorKey cursorKey = HotCursorKey.parse(cursor, size);
        LocalDateTime since = LocalDateTime.now().minusDays(HOT_DAYS);
        Pageable limit = PageRequest.of(0, sizeWithLookahead(size));

        List<Community> communities = communityRepository.findHotFeedByRegionOrGlobalTypes(
                REGION_BASED_FEED_TYPES,
                GLOBAL_FEED_TYPES,
                region,
                since,
                cursorKey.popularity(),
                cursorKey.communityId(),
                limit
        );

        boolean hasNext = communities.size() > size;
        List<Community> pageItems = hasNext ? communities.subList(0, size) : communities;

        List<CommunityFeedItemResDTO> content = pageItems.stream()
                .map(this::toFeedItem)
                .toList();

        String nextCursor = hasNext
                ? HotCursorKey.from(pageItems.get(pageItems.size() - 1)).encode()
                : null;

        return new CommunityCursorPageResDTO(region, content, nextCursor, hasNext);
    }

    private int sizeWithLookahead(int requestSize) {
        return Math.max(1, requestSize) + 1;
    }

    private record CursorKey(LocalDateTime createdAt, Long communityId, int requestSize) {

        private static CursorKey parse(String raw, int requestSize) {
            if (raw == null || raw.isBlank()) {
                return new CursorKey(null, null, requestSize);
            }

            String[] parts = raw.split("\\|", 2);

            if (parts.length != 2) {
                throw CommunityException.of(CommunityErrorCode.COMMUNITY_400_3);
            }

            try {
                LocalDateTime createdAt = LocalDateTime.parse(parts[0]);
                Long communityId = Long.parseLong(parts[1]);

                return new CursorKey(createdAt, communityId, requestSize);
            } catch (DateTimeParseException | NumberFormatException e) {
                throw CommunityException.of(CommunityErrorCode.COMMUNITY_400_3);
            }
        }

        private static CursorKey from(Community community) {
            return new CursorKey(community.getCreatedAt(), community.getCommunityId(), 0);
        }

        private String encode() {
            return createdAt + "|" + communityId;
        }
    }

    private record HotCursorKey(Double popularity, Long communityId, int requestSize) {

        private static HotCursorKey parse(String raw, int requestSize) {
            if (raw == null || raw.isBlank()) {
                return new HotCursorKey(null, null, requestSize);
            }

            String[] parts = raw.split("\\|", 2);

            if (parts.length != 2) {
                throw CommunityException.of(CommunityErrorCode.COMMUNITY_400_3);
            }

            try {
                Double popularity = Double.parseDouble(parts[0]);
                Long communityId = Long.parseLong(parts[1]);

                return new HotCursorKey(popularity, communityId, requestSize);
            } catch (NumberFormatException e) {
                throw CommunityException.of(CommunityErrorCode.COMMUNITY_400_3);
            }
        }

        private static HotCursorKey from(Community community) {
            return new HotCursorKey(community.getPopularity(), community.getCommunityId(), 0);
        }

        private String encode() {
            return popularity + "|" + communityId;
        }
    }
}