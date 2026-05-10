package issueissyu.backend.domain.community.service.command;

public interface CommunityCommandService {

    void deleteCommunity(Long communityId, String uid);

    void takedownCommunity(Long communityId, String uid);
}
