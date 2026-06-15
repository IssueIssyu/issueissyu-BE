package issueissyu.backend.domain.community.service.query;

import issueissyu.backend.domain.community.dto.res.CommunityCursorPageResDTO;
import issueissyu.backend.domain.community.dto.res.CommunityDetailResDTO;
import issueissyu.backend.domain.community.dto.res.CommunityFeedItemResDTO;
import issueissyu.backend.domain.community.dto.res.CommunityHomeResDTO;
import issueissyu.backend.domain.community.entity.CardnewsImageS3;
import issueissyu.backend.domain.community.entity.Community;
import issueissyu.backend.domain.community.enums.CommunityTab;
import issueissyu.backend.domain.community.enums.CommunityType;
import issueissyu.backend.domain.community.exception.CommunityException;
import issueissyu.backend.domain.community.exception.code.CommunityErrorCode;
import issueissyu.backend.domain.community.exception.code.CommunitySuccessCode;
import issueissyu.backend.domain.community.repository.CardnewsImageS3Repository;
import issueissyu.backend.domain.community.repository.CommunityRepository;
import issueissyu.backend.domain.issue.entity.IssuePin;
import issueissyu.backend.domain.issue.repository.IssuePetitionRepository;
import issueissyu.backend.domain.issue.repository.IssuePinRepository;
import issueissyu.backend.domain.issue.repository.ProblemSolverRepository;
import issueissyu.backend.domain.location.entity.PinLocation;
import issueissyu.backend.domain.location.exception.LocationException;
import issueissyu.backend.domain.location.repository.LocationRepository;
import issueissyu.backend.domain.location.repository.PinLocationRepository;
import issueissyu.backend.domain.location.service.LocationService;
import issueissyu.backend.domain.pin.entity.EventPin;
import issueissyu.backend.domain.pin.entity.Pin;
import issueissyu.backend.domain.pin.entity.PinImage;
import issueissyu.backend.domain.pin.entity.StoreImage;
import issueissyu.backend.domain.pin.repository.DeclarationRepository;
import issueissyu.backend.domain.pin.repository.EventPinRepository;
import issueissyu.backend.domain.pin.repository.PinImageRepository;
import issueissyu.backend.domain.pin.repository.PinLikeRepository;
import issueissyu.backend.domain.pin.repository.PinRepository;
import issueissyu.backend.domain.pin.repository.StoreImageRepository;
import issueissyu.backend.domain.user.entity.User;
import issueissyu.backend.domain.user.enums.UserRole;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

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

    private static final List<CommunityType> CARDNEWS_FEED_SOURCE_TYPES = List.of(
            CommunityType.POLICY,
            CommunityType.CONTEST
    );

    // HOT 인기 점수 계산용
    private static final int HOT_DAYS = 7;
    // 홈에서 사용
    private static final int HOT_PREVIEW_SIZE = 3;
    private static final int MAX_STORE_SIZE = 10;

    private final CommunityRepository communityRepository;
    private final CardnewsImageS3Repository cardnewsImageS3Repository;
    private final LocationRepository locationRepository;
    private final LocationService locationService;
    private final PinLocationRepository pinLocationRepository;
    private final EventPinRepository eventPinRepository;
    private final StoreImageRepository storeImageRepository;
    private final PinImageRepository pinImageRepository;
    private final PinRepository pinRepository;
    private final IssuePinRepository issuePinRepository;
    private final IssuePetitionRepository issuePetitionRepository;
    private final ProblemSolverRepository problemSolverRepository;
    private final DeclarationRepository declarationRepository;
    private final PinLikeRepository pinLikeRepository;
    private final UserRepository userRepository;
    private final UserProfileImageQueryService userProfileImageQueryService;

    @Override
    public CommunityHomeResDTO getCommunityHome(
            String uid,
            Long locationId,
            String recentCursor,
            int storeSize,
            int recentSize
    ) {
        Long resolvedLocationId = resolveLocationId(uid, locationId);

        boolean isInitialLoad = recentCursor == null || recentCursor.isBlank();

        List<CommunityFeedItemResDTO> storePromotions =
                isInitialLoad ? fetchStorePromotions(resolvedLocationId, storeSize) : List.of();

        List<CommunityFeedItemResDTO> hotPreviews =
                isInitialLoad ? fetchHotPreviews(resolvedLocationId) : List.of();

        CommunityCursorPageResDTO recentNews = fetchRecentNews(resolvedLocationId, recentCursor, recentSize);

        return new CommunityHomeResDTO(resolvedLocationId, storePromotions, hotPreviews, recentNews);
    }

    @Override
    public CommunityCursorPageResDTO getCommunityFeed(
            CommunityTab tab,
            String uid,
            Long locationId,
            String cursor,
            int size
    ) {
        Long resolvedLocationId = usesRegion(tab) ? resolveLocationId(uid, locationId) : locationId;

        if (tab == CommunityTab.HOT) {
            return getHotFeed(resolvedLocationId, cursor, size);
        }

        CursorKey cursorKey = CursorKey.parse(cursor, size);
        List<Community> communities = fetchCommunities(tab, resolvedLocationId, cursorKey);

        boolean hasNext = communities.size() > size;
        List<Community> pageItems = hasNext ? communities.subList(0, size) : communities;

        List<CommunityFeedItemResDTO> content = toFeedItems(pageItems);
        if (tab == CommunityTab.CARDNEWS) {
            content = content.stream()
                    .map(item -> new CommunityFeedItemResDTO(
                            CommunityType.CARDNEWS,
                            item.communityId(),
                            item.pinId(),
                            item.title(),
                            item.content(),
                            item.thumbnailUrl(),
                            item.writerNickname(),
                            item.writerProfileUrl(),
                            item.detailAddress(),
                            item.viewCount(),
                            item.likeCount(),
                            item.discount(),
                            item.eventStartTime(),
                            item.eventEndTime()
                    ))
                    .toList();
        }

        String nextCursor = hasNext
                ? CursorKey.from(pageItems.get(pageItems.size() - 1)).encode()
                : null;

        return new CommunityCursorPageResDTO(resolvedLocationId, content, nextCursor, hasNext);
    }

    @Override
    @Transactional(readOnly = false)
    public CommunityQueryService.CommunityDetailResult getCommunityDetail(
            Long communityId,
            CommunityType kind,
            String uid
    ) {
        userRepository.findById(uid)
                .orElseThrow(() -> GeneralException.of(GeneralErrorCode.USER_NOT_FOUND));

        Community community = communityRepository.findDetailById(communityId)
                .orElseThrow(() -> CommunityException.of(CommunityErrorCode.COMMUNITY_404_1));

        CommunityType type = community.getCommunityType();
        CommunityType responseKind = resolveDetailResponseKind(type, kind, community);
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
        boolean isLike = pinLikeRepository.existsByPin_PinIdAndUser_Uid(pin.getPinId(), uid);

        CommunityDetailResDTO detail = toDetailRes(
                community,
                responseKind,
                viewCount,
                isLike,
                isReported,
                isPetitioned,
                isProblemSolver,
                issuePinState,
                petitionCount,
                isMine
        );

        return new CommunityQueryService.CommunityDetailResult(
                CommunitySuccessCode.forType(responseKind),
                detail
        );
    }

    private List<Community> fetchCommunities(CommunityTab tab, Long locationId, CursorKey cursorKey) {
        Pageable limit = PageRequest.of(0, sizeWithLookahead(cursorKey.requestSize()));

        return switch (tab) {
            case ISSUE -> communityRepository.findFeedByTypeAndRegion(
                    CommunityType.ISSUE,
                    locationId,
                    cursorKey.createdAt(),
                    cursorKey.communityId(),
                    limit
            );

            case STORE -> communityRepository.findFeedByTypeAndRegion(
                    CommunityType.STORE,
                    locationId,
                    cursorKey.createdAt(),
                    cursorKey.communityId(),
                    limit
            );

            case COMMUNICATION -> communityRepository.findFeedByTypeAndRegion(
                    CommunityType.COMMUNICATION,
                    locationId,
                    cursorKey.createdAt(),
                    cursorKey.communityId(),
                    limit
            );

            case FESTIVAL -> communityRepository.findFeedByTypeAndRegion(
                    CommunityType.FESTIVAL,
                    locationId,
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

            case CARDNEWS -> communityRepository.findCardnewsFeedByTypesWithImages(
                    CARDNEWS_FEED_SOURCE_TYPES,
                    cursorKey.createdAt(),
                    cursorKey.communityId(),
                    limit
            );

            case ALL -> communityRepository.findFeedByRegionOrGlobalTypes(
                    REGION_BASED_FEED_TYPES,
                    GLOBAL_FEED_TYPES,
                    locationId,
                    cursorKey.createdAt(),
                    cursorKey.communityId(),
                    limit
            );

            case HOME, HOT -> List.of();
        };
    }

    private Long resolveLocationId(String uid, Long locationId) {
        if (locationId != null) {
            if (!locationRepository.existsById(locationId)) {
                throw CommunityException.of(CommunityErrorCode.COMMUNITY_400_2);
            }
            return locationId;
        }
        try {
            return locationService.getUserCertifiedLocationId(uid);
        } catch (LocationException e) {
            throw CommunityException.of(CommunityErrorCode.COMMUNITY_400_4);
        }
    }

    private boolean usesRegion(CommunityTab tab) {
        return tab == CommunityTab.HOME
                || tab == CommunityTab.ISSUE
                || tab == CommunityTab.STORE
                || tab == CommunityTab.COMMUNICATION
                || tab == CommunityTab.FESTIVAL
                || tab == CommunityTab.ALL
                || tab == CommunityTab.HOT;
    }

    private List<CommunityFeedItemResDTO> fetchStorePromotions(Long locationId, int storeSize) {
        int resolvedSize = Math.min(Math.max(1, storeSize), MAX_STORE_SIZE);
        Pageable limit = PageRequest.of(0, resolvedSize);

        List<Community> communities = communityRepository.findFeedByTypeAndRegion(
                CommunityType.STORE,
                locationId,
                null,
                null,
                limit
        );

        return toFeedItems(communities);
    }

    private List<CommunityFeedItemResDTO> fetchHotPreviews(Long locationId) {
        LocalDateTime since = LocalDateTime.now().minusDays(HOT_DAYS);
        Pageable limit = PageRequest.of(0, HOT_PREVIEW_SIZE);

        return toFeedItems(communityRepository.findHotFeedByRegionOrGlobalTypes(
                REGION_BASED_FEED_TYPES,
                GLOBAL_FEED_TYPES,
                locationId,
                since,
                null,
                null,
                limit
        ));
    }

    private CommunityCursorPageResDTO fetchRecentNews(Long locationId, String recentCursor, int recentSize) {
        CursorKey cursorKey = CursorKey.parse(recentCursor, recentSize);
        Pageable limit = PageRequest.of(0, sizeWithLookahead(cursorKey.requestSize()));

        List<Community> communities = communityRepository.findFeedByTypesAndRegion(
                REGION_BASED_FEED_TYPES,
                locationId,
                cursorKey.createdAt(),
                cursorKey.communityId(),
                limit
        );

        boolean hasNext = communities.size() > cursorKey.requestSize();
        List<Community> pageItems =
                hasNext ? communities.subList(0, cursorKey.requestSize()) : communities;

        List<CommunityFeedItemResDTO> content = toFeedItems(pageItems);

        String nextCursor = hasNext
                ? CursorKey.from(pageItems.get(pageItems.size() - 1)).encode()
                : null;

        return new CommunityCursorPageResDTO(locationId, content, nextCursor, hasNext);
    }

    private List<CommunityFeedItemResDTO> toFeedItems(List<Community> communities) {
        FeedItemContext context = buildFeedItemContext(communities);
        return communities.stream()
                .map(community -> toFeedItem(community, context))
                .toList();
    }

    private static final Set<CommunityType> ADMIN_AUTHORED_TYPES = Set.of(
            CommunityType.FESTIVAL, CommunityType.POLICY, CommunityType.CONTEST, CommunityType.CARDNEWS
    );

    private FeedItemContext buildFeedItemContext(List<Community> communities) {
        if (communities.isEmpty()) {
            return new FeedItemContext(Map.of(), Map.of(), Map.of(), Map.of(), Map.of(), null, null);
        }

        List<Long> pinIds = communities.stream()
                .map(community -> community.getPin().getPinId())
                .distinct()
                .toList();

        Set<String> uids = communities.stream()
                .map(community -> community.getPin().getUser().getUid())
                .collect(Collectors.toSet());

        List<Long> eventPinIds = communities.stream()
                .filter(community -> community.getCommunityType() == CommunityType.STORE
                        || community.getCommunityType() == CommunityType.FESTIVAL)
                .map(community -> community.getPin().getPinId())
                .distinct()
                .toList();

        List<Long> cardnewsCommunityIds = communities.stream()
                .filter(community -> community.getCommunityType() == CommunityType.CARDNEWS
                        || community.getCommunityType() == CommunityType.POLICY
                        || community.getCommunityType() == CommunityType.CONTEST)
                .map(Community::getCommunityId)
                .distinct()
                .toList();

        Map<String, String> profileUrlByUid = userProfileImageQueryService.findUrlsByUserUids(uids);

        Map<Long, String> addressByPinId = new HashMap<>();
        for (PinLocation pinLocation : pinLocationRepository.findByPin_PinIdIn(pinIds)) {
            addressByPinId.putIfAbsent(pinLocation.getPin().getPinId(), pinLocation.getDetailAddress());
        }

        Map<Long, EventPin> eventPinByPinId = eventPinIds.isEmpty()
                ? Map.of()
                : eventPinRepository.findWithStoreImageByPinIdIn(eventPinIds).stream()
                        .collect(Collectors.toMap(
                                eventPin -> eventPin.getPin().getPinId(),
                                eventPin -> eventPin,
                                (left, right) -> left
                        ));

        Map<Long, String> pinThumbnailByPinId = new HashMap<>();
        for (PinImage pinImage : pinImageRepository
                .findByPin_PinIdInOrderByPin_PinIdAscMainImageDescPinImageIdAsc(pinIds)) {
            pinThumbnailByPinId.putIfAbsent(pinImage.getPin().getPinId(), pinImage.getPinS3Url());
        }

        Map<Long, String> cardnewsThumbnailByCommunityId = new HashMap<>();
        if (!cardnewsCommunityIds.isEmpty()) {
            for (CardnewsImageS3 cardnewsImage : cardnewsImageS3Repository
                    .findAllByCommunityCommunityIdInOrderByCardnewsImageS3IdAsc(cardnewsCommunityIds)) {
                cardnewsThumbnailByCommunityId.putIfAbsent(
                        cardnewsImage.getCommunity().getCommunityId(),
                        cardnewsImage.getCardnewsImageS3Url()
                );
            }
        }

        boolean hasAdminType = communities.stream()
                .anyMatch(c -> ADMIN_AUTHORED_TYPES.contains(c.getCommunityType()));

        String adminNickname = null;
        String adminProfileUrl = null;
        if (hasAdminType) {
            User admin = userRepository.findFirstByRole(UserRole.ADMIN).orElse(null);
            if (admin != null) {
                adminNickname = admin.getNickname();
                adminProfileUrl = userProfileImageQueryService.findUrlByUserUid(admin.getUid()).orElse(null);
            }
        }

        return new FeedItemContext(
                profileUrlByUid,
                addressByPinId,
                eventPinByPinId,
                pinThumbnailByPinId,
                cardnewsThumbnailByCommunityId,
                adminNickname,
                adminProfileUrl
        );
    }

    private CommunityFeedItemResDTO toFeedItem(Community community, FeedItemContext context) {
        Pin pin = community.getPin();
        CommunityType type = community.getCommunityType();
        Long pinId = pin.getPinId();
        EventPin eventPin = context.eventPinByPinId().get(pinId);

        return new CommunityFeedItemResDTO(
                type,
                community.getCommunityId(),
                pinId,
                pin.getPinTitle(),
                pin.getPinContent(),
                resolveFeedThumbnailUrl(community, context).orElse(null),
                resolveFeedWriterNickname(type, pin, eventPin, context),
                resolveFeedWriterProfileUrl(type, pin, eventPin, context),
                context.addressByPinId().get(pinId),
                pin.getViewCount(),
                pin.getLikeCount(),
                eventPin != null ? eventPin.getDiscount() : null,
                eventPin != null ? eventPin.getEventStartTime() : null,
                eventPin != null ? eventPin.getEventEndTime() : null
        );
    }

    private Optional<String> resolveFeedThumbnailUrl(Community community, FeedItemContext context) {
        Long pinId = community.getPin().getPinId();

        if (community.getCommunityType() == CommunityType.CARDNEWS
                || community.getCommunityType() == CommunityType.POLICY
                || community.getCommunityType() == CommunityType.CONTEST) {
            return Optional.ofNullable(context.cardnewsThumbnailByCommunityId().get(community.getCommunityId()));
        }

        if (community.getCommunityType() == CommunityType.STORE
                || community.getCommunityType() == CommunityType.FESTIVAL) {
            EventPin eventPin = context.eventPinByPinId().get(pinId);
            if (eventPin != null && eventPin.getStoreImage() != null) {
                return Optional.of(eventPin.getStoreImage().getImageS3Url());
            }
        }

        return Optional.ofNullable(context.pinThumbnailByPinId().get(pinId));
    }

    private record FeedItemContext(
            Map<String, String> profileUrlByUid,
            Map<Long, String> addressByPinId,
            Map<Long, EventPin> eventPinByPinId,
            Map<Long, String> pinThumbnailByPinId,
            Map<Long, String> cardnewsThumbnailByCommunityId,
            String adminNickname,
            String adminProfileUrl
    ) {
    }

    private String resolveFeedWriterNickname(CommunityType type, Pin pin, EventPin eventPin, FeedItemContext context) {
        if (type == CommunityType.STORE) {
            return pin.getPinTitle();
        }
        if (ADMIN_AUTHORED_TYPES.contains(type)) {
            return context.adminNickname() != null ? context.adminNickname() : pin.getUser().getNickname();
        }
        return pin.getUser().getNickname();
    }

    private String resolveFeedWriterProfileUrl(CommunityType type, Pin pin, EventPin eventPin, FeedItemContext context) {
        if (type == CommunityType.STORE) {
            return eventPin != null && eventPin.getStoreImage() != null
                    ? eventPin.getStoreImage().getImageS3Url()
                    : null;
        }
        if (ADMIN_AUTHORED_TYPES.contains(type)) {
            return context.adminProfileUrl();
        }
        return context.profileUrlByUid().get(pin.getUser().getUid());
    }

    private CommunityDetailResDTO toDetailRes(
            Community community,
            CommunityType responseKind,
            int viewCount,
            boolean isLike,
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
        boolean isCardnews = responseKind == CommunityType.CARDNEWS;

        String moveCardnews = null;
        if ((type == CommunityType.POLICY || type == CommunityType.CONTEST)
                && hasCardnewsImages(community)
                && !isCardnews) {
            moveCardnews = "/api/communities/" + community.getCommunityId() + "?kind=CARDNEWS";
        }

        return new CommunityDetailResDTO(
                responseKind,
                community.getCommunityId(),
                pin.getPinId(),
                pin.getPinTitle(),
                isCardnews ? null : pin.getPinContent(),
                resolveDetailImageUrls(community, responseKind),
                resolveWriterNickname(responseKind, pin),
                resolveWriterProfileUrl(responseKind, pin),
                resolveAddress(pin.getPinId()),
                viewCount,
                pin.getLikeCount(),
                isLike,
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
                isMine,
                moveCardnews
        );
    }

    private CommunityType resolveDetailResponseKind(
            CommunityType type,
            CommunityType kind,
            Community community
    ) {
        if (kind != CommunityType.CARDNEWS) {
            return type;
        }

        if (hasCardnewsImages(community)) {
            return CommunityType.CARDNEWS;
        }

        throw CommunityException.of(CommunityErrorCode.COMMUNITY_404_1);
    }

    private boolean hasCardnewsImages(Community community) {
        return !community.getCardnewsImages().isEmpty();
    }

    private Optional<EventPin> resolveEventPin(CommunityType type, Long pinId) {
        if (type == CommunityType.STORE || type == CommunityType.FESTIVAL) {
            return eventPinRepository.findByPin_PinId(pinId);
        }

        return Optional.empty();
    }

    private List<String> resolveDetailImageUrls(Community community, CommunityType responseKind) {
        if (responseKind == CommunityType.CARDNEWS) {
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

    private String resolveAddress(Long pinId) {
        return pinLocationRepository.findByPin_PinId(pinId)
                .map(PinLocation::getDetailAddress)
                .orElse(null);
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

    private String resolveWriterNickname(CommunityType type, Pin pin) {
        if (type == CommunityType.STORE) {
            return pin.getPinTitle();
        }
        if (type == CommunityType.FESTIVAL
                || type == CommunityType.POLICY
                || type == CommunityType.CONTEST
                || type == CommunityType.CARDNEWS) {
            return userRepository.findFirstByRole(UserRole.ADMIN)
                    .map(User::getNickname)
                    .orElse(pin.getUser().getNickname());
        }
        return pin.getUser().getNickname();
    }

    private String resolveWriterProfileUrl(CommunityType type, Pin pin) {
        if (type == CommunityType.STORE) {
            return storeImageRepository.findByEventPin_Pin_PinId(pin.getPinId())
                    .map(StoreImage::getImageS3Url)
                    .orElse(null);
        }
        if (type == CommunityType.FESTIVAL
                || type == CommunityType.POLICY
                || type == CommunityType.CONTEST
                || type == CommunityType.CARDNEWS) {
            return userRepository.findFirstByRole(UserRole.ADMIN)
                    .flatMap(admin -> userProfileImageQueryService.findUrlByUserUid(admin.getUid()))
                    .orElse(null);
        }
        return userProfileImageQueryService.findUrlByUserUid(pin.getUser().getUid()).orElse(null);
    }

    private CommunityCursorPageResDTO getHotFeed(Long locationId, String cursor, int size) {
        HotCursorKey cursorKey = HotCursorKey.parse(cursor, size);
        LocalDateTime since = LocalDateTime.now().minusDays(HOT_DAYS);
        Pageable limit = PageRequest.of(0, sizeWithLookahead(size));

        List<Community> communities = communityRepository.findHotFeedByRegionOrGlobalTypes(
                REGION_BASED_FEED_TYPES,
                GLOBAL_FEED_TYPES,
                locationId,
                since,
                cursorKey.popularity(),
                cursorKey.communityId(),
                limit
        );

        boolean hasNext = communities.size() > size;
        List<Community> pageItems = hasNext ? communities.subList(0, size) : communities;

        List<CommunityFeedItemResDTO> content = toFeedItems(pageItems);

        String nextCursor = hasNext
                ? HotCursorKey.from(pageItems.get(pageItems.size() - 1)).encode()
                : null;

        return new CommunityCursorPageResDTO(locationId, content, nextCursor, hasNext);
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