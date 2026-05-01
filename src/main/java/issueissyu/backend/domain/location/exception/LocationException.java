package issueissyu.backend.domain.location.exception;

import issueissyu.backend.global.api.code.BaseErrorCode;
import issueissyu.backend.global.exception.GeneralException;

public class LocationException extends GeneralException {

    public LocationException(BaseErrorCode code) {
        super(code);
    }

    public static LocationException of(BaseErrorCode code) {
        return new LocationException(code);
    }
}
