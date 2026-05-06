package issueissyu.backend.domain.community.service.query;

import issueissyu.backend.domain.community.dto.res.CommunicationCommunityFeedItemResDTO;
import issueissyu.backend.domain.community.dto.res.CommunityCursorPageResDTO;
import issueissyu.backend.domain.community.dto.res.CommunityDetailResDTO;
import issueissyu.backend.domain.community.dto.res.CommunityFeedItemResDTO;
import issueissyu.backend.domain.community.dto.res.FestivalCommunityFeedItemResDTO;
import issueissyu.backend.domain.community.dto.res.IssueCommunityFeedItemResDTO;
import issueissyu.backend.domain.community.dto.res.StoreCommunityFeedItemResDTO;
import issueissyu.backend.domain.community.entity.Community;
import issueissyu.backend.domain.community.enums.CommunityTab;
import issueissyu.backend.domain.community.enums.CommunityType;
import issueissyu.backend.domain.community.exception.CommunityException;
import issueissyu.backend.domain.community.exception.code.CommunityErrorCode;
import issueissyu.backend.domain.community.repository.CommunityRepository;
import issueissyu.backend.domain.location.enums.RegionCode;
import issueissyu.backend.domain.location.repository.PinLocationRepository;
import issueissyu.backend.domain.pin.entity.EventPin;
import issueissyu.backend.domain.pin.entity.Pin;
import issueissyu.backend.domain.pin.entity.PinImage;
import issueissyu.backend.domain.pin.entity.StoreImage;
import issueissyu.backend.domain.pin.repository.EventPinRepository;
import issueissyu.backend.domain.pin.repository.PinImageRepository;
import issueissyu.backend.domain.pin.repository.StoreImageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommunityQueryServiceImpl implements CommunityQueryService {

    private static final List<CommunityType> IMPLEMENTED_FEED_TYPES = List.of(
            CommunityType.ISSUE,
            CommunityType.STORE,
            CommunityType.FESTIVAL,
            CommunityType.COMMUNICATION
    );

    private final CommunityRepository communityRepository;
    private final PinLocationRepository pinLocationRepository;
    private final EventPinRepository eventPinRepository;
    private final StoreImageRepository storeImageRepository;
    private final PinImageRepository pinImageRepository;

    @Override
    // 탭/지역/커서 기준으로 피드 한 페이지를 만든다.
    public CommunityCursorPageResDTO getCommunityFeed(
            CommunityTab tab, RegionCode region, String cursor, int size) {
        CursorKey cursorKey = CursorKey.parse(cursor, size);
        List<Community> communities = fetchCommunities(tab, region, cursorKey);
        boolean hasNext = communities.size() > size;
        List<Community> pageItems = hasNext ? communities.subList(0, size) : communities;

        List<CommunityFeedItemResDTO> items = pageItems.stream()
                .map(this::toFeedItem)
                .toList();

        String nextCursor = hasNext ? CursorKey.from(pageItems.get(pageItems.size() - 1)).encode() : null;
        return new CommunityCursorPageResDTO(items, nextCursor, hasNext);
    }

    @Override
    @Transactional(readOnly = false)
    public CommunityDetailResDTO getCommunityDetail(Long communityId) {
        Community community = communityRepository.findDetailById(communityId)
                .orElseThrow(() -> CommunityException.of(CommunityErrorCode.COMMUNITY_404_1));
        community.incrementViewCount();
        CommunityFeedItemResDTO item = toFeedItem(community);
        return new CommunityDetailResDTO(
                item,
                community.getContent(),
                community.getCreatedAt(),
                community.getUpdatedAt());
    }

    // 탭 규칙에 맞는 community 목록을 조회한다.
    private List<Community> fetchCommunities(CommunityTab tab, RegionCode region, CursorKey cursorKey) {
        Pageable limit = PageRequest.of(0, sizeWithLookahead(cursorKey.requestSize()));
        return switch (tab) {
            case ISSUE -> communityRepository.findFeedByTypeAndRegion(
                    CommunityType.ISSUE, region, cursorKey.createdAt(), cursorKey.communityId(), limit);
            case STORE -> communityRepository.findFeedByTypeAndRegion(
                    CommunityType.STORE, region, cursorKey.createdAt(), cursorKey.communityId(), limit);
            case FESTIVAL -> communityRepository.findFeedByTypeAndRegion(
                    CommunityType.FESTIVAL, region, cursorKey.createdAt(), cursorKey.communityId(), limit);
            case COMMUNICATION -> communityRepository.findFeedByTypeAndRegion(
                    CommunityType.COMMUNICATION, region, cursorKey.createdAt(), cursorKey.communityId(), limit);
            case ALL -> communityRepository.findFeedByTypesAndRegion(
                    IMPLEMENTED_FEED_TYPES, region, cursorKey.createdAt(), cursorKey.communityId(), limit);
            case HOT, POLICY, CONTEST, CARDNEWS -> List.of();
        };
    }

    // communityType에 맞는 피드 DTO로 분기한다.
    private CommunityFeedItemResDTO toFeedItem(Community community) {
        return switch (community.getCommunityType()) {
            case ISSUE -> toIssueFeedItem(community);
            case STORE -> toStoreFeedItem(community);
            case FESTIVAL -> toFestivalFeedItem(community);
            case COMMUNICATION -> toCommunicationFeedItem(community);
            case POLICY, CONTEST, CARDNEWS -> throw new IllegalStateException("Unsupported feed type");
        };
    }

    // ISSUE 카드 DTO 매핑.
    private IssueCommunityFeedItemResDTO toIssueFeedItem(Community community) {
        Pin pin = community.getPin();
        return new IssueCommunityFeedItemResDTO(
                community.getCommunityId(),
                pin.getPinId(),
                community.getTitle(),
                resolvePinThumbnailUrl(pin.getPinId()).orElse(null),
                pin.getUser().getNickname(),
                pin.getUser().getProfileImageUrl(),
                resolveAddress(pin.getPinId()),
                community.getViewCount(),
                pin.getLikeCount());
    }

    // STORE 카드 DTO 매핑.
    private StoreCommunityFeedItemResDTO toStoreFeedItem(Community community) {
        Pin pin = community.getPin();
        Optional<StoreImage> storeImage = storeImageRepository.findByEventPin_Pin_PinId(pin.getPinId());
        Optional<EventPin> eventPin = eventPinRepository.findByPin_PinId(pin.getPinId());
        return new StoreCommunityFeedItemResDTO(
                community.getCommunityId(),
                pin.getPinId(),
                pin.getPinTitle(),
                storeImage.map(StoreImage::getImageS3Url)
                        .or(() -> resolvePinThumbnailUrl(pin.getPinId()))
                        .orElse(null),
                pin.getUser().getNickname(),
                pin.getUser().getProfileImageUrl(),
                eventPin.map(EventPin::getDiscount).orElse(null),
                resolveAddress(pin.getPinId()),
                community.getViewCount(),
                pin.getLikeCount());
    }

    // FESTIVAL 카드 DTO 매핑.
    private FestivalCommunityFeedItemResDTO toFestivalFeedItem(Community community) {
        Pin pin = community.getPin();
        Optional<EventPin> eventPin = eventPinRepository.findByPin_PinId(pin.getPinId());
        return new FestivalCommunityFeedItemResDTO(
                community.getCommunityId(),
                pin.getPinId(),
                community.getTitle(),
                resolvePinThumbnailUrl(pin.getPinId()).orElse(null),
                resolveAddress(pin.getPinId()),
                community.getViewCount(),
                pin.getLikeCount(),
                eventPin.map(EventPin::getEventStartTime).orElse(null),
                eventPin.map(EventPin::getEventEndTime).orElse(null)
        );
    }

    // COMMUNICATION 카드 DTO 매핑.
    private CommunicationCommunityFeedItemResDTO toCommunicationFeedItem(Community community) {
        Pin pin = community.getPin();
        return new CommunicationCommunityFeedItemResDTO(
                community.getCommunityId(),
                pin.getPinId(),
                community.getTitle(),
                resolvePinThumbnailUrl(pin.getPinId()).orElse(null),
                pin.getUser().getNickname(),
                pin.getUser().getProfileImageUrl(),
                resolveAddress(pin.getPinId()),
                community.getViewCount(),
                pin.getLikeCount());
    }

    // 핀의 대표 주소(최초 위치 1건) 조회.
    private String resolveAddress(Long pinId) {
        return pinLocationRepository.findFirstByPin_PinIdOrderByPinLocationIdAsc(pinId)
                .map(pl -> pl.getDetailAddress())
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
}
