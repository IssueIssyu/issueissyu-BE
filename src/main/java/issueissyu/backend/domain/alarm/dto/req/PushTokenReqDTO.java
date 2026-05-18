package issueissyu.backend.domain.alarm.dto.req;

import jakarta.validation.constraints.NotBlank;

public record PushTokenReqDTO(@NotBlank String fcmPushToken) {}
