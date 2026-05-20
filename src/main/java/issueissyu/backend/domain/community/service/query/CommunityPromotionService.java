package issueissyu.backend.domain.community.service.query;

import issueissyu.backend.domain.community.entity.Community;
import issueissyu.backend.domain.community.enums.CommunityType;
import issueissyu.backend.domain.community.repository.CommunityRepository;
import issueissyu.backend.domain.location.entity.PinLocation;
import issueissyu.backend.domain.location.entity.PopulationDensity;
import issueissyu.backend.domain.location.repository.PinLocationRepository;
import issueissyu.backend.domain.location.repository.PopulationDensityRepository;
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

    private static final int DEFAULT_TARGET_COMMUNITY = 10;

    private final CommunityRepository communityRepository;
    private final PinLocationRepository pinLocationRepository;
    private final PopulationDensityRepository populationDensityRepository;

    @Transactional
    public void promoteIfTargetReached(Pin pin, int likeCount) {
        Long pinId = pin.getPinId();

        // 이미 커뮤니티로 등업된 핀이면 중복 생성 방지
        if (communityRepository.existsByPin_PinId(pinId)) {
            return;
        }

        PinLocation pinLocation = pinLocationRepository.findFirstByPin_PinId(pinId)
                .orElse(null);

        // 위치 정보가 없으면 지역별 등업 기준을 판단할 수 없으므로 등업하지 않음
        if (pinLocation == null) {
            return;
        }

        int targetCommunity = populationDensityRepository
                .findByLocation_LocationId(pinLocation.getLocation().getLocationId())
                .map(PopulationDensity::getTargetCommunity)
                .orElse(DEFAULT_TARGET_COMMUNITY);

        // 아직 커뮤니티 등업 기준에 도달하지 못한 경우
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