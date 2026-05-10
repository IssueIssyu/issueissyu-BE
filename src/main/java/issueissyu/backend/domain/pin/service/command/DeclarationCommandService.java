package issueissyu.backend.domain.pin.service.command;

public interface DeclarationCommandService {

    void declareCommunity(Long communityId, String uid, int reasonIndex);
}
