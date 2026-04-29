package issueissyu.backend.domain.location.exception;

import issueissyu.backend.domain.location.exception.code.LocationErrorCode;

public class LocationNotFoundException extends LocationException {

    public LocationNotFoundException(String message) {
        super(LocationErrorCode.LOCATION_ADDRESS_NOT_FOUND, message);
    }
}
