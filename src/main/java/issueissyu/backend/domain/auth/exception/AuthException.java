package issueissyu.backend.domain.auth.exception;

import issueissyu.backend.domain.auth.exception.code.AuthErrorCode;
import issueissyu.backend.global.exception.GeneralException;

public class AuthException extends GeneralException {

    public AuthException(AuthErrorCode code) {
        super(code);
    }

    public static AuthException of(AuthErrorCode code) {
        return new AuthException(code);
    }
}
