package issueissyu.backend.domain.pin.converter;

import issueissyu.backend.domain.pin.dto.res.CommentResDTO;
import issueissyu.backend.domain.pin.entity.Comment;

public class CommentConverter {
    private CommentConverter() {
    }

    public static CommentResDTO toCommentResDTO(Comment comment, String uid) {
        return CommentResDTO.builder()
                .commentId(comment.getCommentId())
                .uid(comment.getUser().getUid())
                .nickname(comment.getUser().getNickname())
                .profileImageUrl(comment.getUser().getProfileImageUrl())
                .commentContent(comment.getCommentContent())
                .isMine(comment.getUser().getUid().equals(uid))
                .edited(comment.isEdited())
                .createdAt(comment.getCreatedAt())
                .build();
    }
}
