package issueissyu.backend.domain.pin.exception;

import issueissyu.backend.global.api.code.BaseErrorCode;
import issueissyu.backend.global.exception.GeneralException;

public class PinException extends GeneralException {

    public PinException(BaseErrorCode code) {
        super(code);
    }

    public static PinException of(BaseErrorCode code) {
        return new PinException(code);
    }
}
