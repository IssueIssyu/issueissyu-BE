package issueissyu.backend.domain.pin.service.query;

import issueissyu.backend.domain.pin.dto.res.CommentResDTO;

import java.util.List;

public interface CommentQueryService {
    List<CommentResDTO> getComments(Long pinId, String uid);
}
