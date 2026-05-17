package issueissyu.backend.domain.user.service.query;

import issueissyu.backend.domain.user.dto.res.UserMyPinsResDTO;

public interface UserPinQueryService {

    UserMyPinsResDTO getMyPins(String uid, Integer size, String cursor);
}
