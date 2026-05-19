package issueissyu.backend.domain.alarm.dto.req;

import jakarta.validation.constraints.NotBlank;

public record EventAlarmReqDTO(
        @NotBlank String eventAlarmTitle,
        @NotBlank String eventAlarmBody) {}
