package issueissyu.backend.domain.issue.exception;

import issueissyu.backend.global.api.code.BaseErrorCode;
import issueissyu.backend.global.exception.GeneralException;

public class ProblemSolverException extends GeneralException {

    private ProblemSolverException(BaseErrorCode code) {
        super(code);
    }

    public static ProblemSolverException of(BaseErrorCode code) {
        return new ProblemSolverException(code);
    }
}
