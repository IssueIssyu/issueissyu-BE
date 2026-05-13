package issueissyu.backend.domain.collection.exception;

import issueissyu.backend.global.api.code.BaseErrorCode;
import issueissyu.backend.global.exception.GeneralException;

public class CustomCollectionException extends GeneralException {

    private CustomCollectionException(BaseErrorCode code) {
        super(code);
    }

    public static CustomCollectionException of(BaseErrorCode code) {
        return new CustomCollectionException(code);
    }
}
