package issueissyu.backend.domain.pin.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ApplyPinEmojiReqDTO {

    @Schema(description = "적용할 이모지 ID", example = "1")
    @NotNull
    private Long emogjiId;
}
