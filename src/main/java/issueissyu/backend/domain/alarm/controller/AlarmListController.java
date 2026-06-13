package issueissyu.backend.domain.alarm.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import issueissyu.backend.domain.alarm.dto.res.AlarmListResDTO;
import issueissyu.backend.domain.alarm.exception.code.AlarmSuccessCode;
import issueissyu.backend.domain.alarm.service.command.UserAlarmCommandService;
import issueissyu.backend.domain.alarm.service.query.UserAlarmQueryService;
import issueissyu.backend.global.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Alarm", description = "푸시 알림 API")
@RestController
@RequestMapping("/api/alarms-list")
@RequiredArgsConstructor
public class AlarmListController {

    private final UserAlarmQueryService userAlarmQueryService;
    private final UserAlarmCommandService userAlarmCommandService;

    @Operation(
            summary = "내 알람 목록 조회",
            description =
                    """
                    로그인한 사용자의 알람 목록을 user_alarm_id 내림차순으로 커서 페이징 조회합니다.
                    size 기본값 10, cursor는 이전 응답의 pageInfo.nextCursor 값을 사용합니다.
                    응답에 포함된 isConfirmed=false 알람은 응답 직후 true로 전환됩니다.
                    """)
    @GetMapping
    public ApiResponse<AlarmListResDTO> getAlarmList(
            @AuthenticationPrincipal String uid,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) String cursor) {
        AlarmListResDTO result = userAlarmQueryService.getAlarmList(uid, size, cursor);
        userAlarmCommandService.confirmUnconfirmedAlarmsInList(uid, result);
        return ApiResponse.onSuccess(AlarmSuccessCode.ALARM_LIST_200, result);
    }
}
