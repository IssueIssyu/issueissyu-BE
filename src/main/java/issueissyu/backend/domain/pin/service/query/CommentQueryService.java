package issueissyu.backend.domain.pin.service.query;

import issueissyu.backend.domain.pin.dto.res.CommentResDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CommentQueryService {
    Page<CommentResDTO> getComments(Long pinId, String uid, Pageable pageable);
}
