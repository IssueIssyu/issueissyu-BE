package issueissyu.backend.domain.community.service.query;

import issueissyu.backend.domain.community.dto.res.CardnewsCommunityFeedItemResDTO;
import issueissyu.backend.domain.community.dto.res.CommunicationCommunityFeedItemResDTO;
import issueissyu.backend.domain.community.dto.res.CommunityCursorPageResDTO;
import issueissyu.backend.domain.community.dto.res.CommunityDetailItemResDTO;
import issueissyu.backend.domain.community.dto.res.CommunityDetailResDTO;
import issueissyu.backend.domain.community.dto.res.CommunityFeedItemResDTO;
import issueissyu.backend.domain.community.dto.res.ContestCommunityFeedItemResDTO;
import issueissyu.backend.domain.community.dto.res.FestivalCommunityFeedItemResDTO;
import issueissyu.backend.domain.community.dto.res.IssueCommunityDetailItemResDTO;
import issueissyu.backend.domain.community.dto.res.IssueCommunityFeedItemResDTO;
import issueissyu.backend.domain.community.dto.res.PolicyCommunityFeedItemResDTO;
import issueissyu.backend.domain.community.dto.res.StoreCommunityFeedItemResDTO;
import issueissyu.backend.domain.community.entity.CardnewsImageS3;
import issueissyu.backend.domain.community.entity.Community;
import issueissyu.backend.domain.community.enums.CommunityTab;
import issueissyu.backend.domain.community.enums.CommunityType;
import issueissyu.backend.domain.community.exception.CommunityException;
import issueissyu.backend.domain.community.exception.code.CommunityErrorCode;
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

    private static final List<CommunityType> PIN_BASED_FEED_TYPES = List.of(
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
        validateRegionIfNeeded(tab, region);

        if (tab == CommunityTab.HOT) {
            return getHotFeed(region, cursor, size);
        }

        CursorKey cursorKey = CursorKey.parse(cursor, size);
        List<Community> communities = fetchCommunities(tab, region, cursorKey);

        boolean hasNext = communities.size() > size;
        List<Community> pageItems = hasNext ? communities.subList(0, size) : communities;

        List<CommunityFeedItemResDTO> items = pageItems.stream()
                .map(this::toFeedItem)
                .toList();

        String nextCursor = hasNext
                ? CursorKey.from(pageItems.get(pageItems.size() - 1)).encode()
                : null;

        return new CommunityCursorPageResDTO(region, items, nextCursor, hasNext);
    }

    @Override
    @Transactional(readOnly = false)
    public CommunityDetailResDTO getCommunityDetail(Long communityId, String uid) {
        userRepository.findById(uid)
                .orElseThrow(() -> GeneralException.of(GeneralErrorCode.USER_NOT_FOUND));

        Community community = communityRepository.findDetailById(communityId)
                .orElseThrow(() -> CommunityException.of(CommunityErrorCode.COMMUNITY_404_1));

        validatePinConsistency(community);

        CommunityType type = community.getCommunityType();
        Pin pin = community.getPin();

        int viewCount = increaseDetailViewCount(community);

        CommunityDetailItemResDTO item = toDetailItem(community, viewCount);
        List<String> imageUrls = resolveDetailImageUrls(community);

        String detailContent = resolveWrapperDetailContent(community);

        Boolean isReported = pin != null && isReportableType(type)
                ? declarationRepository.existsByPin_PinIdAndUser_Uid(pin.getPinId(), uid)
                : null;

        Boolean isPetitioned = type == CommunityType.ISSUE && pin != null
                ? issuePetitionRepository.existsByIssuePin_Pin_PinIdAndUser_Uid(pin.getPinId(), uid)
                : null;

        Boolean isProblemSolver = type == CommunityType.ISSUE && pin != null
                ? problemSolverRepository.existsByIssuePin_Pin_PinIdAndUser_Uid(pin.getPinId(), uid)
                : null;

        boolean isMine = pin != null && Objects.equals(pin.getUser().getUid(), uid);

        return new CommunityDetailResDTO(
                item,
                detailContent,
                imageUrls,
                community.getCreatedAt(),
                community.getUpdatedAt(),
                isReported,
                isPetitioned,
                isProblemSolver,
                isMine
        );
    }

    private void validateRegionIfNeeded(CommunityTab tab, String region) {
        if (usesRegion(tab) && !locationRepository.existsByRegion(region)) {
            throw CommunityException.of(CommunityErrorCode.COMMUNITY_400_2);
        }
    }

    private boolean usesRegion(CommunityTab tab) {
        return tab == CommunityTab.ISSUE
                || tab == CommunityTab.STORE
                || tab == CommunityTab.COMMUNICATION
                || tab == CommunityTab.FESTIVAL
                || tab == CommunityTab.ALL
                || tab == CommunityTab.HOT;
    }

    private void validatePinConsistency(Community community) {
        if (community.requiresPin() && !community.hasPin()) {
            throw CommunityException.of(CommunityErrorCode.COMMUNITY_404_1);
        }
    }

    private int increaseDetailViewCount(Community community) {
        if (community.hasPin()) {
            return pinRepository.incrementViewCountAndGetCount(community.getPin().getPinId());
        }

        community.increaseViewCount();
        return community.getViewCount();
    }

    private String resolveWrapperDetailContent(Community community) {
        CommunityType type = community.getCommunityType();

        if (type == CommunityType.ISSUE || type == CommunityType.COMMUNICATION) {
            return community.getContent();
        }

        return null;
    }

    private boolean isReportableType(CommunityType type) {
        return type == CommunityType.ISSUE
                || type == CommunityType.STORE
                || type == CommunityType.FESTIVAL
                || type == CommunityType.COMMUNICATION;
    }

    private List<String> resolveDetailImageUrls(Community community) {
        if (community.hasPin()) {
            return pinImageRepository
                    .findByPin_PinIdOrderByPinImageIdAsc(community.getPin().getPinId())
                    .stream()
                    .map(PinImage::getPinS3Url)
                    .toList();
        }

        if (community.getCommunityType() == CommunityType.CARDNEWS) {
            return community.getCardnewsImages()
                    .stream()
                    .map(CardnewsImageS3::getCardnewsImageS3Url)
                    .toList();
        }

        return List.of();
    }

    private CommunityDetailItemResDTO toDetailItem(Community community, int viewCount) {
        return switch (community.getCommunityType()) {
            case ISSUE -> toIssueDetailItem(community, viewCount);
            case STORE -> toStoreFeedItem(community, viewCount);
            case COMMUNICATION -> toCommunicationFeedItem(community, viewCount);
            case FESTIVAL -> toFestivalFeedItem(community, viewCount);
            case POLICY -> toPolicyFeedItem(community, viewCount);
            case CONTEST -> toContestFeedItem(community, viewCount);
            case CARDNEWS -> toCardnewsFeedItem(community, viewCount);
        };
    }

    private IssueCommunityDetailItemResDTO toIssueDetailItem(Community community, int viewCount) {
        Pin pin = getRequiredPin(community);

        String issuePinState = issuePinRepository.findByPin_PinId(pin.getPinId())
                .map(IssuePin::getIssuePinState)
                .map(Enum::name)
                .orElse(null);

        return new IssueCommunityDetailItemResDTO(
                community.getCommunityId(),
                pin.getPinId(),
                community.getTitle(),
                resolvePinThumbnailUrl(pin.getPinId()).orElse(null),
                pin.getUser().getNickname(),
                userProfileImageQueryService.findUrlByUserUid(pin.getUser().getUid()).orElse(null),
                resolveAddress(pin.getPinId()),
                viewCount,
                pin.getLikeCount(),
                issuePinState
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

            case POLICY -> communityRepository.findFeedByType(
                    CommunityType.POLICY,
                    cursorKey.createdAt(),
                    cursorKey.communityId(),
                    limit
            );

            case CONTEST -> communityRepository.findFeedByType(
                    CommunityType.CONTEST,
                    cursorKey.createdAt(),
                    cursorKey.communityId(),
                    limit
            );

            case CARDNEWS -> communityRepository.findFeedByType(
                    CommunityType.CARDNEWS,
                    cursorKey.createdAt(),
                    cursorKey.communityId(),
                    limit
            );

            case ALL -> communityRepository.findFeedByRegionOrGlobalTypes(
                    PIN_BASED_FEED_TYPES,
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
        return switch (community.getCommunityType()) {
            case ISSUE -> toIssueFeedItem(community);
            case STORE -> toStoreFeedItem(community);
            case COMMUNICATION -> toCommunicationFeedItem(community);
            case FESTIVAL -> toFestivalFeedItem(community);
            case POLICY -> toPolicyFeedItem(community);
            case CONTEST -> toContestFeedItem(community);
            case CARDNEWS -> toCardnewsFeedItem(community);
        };
    }

    private IssueCommunityFeedItemResDTO toIssueFeedItem(Community community) {
        Pin pin = getRequiredPin(community);

        return new IssueCommunityFeedItemResDTO(
                community.getCommunityId(),
                pin.getPinId(),
                community.getTitle(),
                resolvePinThumbnailUrl(pin.getPinId()).orElse(null),
                pin.getUser().getNickname(),
                userProfileImageQueryService.findUrlByUserUid(pin.getUser().getUid()).orElse(null),
                resolveAddress(pin.getPinId()),
                pin.getViewCount(),
                pin.getLikeCount()
        );
    }

    private StoreCommunityFeedItemResDTO toStoreFeedItem(Community community) {
        return toStoreFeedItem(community, getRequiredPin(community).getViewCount());
    }

    private StoreCommunityFeedItemResDTO toStoreFeedItem(Community community, int viewCount) {
        Pin pin = getRequiredPin(community);
        Optional<StoreImage> storeImage = storeImageRepository.findByEventPin_Pin_PinId(pin.getPinId());
        Optional<EventPin> eventPin = eventPinRepository.findByPin_PinId(pin.getPinId());

        return new StoreCommunityFeedItemResDTO(
                community.getCommunityId(),
                pin.getPinTitle(),
                storeImage.map(StoreImage::getImageS3Url)
                        .or(() -> resolvePinThumbnailUrl(pin.getPinId()))
                        .orElse(null),
                community.getContent(),
                eventPin.map(EventPin::getDiscount).orElse(null),
                resolveAddress(pin.getPinId()),
                eventPin.map(EventPin::getEventStartTime).orElse(null),
                eventPin.map(EventPin::getEventEndTime).orElse(null),
                viewCount,
                pin.getLikeCount()
        );
    }

    private CommunicationCommunityFeedItemResDTO toCommunicationFeedItem(Community community) {
        return toCommunicationFeedItem(community, getRequiredPin(community).getViewCount());
    }

    private CommunicationCommunityFeedItemResDTO toCommunicationFeedItem(Community community, int viewCount) {
        Pin pin = getRequiredPin(community);

        return new CommunicationCommunityFeedItemResDTO(
                community.getCommunityId(),
                pin.getPinId(),
                community.getTitle(),
                resolvePinThumbnailUrl(pin.getPinId()).orElse(null),
                pin.getUser().getNickname(),
                userProfileImageQueryService.findUrlByUserUid(pin.getUser().getUid()).orElse(null),
                resolveAddress(pin.getPinId()),
                viewCount,
                pin.getLikeCount()
        );
    }

    private FestivalCommunityFeedItemResDTO toFestivalFeedItem(Community community) {
        return toFestivalFeedItem(community, getRequiredPin(community).getViewCount());
    }

    private FestivalCommunityFeedItemResDTO toFestivalFeedItem(Community community, int viewCount) {
        Pin pin = getRequiredPin(community);
        Optional<StoreImage> storeImage = storeImageRepository.findByEventPin_Pin_PinId(pin.getPinId());
        Optional<EventPin> eventPin = eventPinRepository.findByPin_PinId(pin.getPinId());

        return new FestivalCommunityFeedItemResDTO(
                community.getCommunityId(),
                pin.getPinId(),
                pin.getPinTitle(),
                storeImage.map(StoreImage::getImageS3Url)
                        .or(() -> resolvePinThumbnailUrl(pin.getPinId()))
                        .orElse(null),
                community.getContent(),
                eventPin.map(EventPin::getDiscount).orElse(null),
                resolveAddress(pin.getPinId()),
                eventPin.map(EventPin::getEventStartTime).orElse(null),
                eventPin.map(EventPin::getEventEndTime).orElse(null),
                viewCount,
                pin.getLikeCount()
        );
    }

    private PolicyCommunityFeedItemResDTO toPolicyFeedItem(Community community) {
        return toPolicyFeedItem(community, community.getViewCount());
    }

    private PolicyCommunityFeedItemResDTO toPolicyFeedItem(Community community, int viewCount) {
        return new PolicyCommunityFeedItemResDTO(
                community.getCommunityId(),
                community.getTitle(),
                community.getContent(),
                viewCount,
                community.getLikeCount()
        );
    }

    private ContestCommunityFeedItemResDTO toContestFeedItem(Community community) {
        return toContestFeedItem(community, community.getViewCount());
    }

    private ContestCommunityFeedItemResDTO toContestFeedItem(Community community, int viewCount) {
        return new ContestCommunityFeedItemResDTO(
                community.getCommunityId(),
                community.getTitle(),
                community.getContent(),
                viewCount,
                community.getLikeCount()
        );
    }

    private CardnewsCommunityFeedItemResDTO toCardnewsFeedItem(Community community) {
        return toCardnewsFeedItem(community, community.getViewCount());
    }

    private CardnewsCommunityFeedItemResDTO toCardnewsFeedItem(Community community, int viewCount) {
        return new CardnewsCommunityFeedItemResDTO(
                community.getCommunityId(),
                community.getTitle(),
                community.getContent(),
                resolveCardnewsThumbnailUrl(community).orElse(null),
                viewCount,
                community.getLikeCount()
        );
    }

    private Optional<String> resolveCardnewsThumbnailUrl(Community community) {
        return community.getCardnewsImages()
                .stream()
                .map(CardnewsImageS3::getCardnewsImageS3Url)
                .findFirst();
    }

    private Pin getRequiredPin(Community community) {
        if (!community.hasPin()) {
            throw CommunityException.of(CommunityErrorCode.COMMUNITY_404_1);
        }

        return community.getPin();
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

    private CommunityCursorPageResDTO getHotFeed(String region, String cursor, int size) {
        HotCursorKey cursorKey = HotCursorKey.parse(cursor, size);
        LocalDateTime since = LocalDateTime.now().minusDays(HOT_DAYS);
        Pageable limit = PageRequest.of(0, sizeWithLookahead(size));

        List<Community> communities = communityRepository.findHotFeedByRegionOrGlobalTypes(
                PIN_BASED_FEED_TYPES,
                GLOBAL_FEED_TYPES,
                region,
                since,
                cursorKey.popularity(),
                cursorKey.communityId(),
                limit
        );

        boolean hasNext = communities.size() > size;
        List<Community> pageItems = hasNext ? communities.subList(0, size) : communities;

        List<CommunityFeedItemResDTO> items = pageItems.stream()
                .map(this::toFeedItem)
                .toList();

        String nextCursor = hasNext
                ? HotCursorKey.from(pageItems.get(pageItems.size() - 1)).encode()
                : null;

        return new CommunityCursorPageResDTO(region, items, nextCursor, hasNext);
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