package issueissyu.backend.domain.user.exception;

import issueissyu.backend.global.api.code.GeneralErrorCode;

public class UserNotFoundException extends UserException {

    public UserNotFoundException() {
        super(GeneralErrorCode.USER_NOT_FOUND);
    }
}
