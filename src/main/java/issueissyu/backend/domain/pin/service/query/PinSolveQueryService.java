package issueissyu.backend.domain.pin.service.query;

import issueissyu.backend.domain.pin.dto.res.PinSolveResDTO;

public interface PinSolveQueryService {

    PinSolveResDTO getPinSolve(Long pinId, String uid);
}
