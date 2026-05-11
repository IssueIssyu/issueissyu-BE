package issueissyu.backend.domain.community.exception;

import issueissyu.backend.domain.community.exception.code.CommunityErrorCode;

public class CommunityNotFoundException extends CommunityException {

    public CommunityNotFoundException() {
        super(CommunityErrorCode.COMMUNITY_404_1);
    }
}
