package issueissyu.backend.domain.community.service.query;

import issueissyu.backend.domain.community.entity.Community;
import issueissyu.backend.domain.community.enums.CommunityType;
import issueissyu.backend.domain.community.repository.CommunityRepository;
import issueissyu.backend.domain.location.service.query.LocationTargetQueryService;
import issueissyu.backend.domain.pin.entity.Pin;
import issueissyu.backend.domain.pin.enums.PinType;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommunityPromotionService {

    private final CommunityRepository communityRepository;
    private final LocationTargetQueryService locationTargetQueryService;

    @Transactional
    public void promoteIfTargetReached(Pin pin, int likeCount) {
        Long pinId = pin.getPinId();

        if (communityRepository.existsByPin_PinId(pinId)) {
            return;
        }

        int targetCommunity = locationTargetQueryService.getTargetCommunityByPinId(pinId);

        if (likeCount < targetCommunity) {
            return;
        }

        Community community = Community.builder()
                .pin(pin)
                .communityType(resolveCommunityType(pin.getPinType()))
                .title(pin.getPinTitle())
                .content(pin.getPinContent())
                .build();

        try {
            communityRepository.saveAndFlush(community);
        } catch (DataIntegrityViolationException e) {
            // 동시성 상황에서 이미 생성된 경우 무시
        }
    }

    private CommunityType resolveCommunityType(PinType pinType) {
        return switch (pinType) {
            case ISSUE -> CommunityType.ISSUE;
            case STORE -> CommunityType.STORE;
            case FESTIVAL -> CommunityType.FESTIVAL;
            case COMMUNICATION -> CommunityType.COMMUNICATION;
        };
    }
}