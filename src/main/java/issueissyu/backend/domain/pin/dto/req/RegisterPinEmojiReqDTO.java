package issueissyu.backend.domain.pin.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class RegisterPinEmojiReqDTO {

    @Schema(description = "등록할 이모지 ID. null이면 반응 취소(피커에서 선택 해제 후 버튼)", example = "1", nullable = true)
    private Long emojiId;
}
