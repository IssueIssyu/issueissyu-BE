package issueissyu.backend.domain.pin.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CommentReqDTO {
    @Schema(description = "댓글 내용", example = "저도 같은 고민 있었어요.")
    @NotBlank
    private String commentContent;
}
