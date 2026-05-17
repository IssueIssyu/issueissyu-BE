package issueissyu.backend.domain.user.dto.res;

import java.util.List;

public record UserMyPinsResDTO(List<UserMyPinItemResDTO> pins, UserMyPinPageInfoResDTO pageInfo) {}
