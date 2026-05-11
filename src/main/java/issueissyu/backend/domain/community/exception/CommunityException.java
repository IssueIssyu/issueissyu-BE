package issueissyu.backend.domain.community.exception;

import issueissyu.backend.global.api.code.BaseErrorCode;
import issueissyu.backend.global.exception.GeneralException;

public class CommunityException extends GeneralException {

    public CommunityException(BaseErrorCode code) {
        super(code);
    }

    public static CommunityException of(BaseErrorCode code) {
        return new CommunityException(code);
    }
}
