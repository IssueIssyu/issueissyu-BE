package issueissyu.backend.domain.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import issueissyu.backend.domain.location.dto.res.UserLocationCertResDto;
import issueissyu.backend.domain.location.exception.code.LocationSuccessCode;
import issueissyu.backend.domain.user.dto.req.NicknameChangeReqDTO;
import issueissyu.backend.domain.user.dto.res.UserAlarmToggleResDTO;
import issueissyu.backend.domain.user.dto.res.UserMyPinsResDTO;
import issueissyu.backend.domain.user.enums.UserAlarmType;
import issueissyu.backend.domain.user.exception.code.UserSuccessCode;
import issueissyu.backend.domain.user.service.command.UserCommandService;
import issueissyu.backend.domain.user.service.query.UserPinQueryService;
import issueissyu.backend.global.api.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "User", description = "사용자 API")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserPinQueryService userPinQueryService;
    private final UserCommandService userCommandService;

    @Operation(
            summary = "동네 변경",
            description =
                    "위·경도로 시군구를 재인증합니다. POST /api/location/user/cert 와 동일한 처리이며 성공 시 LOCATION_200_4를 반환합니다. "
                            + "user_point_updated 기준으로 한 달이 지나지 않은 경우 LOCATION_400_2(동네 변경은 한 달에 1회 가능합니다.)를 반환합니다.")
    @PatchMapping("/me/region")
    public ApiResponse<UserLocationCertResDto> changeUserRegion(
            @AuthenticationPrincipal String uid,
            @RequestParam("lat") double lat,
            @RequestParam("lng") double lng) {
        UserLocationCertResDto result = userCommandService.changeUserRegion(uid, lat, lng);
        return ApiResponse.onSuccess(LocationSuccessCode.LOCATION_USER_CERT_SUCCESS, result);
    }

    @Operation(
            summary = "알람 설정 토글",
            description = "alarmType별 알람 활성 여부를 true ↔ false 로 전환합니다. (LIKE, EVENT, HOT, STORE)")
    @PatchMapping("/me/alarms/{alarmType}")
    public ApiResponse<UserAlarmToggleResDTO> toggleUserAlarm(
            @AuthenticationPrincipal String uid, @PathVariable("alarmType") String alarmType) {
        var outcome = userCommandService.toggleUserAlarm(uid, UserAlarmType.fromToken(alarmType));
        return ApiResponse.onSuccess(outcome.successCode(), outcome.result());
    }

    @Operation(summary = "닉네임 변경", description = "현재 로그인한 사용자의 닉네임을 변경합니다. 요청 본문에 nickname을 전달합니다.")
    @PatchMapping("/me/nickname")
    public ApiResponse<Void> changeNickname(
            @AuthenticationPrincipal String uid, @Valid @RequestBody NicknameChangeReqDTO request) {
        userCommandService.changeNickname(uid, request.getNickname());
        return ApiResponse.onSuccess(UserSuccessCode.USER_NICKNAME_200, null);
    }

    @Operation(
            summary = "내 핀 조회",
            description = "인증된 사용자가 생성한 핀을 created_at 내림차순으로 커서 페이징 조회합니다.")
    @GetMapping("/me/pins")
    public ApiResponse<UserMyPinsResDTO> getMyPins(
            @AuthenticationPrincipal String uid,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) String cursor) {
        return ApiResponse.onSuccess(UserSuccessCode.USER_PIN_200, userPinQueryService.getMyPins(uid, size, cursor));
    }
}
