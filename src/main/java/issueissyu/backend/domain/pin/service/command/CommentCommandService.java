package issueissyu.backend.domain.pin.service.command;

import issueissyu.backend.domain.pin.dto.req.CommentReqDTO;
import issueissyu.backend.domain.pin.dto.res.CommentResDTO;

public interface CommentCommandService {
    CommentResDTO createComment(Long pinId, String uid, CommentReqDTO request);

    CommentResDTO updateComment(Long commentId, String uid, CommentReqDTO request);

    void deleteComment(Long commentId, String uid);
}
