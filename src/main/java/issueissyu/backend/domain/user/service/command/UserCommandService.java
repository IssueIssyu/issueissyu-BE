package issueissyu.backend.domain.user.service.command;

import issueissyu.backend.domain.user.dto.req.TermReqDTO;
import issueissyu.backend.domain.user.dto.res.TermResDTO;

public interface UserCommandService {

    TermResDTO agreeTerms(String uid, TermReqDTO request);
}
