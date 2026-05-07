package issueissyu.backend.domain.issue.service.query;

public interface ProblemSolverQueryService {

    ProblemSolverListQueryEnvelope findProblemSolverList(Long pinId, String pathUserUid, String authUid);
}
