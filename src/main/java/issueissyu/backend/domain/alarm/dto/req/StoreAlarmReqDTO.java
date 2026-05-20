package issueissyu.backend.domain.alarm.dto.req;

import jakarta.validation.constraints.NotBlank;

public record StoreAlarmReqDTO(
        @NotBlank String storeAlarmTitle,
        @NotBlank String storeAlarmBody) {}
