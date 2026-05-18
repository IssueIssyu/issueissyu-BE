package issueissyu.backend.domain.community.service.query;

import issueissyu.backend.domain.community.dto.res.CommunicationCommunityFeedItemResDTO;
import issueissyu.backend.domain.community.dto.res.CommunityCursorPageResDTO;
import issueissyu.backend.domain.community.dto.res.CommunityDetailItemResDTO;
import issueissyu.backend.domain.community.dto.res.CommunityDetailResDTO;
import issueissyu.backend.domain.community.dto.res.CommunityFeedItemResDTO;
import issueissyu.backend.domain.community.dto.res.IssueCommunityDetailItemResDTO;
import issueissyu.backend.domain.community.dto.res.IssueCommunityFeedItemResDTO;
import issueissyu.backend.domain.community.dto.res.StoreCommunityFeedItemResDTO;
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
import issueissyu.backend.global.api.code.GeneralErrorCode;
import issueissyu.backend.global.exception.GeneralException;
import issueissyu.backend.domain.user.service.query.UserProfileImageQueryService;
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

    private static final List<CommunityType> IMPLEMENTED_FEED_TYPES = List.of(
            CommunityType.ISSUE,
            CommunityType.STORE,
            CommunityType.COMMUNICATION,
            CommunityType.FESTIVAL,
            CommunityType.POLICY,
            CommunityType.CONTEST,
            CommunityType.CARDNEWS
    );

    private boolean isPinBasedType(CommunityType type) {
        return type == CommunityType.ISSUE
                || type == CommunityType.STORE
                || type == CommunityType.FESTIVAL
                || type == CommunityType.COMMUNICATION;
    }

    private boolean isCommunityBasedType(CommunityType type) {
        return type == CommunityType.POLICY
                || type == CommunityType.CONTEST
                || type == CommunityType.CARDNEWS;
    }

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

    private static final int HOT_DAYS = 7;

    @Override
    // 탭/지역/커서 기준으로 피드 한 페이지를 만든다.
    public CommunityCursorPageResDTO getCommunityFeed(
            CommunityTab tab, String region, String cursor, int size) {

        if (!locationRepository.existsByRegion(region)) {
            throw CommunityException.of(CommunityErrorCode.COMMUNITY_400_2);
        }

        // 커서 구조 다르므로 분리
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

        String nextCursor = hasNext ? CursorKey.from(pageItems.get(pageItems.size() - 1)).encode() : null;
        return new CommunityCursorPageResDTO(region, items, nextCursor, hasNext);
    }

    @Override
    @Transactional(readOnly = false)
    public CommunityDetailResDTO getCommunityDetail(Long communityId, String uid) {
        userRepository.findById(uid)
                .orElseThrow(() -> GeneralException.of(GeneralErrorCode.USER_NOT_FOUND));

        Community community = communityRepository.findDetailById(communityId)
                .orElseThrow(() -> CommunityException.of(CommunityErrorCode.COMMUNITY_404_1));
        Pin pin = community.getPin();
        int viewCount = pinRepository.incrementViewCountAndGetCount(pin.getPinId());
        Long pinId = pin.getPinId();
        CommunityType type = community.getCommunityType();

        CommunityDetailItemResDTO item = toDetailItem(community, viewCount);
        List<String> pinImageUrls = pinImageRepository
                .findByPin_PinIdOrderByPinImageIdAsc(pinId)
                .stream()
                .map(PinImage::getPinS3Url)
                .toList();

        // STORE는 content가 피드 item 안에 포함되므로 래퍼의 content는 null
        String detailContent = type == CommunityType.STORE ? null : community.getContent();

        // 신고 여부 - ISSUE, COMMUNICATION, STORE 타입만
        Boolean isReported = (type == CommunityType.ISSUE
                        || type == CommunityType.COMMUNICATION
                        || type == CommunityType.STORE)
                ? declarationRepository.existsByPin_PinIdAndUser_Uid(pinId, uid)
                : null;

        // 청원·지금가요 여부 - ISSUE 타입만
        Boolean isPetitioned = type == CommunityType.ISSUE
                ? issuePetitionRepository.existsByIssuePin_Pin_PinIdAndUser_Uid(pinId, uid)
                : null;
        Boolean isProblemSolver = type == CommunityType.ISSUE
                ? problemSolverRepository.existsByIssuePin_Pin_PinIdAndUser_Uid(pinId, uid)
                : null;

        boolean isMine = Objects.equals(pin.getUser().getUid(), uid);

        return new CommunityDetailResDTO(
                item,
                detailContent,
                pinImageUrls,
                community.getCreatedAt(),
                community.getUpdatedAt(),
                isReported,
                isPetitioned,
                isProblemSolver,
                isMine);
    }

    // 상세 조회용 DTO 분기 (ISSUE는 추가 데이터 포함, 나머지는 피드 DTO 재사용)
    private CommunityDetailItemResDTO toDetailItem(Community community, int viewCount) {
        return switch (community.getCommunityType()) {
            case ISSUE -> toIssueDetailItem(community, viewCount);
            case STORE -> toStoreFeedItem(community, viewCount);
            case COMMUNICATION -> toCommunicationFeedItem(community, viewCount);
            case FESTIVAL -> toFestivalDetailItem(community, viewCount);
            case POLICY -> toPolicyDetailItem(community, viewCount);
            case CONTEST -> toContestDetailItem(community, viewCount);
            case CARDNEWS -> toCardnewsDetailItem(community, viewCount);
        };
    }

    // ISSUE 상세 DTO 매핑 (issuePinState 포함)
    private IssueCommunityDetailItemResDTO toIssueDetailItem(Community community, int viewCount) {
        Pin pin = community.getPin();
        IssuePin issuePin = issuePinRepository.findByPin_PinId(pin.getPinId()).orElse(null);

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
                issuePin != null ? issuePin.getIssuePinState().name() : null);
    }

    // 탭 규칙에 맞는 community 목록을 조회한다.
    private List<Community> fetchCommunities(CommunityTab tab, String region, CursorKey cursorKey) {
        Pageable limit = PageRequest.of(0, sizeWithLookahead(cursorKey.requestSize()));
        return switch (tab) {
            case ISSUE -> communityRepository.findFeedByTypeAndRegion(
                    CommunityType.ISSUE, region, cursorKey.createdAt(), cursorKey.communityId(), limit);
            case STORE -> communityRepository.findFeedByTypeAndRegion(
                    CommunityType.STORE, region, cursorKey.createdAt(), cursorKey.communityId(), limit);
            case COMMUNICATION -> communityRepository.findFeedByTypeAndRegion(
                    CommunityType.COMMUNICATION, region, cursorKey.createdAt(), cursorKey.communityId(), limit);
            case FESTIVAL -> communityRepository.findFeedByTypeAndRegion(
                    CommunityType.FESTIVAL, region, cursorKey.createdAt(), cursorKey.communityId(), limit);
            case POLICY -> communityRepository.findFeedByTypeAndRegion(
                    CommunityType.POLICY, region, cursorKey.createdAt(), cursorKey.communityId(), limit);
            case CONTEST -> communityRepository.findFeedByTypeAndRegion(
                    CommunityType.CONTEST, region, cursorKey.createdAt(), cursorKey.communityId(), limit);
            case CARDNEWS -> communityRepository.findFeedByTypeAndRegion(
                    CommunityType.CARDNEWS, region, cursorKey.createdAt(), cursorKey.communityId(), limit);
            case ALL -> communityRepository.findFeedByTypesAndRegion(
                    IMPLEMENTED_FEED_TYPES, region, cursorKey.createdAt(), cursorKey.communityId(), limit);
        };
    }

    // communityType에 맞는 피드 DTO로 분기한다.
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

    // ISSUE 카드 DTO 매핑.(목록 용)
    private IssueCommunityFeedItemResDTO toIssueFeedItem(Community community) {
        Pin pin = community.getPin();
        return new IssueCommunityFeedItemResDTO(
                community.getCommunityId(),
                pin.getPinId(),
                community.getTitle(),
                resolvePinThumbnailUrl(pin.getPinId()).orElse(null),
                pin.getUser().getNickname(),
                userProfileImageQueryService.findUrlByUserUid(pin.getUser().getUid()).orElse(null),
                resolveAddress(pin.getPinId()),
                pin.getViewCount(),
                pin.getLikeCount());
    }

    // STORE 카드 DTO 매핑.
    private StoreCommunityFeedItemResDTO toStoreFeedItem(Community community) {
        return toStoreFeedItem(community, community.getPin().getViewCount());
    }

    private StoreCommunityFeedItemResDTO toStoreFeedItem(Community community, int viewCount) {
        Pin pin = community.getPin();
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
                pin.getLikeCount());
    }

    // COMMUNICATION 카드 DTO 매핑.
    private CommunicationCommunityFeedItemResDTO toCommunicationFeedItem(Community community) {
        return toCommunicationFeedItem(community, community.getPin().getViewCount());
    }

    private CommunicationCommunityFeedItemResDTO toCommunicationFeedItem(Community community, int viewCount) {
        Pin pin = community.getPin();
        return new CommunicationCommunityFeedItemResDTO(
                community.getCommunityId(),
                pin.getPinId(),
                community.getTitle(),
                resolvePinThumbnailUrl(pin.getPinId()).orElse(null),
                pin.getUser().getNickname(),
                userProfileImageQueryService.findUrlByUserUid(pin.getUser().getUid()).orElse(null),
                resolveAddress(pin.getPinId()),
                viewCount,
                pin.getLikeCount());
    }

    // 핀의 주소 조회.
    private String resolveAddress(Long pinId) {
        return pinLocationRepository.findByPin_PinId(pinId)
                .map(PinLocation::getDetailAddress)
                .orElse(null);
    }

    // 핀 썸네일 URL(대표 이미지 우선, 없으면 첫 이미지) 조회.
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
        // 현재 시각 기준 7일 전 — 이보다 오래된 글은 HOT 후보에서 제외
        LocalDateTime since = LocalDateTime.now().minusDays(HOT_DAYS);
        // hasNext 판단을 위해 size+1개 조회
        Pageable limit = PageRequest.of(0, sizeWithLookahead(size));

        List<Community> communities = communityRepository.findHotFeedByTypesAndRegion(
                IMPLEMENTED_FEED_TYPES,
                region,
                since,
                cursorKey.popularity(),
                cursorKey.communityId(),
                limit);

        boolean hasNext = communities.size() > size;
        // size+1개 중 실제 페이지 크기만큼만 잘라냄
        List<Community> pageItems = hasNext ? communities.subList(0, size) : communities;

        List<CommunityFeedItemResDTO> items = pageItems.stream()
                .map(this::toFeedItem)
                .toList();
        String nextCursor = hasNext
                ? HotCursorKey.from(pageItems.get(pageItems.size() - 1)).encode()
                : null;
        return new CommunityCursorPageResDTO(region, items, nextCursor, hasNext);
    }

    // hasNext 판단을 위해 요청 개수 + 1만큼 조회한다.
    private int sizeWithLookahead(int requestSize) {
        return Math.max(1, requestSize) + 1;
    }

    // 커서 파싱/인코딩용 키(createdAt + communityId).
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
            // cursor가 없으면(첫 페이지) null로 초기화 — 쿼리에서 null이면 전체 대상
            if (raw == null || raw.isBlank()) {
                return new HotCursorKey(null, null, requestSize);
            }
            // "popularity|communityId" 형태로 인코딩되어 있어서 | 기준으로 분리
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
        // 페이지 마지막 community에서 다음 커서 생성
        private static HotCursorKey from(Community community) {
            return new HotCursorKey(community.getPopularity(), community.getCommunityId(), 0);
        }

        // "popularity|communityId" 형태로 인코딩해서 클라이언트에 전달
        private String encode() {
            return popularity + "|" + communityId;
        }
    }
}
