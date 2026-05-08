package issueissyu.backend.domain.pin.converter;

import issueissyu.backend.domain.pin.dto.res.CommentResDTO;
import issueissyu.backend.domain.pin.entity.Comment;

public class CommentConverter {
    private CommentConverter() {
    }

    public static CommentResDTO toCommentResDTO(Comment comment, String uid, String profileImageUrl) {
        return CommentResDTO.builder()
                .commentId(comment.getCommentId())
                .nickname(comment.getUser().getNickname())
                .profileImageUrl(profileImageUrl)
                .commentContent(comment.getCommentContent())
                .isMine(comment.getUser().getUid().equals(uid))
                .edited(comment.isEdited())
                .createdAt(comment.getCreatedAt())
                .build();
    }
}
