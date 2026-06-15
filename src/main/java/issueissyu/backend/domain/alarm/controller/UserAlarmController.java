package issueissyu.backend.domain.alarm.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import issueissyu.backend.domain.alarm.dto.req.PushTokenReqDTO;
import issueissyu.backend.domain.alarm.dto.res.AlarmConfirmResDTO;
import issueissyu.backend.domain.alarm.dto.res.AlarmListItemResDTO;
import issueissyu.backend.domain.alarm.dto.res.EventAlarmSendResDTO;
import issueissyu.backend.domain.alarm.dto.res.HotAlarmSendResDTO;
import issueissyu.backend.domain.alarm.dto.res.LikeAlarmSendResDTO;
import issueissyu.backend.domain.alarm.dto.res.StoreAlarmSendResDTO;
import issueissyu.backend.domain.alarm.exception.code.AlarmSuccessCode;
import issueissyu.backend.domain.alarm.service.command.UserAlarmCommandService;
import issueissyu.backend.domain.alarm.service.query.UserAlarmQueryService;
import issueissyu.backend.global.api.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Alarm", description = "푸시 알림 API")
@RestController
@RequestMapping("/api/alarms")
@RequiredArgsConstructor
public class UserAlarmController {

    private final UserAlarmCommandService userAlarmCommandService;
    private final UserAlarmQueryService userAlarmQueryService;

    @Operation(
            summary = "알람 단건 조회",
            description = "로그인한 사용자의 알람 단건을 user_alarm_id 기준으로 조회합니다.")
    @GetMapping("/{alarmId}")
    public ApiResponse<AlarmListItemResDTO> getAlarm(
            @AuthenticationPrincipal String uid, @PathVariable Long alarmId) {
        AlarmListItemResDTO result = userAlarmQueryService.getAlarm(uid, alarmId);
        return ApiResponse.onSuccess(AlarmSuccessCode.ALARM_200, result);
    }

    @Operation(
            summary = "알람 확인 처리",
            description = "alarmId에 해당하는 알람의 is_confirmed 값을 true로 전환합니다.")
    @PatchMapping("/{alarmId}/confirm")
    public ApiResponse<AlarmConfirmResDTO> confirmAlarm(
            @AuthenticationPrincipal String uid, @PathVariable Long alarmId) {
        AlarmConfirmResDTO result = userAlarmCommandService.confirmAlarm(uid, alarmId);
        return ApiResponse.onSuccess(AlarmSuccessCode.ALARM_CONFIRM_200, result);
    }

    @Operation(
            summary = "FCM Push Token 저장",
            description = "기기가 발급받은 FCM push_token을 user 테이블에 저장합니다.")
    @PostMapping("/push-token")
    public ApiResponse<Void> savePushToken(
            @AuthenticationPrincipal String uid,
            @Valid @RequestBody PushTokenReqDTO request) {
        userAlarmCommandService.savePushToken(uid, request.fcmPushToken());
        return ApiResponse.onSuccess(AlarmSuccessCode.ALARM_TOKEN_200, null);
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
            description =
                    """
                    동네 인증 지역의 축제(FESTIVAL) 핀에 대한 푸시 알람을 요청자에게 전송합니다.
                    event_start_time 이 현재 시각 ±12시간 이내인 핀만 대상이며, 제목·본문은 서버에서 고정값으로 생성합니다.
                    user.event_alarm_active 가 false 이면 EVENT_ALARM_403 을 반환합니다.
                    EVENT_ALARM_404_1(동네 인증 없음), EVENT_ALARM_404_2(대상 축제 없음), EVENT_ALARM_404_3(커뮤니티 없음).
                    알람 클릭 시 GET /api/communities/{communityId} 로 이동합니다.
                    """)
    @PostMapping("/event")
    public ApiResponse<EventAlarmSendResDTO> sendEventAlarm(@AuthenticationPrincipal String uid) {
        EventAlarmSendResDTO result = userAlarmCommandService.sendEventAlarm(uid);
        return ApiResponse.onSuccess(AlarmSuccessCode.EVENT_ALARM_200, result);
    }

    @Operation(
            summary = "가게 홍보 푸시 알림 전송",
            description =
                    """
                    동네 인증 지역의 가게(STORE) 핀에 대한 푸시 알람을 요청자에게 전송합니다.
                    event_start_time 이 현재 시각 ±12시간 이내인 핀만 대상이며, 제목·본문은 서버에서 고정값으로 생성합니다.
                    user.store_alarm_active 가 false 이면 STORE_ALARM_403 을 반환합니다.
                    알람 클릭 시 GET /api/communities/{communityId} 로 이동합니다.
                    """)
    @PostMapping("/store")
    public ApiResponse<StoreAlarmSendResDTO> sendStoreAlarm(@AuthenticationPrincipal String uid) {
        StoreAlarmSendResDTO result = userAlarmCommandService.sendStoreAlarm(uid);
        return ApiResponse.onSuccess(AlarmSuccessCode.STORE_ALARM_200, result);
    }

    @Operation(
            summary = "인기글 푸시 알림 전송",
            description =
                    """
                    동네 인증 지역에서 popularity 1위 인기글에 대한 푸시 알람을 요청자에게 전송합니다.
                    최근 7일 게시글 중 HOT 피드와 동일한 기준으로 선정하며, 제목·본문은 서버에서 고정값으로 생성합니다.
                    user.hot_alarm_active 가 false 이면 HOT_ALARM_403 을 반환합니다.
                    알람 클릭 시 GET /api/communities/{communityId} 로 이동합니다.
                    매일 오후 6시(Asia/Seoul) 스케줄러로도 동일 기준의 알람이 일괄 발송됩니다.
                    """)
    @PostMapping("/hot")
    public ApiResponse<HotAlarmSendResDTO> sendHotAlarm(@AuthenticationPrincipal String uid) {
        HotAlarmSendResDTO result = userAlarmCommandService.sendHotAlarm(uid);
        return ApiResponse.onSuccess(AlarmSuccessCode.HOT_ALARM_200, result);
    }
}
