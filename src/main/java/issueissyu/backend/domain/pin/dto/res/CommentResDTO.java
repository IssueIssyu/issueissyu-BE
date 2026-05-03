package issueissyu.backend.domain.pin.dto.res;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class CommentResDTO {
    @Schema(description = "댓글 ID", example = "1")
    private Long commentId;

    @Schema(description = "작성자 uid", example = "2f1f3f84-7a83-4e7f-8f09-7f2c1a93f236")
    private String uid;

    @Schema(description = "작성자 닉네임", example = "이슈쟁이")
    private String nickname;

    @Schema(description = "작성자 프로필 이미지 URL", example = "https://cdn.example.com/profile/u1.png")
    private String profileImageUrl;

    @Schema(description = "댓글 내용", example = "저도 같은 고민 있었어요.")
    private String commentContent;

    @Schema(description = "내 댓글 여부", example = "true")
    private boolean isMine;

    @Schema(description = "댓글 생성 시각")
    private LocalDateTime createdAt;
}
