package issueissyu.backend.domain.pin.service.command;

import issueissyu.backend.domain.pin.dto.res.PinLikeResDTO;

public interface PinLikeCommandService {

    PinLikeResDTO likePin(Long pinId, String uid);
}
