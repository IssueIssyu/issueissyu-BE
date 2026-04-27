package issueissyu.backend.domain.auth.exception;

import issueissyu.backend.global.api.code.BaseErrorCode;
import issueissyu.backend.global.exception.GeneralException;

public class AuthException extends GeneralException {

    public AuthException(BaseErrorCode code) {
        super(code);
    }

    public static AuthException of(BaseErrorCode code) {
        return new AuthException(code);
    }
}
