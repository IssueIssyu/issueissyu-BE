package issueissyu.backend.domain.pin.service.query;

import issueissyu.backend.domain.pin.dto.res.PinHomeResDTO;
import issueissyu.backend.domain.pin.dto.res.PinPostResDTO;
import issueissyu.backend.domain.pin.exception.code.PinSuccessCode;

public interface PinDetailQueryService {

    PinHomeResult getPinHome(Long pinId, String uid);

    PinPostResult getPinPost(Long pinId, String uid);

    record PinHomeResult(PinSuccessCode successCode, PinHomeResDTO data) {}

    record PinPostResult(PinSuccessCode successCode, PinPostResDTO data) {}
}
