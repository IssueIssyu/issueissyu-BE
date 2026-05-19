package issueissyu.backend.domain.alarm.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import issueissyu.backend.domain.alarm.dto.req.EventAlarmReqDTO;
import issueissyu.backend.domain.alarm.dto.req.PushTokenReqDTO;
import issueissyu.backend.domain.alarm.dto.req.StoreAlarmReqDTO;
import issueissyu.backend.domain.alarm.dto.res.AlarmMessageResDTO;
import issueissyu.backend.domain.alarm.dto.res.LikeAlarmSendResDTO;
import issueissyu.backend.domain.alarm.exception.code.AlarmSuccessCode;
import issueissyu.backend.domain.alarm.service.command.UserAlarmCommandService;
import issueissyu.backend.global.api.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Alarm", description = "푸시 알림 API")
@RestController
@RequestMapping("/api/alarms")
@RequiredArgsConstructor
public class UserAlarmController {

    private final UserAlarmCommandService userAlarmCommandService;

    @Operation(
            summary = "FCM Push Token 저장",
            description = "기기가 발급받은 FCM push_token을 user 테이블에 저장합니다.")
    @PostMapping("/push-token")
    public ApiResponse<Void> savePushToken(
            @AuthenticationPrincipal String uid,
            @Valid @RequestBody PushTokenReqDTO request) {
        userAlarmCommandService.savePushToken(uid, request.fcmPushToken());
        return ApiResponse.onSuccess(AlarmSuccessCode.PUSH_TOKEN_200, null);
    }

    @Operation(
            summary = "좋아요 푸시 알림 전송",
            description =
                    """
                    내 핀 좋아요에 대한 푸시 알람을 전송합니다.
                    요청자는 해당 pinId에 좋아요를 누른 사용자여야 하며, 제목·본문은 서버에서 고정값으로 생성합니다.
                    user.like_alarm_active 가 false 이면 LIKE_ALARM_403 을 반환합니다.
                    알람 클릭 시 GET /api/pins/{pinId}/home 으로 이동합니다.
                    """)
    @PostMapping("/like/{pinId}")
    public ApiResponse<LikeAlarmSendResDTO> sendLikeAlarm(
            @AuthenticationPrincipal String uid, @PathVariable Long pinId) {
        LikeAlarmSendResDTO result = userAlarmCommandService.sendLikeAlarm(uid, pinId);
        return ApiResponse.onSuccess(AlarmSuccessCode.LIKE_ALARM_200, result);
    }

    @Operation(
            summary = "이벤트(축제) 푸시 알림 전송",
            description = "event_alarm_id 에 해당하는 알람을 FCM으로 전송합니다. "
                    + "user.event_alarm_active 가 false 이면 EVENT_ALARM_403 을 반환합니다.")
    @PostMapping("/event")
    public ApiResponse<AlarmMessageResDTO> sendEventAlarm(
            @AuthenticationPrincipal String uid,
            @RequestParam Long eventAlarmId,
            @Valid @RequestBody EventAlarmReqDTO request) {
        AlarmMessageResDTO result = userAlarmCommandService.sendEventAlarm(eventAlarmId, request);
        return ApiResponse.onSuccess(AlarmSuccessCode.EVENT_ALARM_200, result);
    }

    @Operation(
            summary = "가게 홍보 푸시 알림 전송",
            description = "store_alarm_id 에 해당하는 알람을 FCM으로 전송합니다. "
                    + "user.store_alarm_active 가 false 이면 STORE_ALARM_403 을 반환합니다.")
    @PostMapping("/store")
    public ApiResponse<AlarmMessageResDTO> sendStoreAlarm(
            @AuthenticationPrincipal String uid,
            @RequestParam Long storeAlarmId,
            @Valid @RequestBody StoreAlarmReqDTO request) {
        AlarmMessageResDTO result = userAlarmCommandService.sendStoreAlarm(storeAlarmId, request);
        return ApiResponse.onSuccess(AlarmSuccessCode.STORE_ALARM_200, result);
    }
}
