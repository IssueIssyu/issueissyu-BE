package issueissyu.backend.domain.community.service.command;

import issueissyu.backend.domain.community.entity.Community;
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

    @Override
    public void deleteCommunity(Long communityId, String uid) {
        Community community = communityRepository.findDetailById(communityId)
                .orElseThrow(() -> CommunityException.of(CommunityErrorCode.COMMUNITY_404_1));
        if (!community.getPin().getUser().getUid().equals(uid)) {
            throw CommunityException.of(CommunityErrorCode.COMMUNITY_403_1);
        }
        communityRepository.delete(community);
    }
}
