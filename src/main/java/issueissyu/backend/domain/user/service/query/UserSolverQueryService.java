package issueissyu.backend.domain.user.service.query;

import issueissyu.backend.domain.user.dto.res.UserMySolversResDTO;

public interface UserSolverQueryService {

    UserMySolversResDTO getMySolvers(String uid, Integer size, String cursor);
}
