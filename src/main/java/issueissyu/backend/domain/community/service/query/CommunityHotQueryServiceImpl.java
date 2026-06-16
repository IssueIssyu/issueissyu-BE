package issueissyu.backend.domain.community.service.query;

import issueissyu.backend.domain.community.dto.HotCommunityTarget;
import issueissyu.backend.domain.community.entity.Community;
import issueissyu.backend.domain.community.enums.CommunityType;
import issueissyu.backend.domain.community.repository.CommunityRepository;
import issueissyu.backend.domain.pin.entity.Pin;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommunityHotQueryServiceImpl implements CommunityHotQueryService {

    private static final int HOT_DAYS = 7;

    private static final List<CommunityType> REGION_BASED_FEED_TYPES = List.of(
            CommunityType.ISSUE,
            CommunityType.STORE,
            CommunityType.COMMUNICATION,
            CommunityType.FESTIVAL);

    private static final List<CommunityType> GLOBAL_FEED_TYPES =
            List.of(CommunityType.POLICY, CommunityType.CONTEST, CommunityType.CARDNEWS);

    private final CommunityRepository communityRepository;

    @Override
    public Optional<HotCommunityTarget> findTopHotInRegion(Long locationId) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime since = LocalDateTime.now().minusDays(HOT_DAYS);
        Pageable limit = PageRequest.of(0, 1);

        List<Community> communities = communityRepository.findHotFeedByRegionOrGlobalTypes(
                REGION_BASED_FEED_TYPES,
                GLOBAL_FEED_TYPES,
                now,
                locationId,
                since,
                null,
                null,
                limit);

        if (communities.isEmpty()) {
            return Optional.empty();
        }

        Community top = communities.getFirst();
        Pin pin = top.getPin();

        return Optional.of(new HotCommunityTarget(
                top.getCommunityId(), pin.getPinId(), pin.getPinTitle(), pin.getViewCount()));
    }
}
