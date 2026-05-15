package issueissyu.backend.domain.user.exception;

import issueissyu.backend.global.api.code.BaseErrorCode;
import issueissyu.backend.global.exception.GeneralException;

public class UserException extends GeneralException {

    public UserException(BaseErrorCode code) {
        super(code);
    }

    public static UserException of(BaseErrorCode code) {
        return new UserException(code);
    }
}
