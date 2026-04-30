package issueissyu.backend.domain.location.exception;

import issueissyu.backend.global.api.code.BaseErrorCode;

public class LocationNotFoundException extends LocationException {

    public LocationNotFoundException(BaseErrorCode code) {
        super(code);
    }

    public static LocationNotFoundException of(BaseErrorCode code) {
        return new LocationNotFoundException(code);
    }
}
