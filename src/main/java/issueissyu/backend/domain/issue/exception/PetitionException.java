package issueissyu.backend.domain.issue.exception;

import issueissyu.backend.global.api.code.BaseErrorCode;
import issueissyu.backend.global.exception.GeneralException;

public class PetitionException extends GeneralException {

    private PetitionException(BaseErrorCode code) {
        super(code);
    }

    public static PetitionException of(BaseErrorCode code) {
        return new PetitionException(code);
    }
}
