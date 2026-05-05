package issueissyu.backend.domain.map.exception;

import issueissyu.backend.global.api.code.BaseErrorCode;
import issueissyu.backend.global.exception.GeneralException;

public class MapException extends GeneralException {

    public MapException(BaseErrorCode code) {
        super(code);
    }

    public static MapException of(BaseErrorCode code) {
        return new MapException(code);
    }
}
