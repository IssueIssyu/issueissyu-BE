package issueissyu.backend.domain.user.dto.res;

import java.util.List;

public record UserMySolversResDTO(List<UserMySolverItemResDTO> pins, UserMyPinPageInfoResDTO pageInfo) {}
