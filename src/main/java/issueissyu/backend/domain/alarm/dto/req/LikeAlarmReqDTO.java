package issueissyu.backend.domain.alarm.dto.req;

import jakarta.validation.constraints.NotBlank;

public record LikeAlarmReqDTO(
        @NotBlank String likeAlarmTitle,
        @NotBlank String likeAlarmBody) {}
