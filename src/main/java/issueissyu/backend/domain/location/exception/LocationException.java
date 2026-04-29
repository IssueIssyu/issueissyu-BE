package issueissyu.backend.domain.location.exception;

import issueissyu.backend.global.api.code.BaseErrorCode;
import lombok.Getter;

@Getter
public class LocationException extends RuntimeException {
    private final BaseErrorCode code;

    public LocationException(BaseErrorCode code) {
        super(code.getReason().getMessage());
        this.code = code;
    }

    public LocationException(BaseErrorCode code, String message) {
        super(message);
        this.code = code;
    }

    public static LocationException of(BaseErrorCode code) {
        return new LocationException(code);
    }
}
