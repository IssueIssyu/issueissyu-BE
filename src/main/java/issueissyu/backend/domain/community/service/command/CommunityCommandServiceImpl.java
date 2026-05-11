package issueissyu.backend.domain.community.service.command;

import issueissyu.backend.domain.community.entity.Community;
import issueissyu.backend.domain.community.enums.CommunityType;
import issueissyu.backend.domain.community.exception.CommunityException;
import issueissyu.backend.domain.community.exception.code.CommunityErrorCode;
import issueissyu.backend.domain.community.repository.CommunityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class CommunityCommandServiceImpl implements CommunityCommandService {

    private final CommunityRepository communityRepository;
    private final CommunicationPinCleaner communicationPinCleaner;

    @Override
    public void deleteCommunity(Long communityId, String uid) {
        Community community = communityRepository.findDetailById(communityId)
                .orElseThrow(() -> CommunityException.of(CommunityErrorCode.COMMUNITY_404_1));

        // 소통 타입만 삭제 허용
        if (community.getCommunityType() != CommunityType.COMMUNICATION) {
            throw CommunityException.of(CommunityErrorCode.COMMUNITY_403_2);
        }
        if (!community.getPin().getUser().getUid().equals(uid)) {
            throw CommunityException.of(CommunityErrorCode.COMMUNITY_403_1);
        }

        communicationPinCleaner.deleteByPinId(community.getPin().getPinId());
    }

    @Override
    public void takedownCommunity(Long communityId, String uid) {
        Community community = communityRepository.findDetailById(communityId)
                .orElseThrow(() -> CommunityException.of(CommunityErrorCode.COMMUNITY_404_1));

        CommunityType type = community.getCommunityType();
        if (type != CommunityType.ISSUE && type != CommunityType.COMMUNICATION) {
            throw CommunityException.of(CommunityErrorCode.COMMUNITY_403_4);
        }
        if (!community.getPin().getUser().getUid().equals(uid)) {
            throw CommunityException.of(CommunityErrorCode.COMMUNITY_403_3);
        }
        communityRepository.delete(community);
    }
}
