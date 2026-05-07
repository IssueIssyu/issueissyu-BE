package issueissyu.backend.domain.issue.service.query;

import issueissyu.backend.domain.issue.dto.res.ProblemSolverListResDTO;
import issueissyu.backend.domain.issue.exception.code.IssueSuccessCode;

public record ProblemSolverListQueryEnvelope(
    IssueSuccessCode successCode, 
    ProblemSolverListResDTO body
    ) {}
