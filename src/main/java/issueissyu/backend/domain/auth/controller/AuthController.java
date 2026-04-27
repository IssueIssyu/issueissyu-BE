package issueissyu.backend.domain.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import issueissyu.backend.domain.auth.dto.req.KakaoAppLoginReqDTO;
import issueissyu.backend.domain.auth.dto.req.LoginLinkReqDTO;
import issueissyu.backend.domain.auth.dto.req.NaverAppLoginReqDTO;
import issueissyu.backend.domain.auth.dto.req.PhoneSendReqDTO;
import issueissyu.backend.domain.auth.dto.req.PhoneVerifyReqDTO;
import issueissyu.backend.domain.auth.dto.res.KakaoAppLoginResDTO;
import issueissyu.backend.domain.auth.dto.res.LoginLinkResDTO;
import issueissyu.backend.domain.auth.dto.res.NaverAppLoginResDTO;
import issueissyu.backend.domain.auth.dto.res.NicknameCheckResDTO;
import issueissyu.backend.domain.auth.service.KakaoAppLoginService;
import issueissyu.backend.domain.auth.service.LoginLinkService;
import issueissyu.backend.domain.auth.service.NaverAppLoginService;
import issueissyu.backend.domain.auth.service.PhoneVerificationService;
import issueissyu.backend.domain.auth.dto.req.TokenReissueReqDTO;
import issueissyu.backend.domain.auth.dto.res.TokenPairDTO;
import issueissyu.backend.domain.auth.exception.code.AuthErrorCode;
import issueissyu.backend.domain.auth.exception.code.AuthSuccessCode;
import issueissyu.backend.domain.auth.service.AuthService;
import issueissyu.backend.domain.user.dto.req.TermReqDTO;
import issueissyu.backend.domain.user.dto.res.TermResDTO;
import issueissyu.backend.domain.user.service.command.UserCommandService;
import issueissyu.backend.global.api.ApiResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;

@Tag(name = "Auth", description = "인증·소셜 로그인")
@RestController
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final NaverAppLoginService naverAppLoginService;
    private final KakaoAppLoginService kakaoAppLoginService;
    private final UserCommandService userCommandService;
    private final PhoneVerificationService phoneVerificationService;
    private final LoginLinkService loginLinkService;

    // 개발용 Dev Naver 콜백 확인 페이지
    // http://localhost:8080/dev/oauth2/authorization/naver 로그인 후 여기로 리다이렉트됨
    @Operation(hidden = true)
    @GetMapping(value = "/dev/login/callback", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> devLoginCallback(
            @RequestParam(required = false) Boolean devIsNew,
            HttpServletRequest request) {

        Cookie[] cookies = request.getCookies() != null ? request.getCookies() : new Cookie[0];
        String accessToken = Arrays.stream(cookies)
                .filter(c -> "accessToken".equals(c.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElse("(쿠키에서 찾을 수 없음)");
        String refreshToken = Arrays.stream(cookies)
                .filter(c -> "refreshToken".equals(c.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElse("(쿠키에서 찾을 수 없음)");

        String html = """
                <!DOCTYPE html>
                <html lang="ko">
                <head>
                  <meta charset="UTF-8">
                  <title>로그인 성공</title>
                  <style>
                    body { font-family: 'Segoe UI', sans-serif; background: #f0f4f8; display: flex;
                           justify-content: center; align-items: center; min-height: 100vh; margin: 0; }
                    .card { background: white; border-radius: 12px; padding: 36px 40px;
                            box-shadow: 0 4px 20px rgba(0,0,0,0.1); max-width: 700px; width: 100%%; }
                    h1 { color: #1a73e8; margin-bottom: 8px; }
                    .badge { display: inline-block; padding: 3px 10px; border-radius: 20px;
                             font-size: 13px; font-weight: 600; margin-bottom: 24px; }
                    .badge.new  { background: #e8f5e9; color: #2e7d32; }
                    .badge.old  { background: #e3f2fd; color: #1565c0; }
                    label { display: block; font-size: 13px; font-weight: 600;
                            color: #666; margin-bottom: 4px; }
                    pre { background: #f8f9fa; border: 1px solid #e0e0e0; border-radius: 8px;
                          padding: 12px 16px; font-size: 13px; word-break: break-all;
                          white-space: pre-wrap; margin-bottom: 20px; color: #212121; }
                    .copy-btn { font-size: 12px; padding: 4px 12px; background: #1a73e8;
                                color: white; border: none; border-radius: 6px; cursor: pointer;
                                margin-top: -14px; margin-bottom: 16px; display: block; }
                    .copy-btn:hover { background: #1558b0; }
                    .note { font-size: 12px; color: #999; margin-top: 8px; }
                  </style>
                </head>
                <body>
                  <div class="card">
                    <h1>✅ DEV 네이버 로그인 성공</h1>
                    <span class="badge %s">%s</span>

                    <label>Access Token</label>
                    <pre id="at">%s</pre>
                    <button class="copy-btn" onclick="navigator.clipboard.writeText(document.getElementById('at').innerText)">복사</button>

                    <label>Refresh Token <span style="font-weight:400;color:#aaa">(HttpOnly 쿠키)</span></label>
                    <pre id="rt">%s</pre>
                    <button class="copy-btn" onclick="navigator.clipboard.writeText(document.getElementById('rt').innerText)">복사</button>

                    <p class="note">⚠️ 이 페이지는 개발 테스트용입니다. 프론트엔드 연결 후 redirect-uri를 변경하세요.</p>
                  </div>
                </body>
                </html>
                """.formatted(
                Boolean.TRUE.equals(devIsNew) ? "new" : "old",
                Boolean.TRUE.equals(devIsNew) ? "🆕 신규 가입" : "🔄 기존 회원",
                accessToken != null ? accessToken : "(토큰 없음)",
                refreshToken
        );

        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_HTML)
                .body(html);
    }

    @Operation(summary = "네이버 앱 로그인")
    @PostMapping("/auth/login/naver")
    public ApiResponse<NaverAppLoginResDTO> naverAppLogin(
            @Valid @RequestBody NaverAppLoginReqDTO request) {

        NaverAppLoginResDTO result = naverAppLoginService.login(request);
        AuthSuccessCode successCode = result.isNew()
                ? AuthSuccessCode.NAVER_LOGIN_200_1
                : AuthSuccessCode.NAVER_LOGIN_200_2;
        return ApiResponse.onSuccess(successCode, result);
    }

    @Operation(summary = "카카오 앱 로그인")
    @PostMapping("/auth/login/kakao")
    public ApiResponse<KakaoAppLoginResDTO> kakaoAppLogin(
            @Valid @RequestBody KakaoAppLoginReqDTO request) {

        KakaoAppLoginResDTO result = kakaoAppLoginService.login(request);
        AuthSuccessCode successCode = result.isNew()
                ? AuthSuccessCode.KAKAO_LOGIN_200_1
                : AuthSuccessCode.KAKAO_LOGIN_200_2;
        return ApiResponse.onSuccess(successCode, result);
    }

    @Operation(summary = "토큰 재발급", description = "refresh token으로 access·refresh 토큰을 재발급합니다.")
    @PostMapping("/auth/refresh")
    public ApiResponse<TokenPairDTO> refresh(@Valid @RequestBody TokenReissueReqDTO request) {
        return ApiResponse.onSuccess(AuthSuccessCode.REFRESH_200, authService.reissue(request));
    }

    @Operation(summary = "닉네임 중복 확인", description = "입력한 닉네임의 형식 및 중복 여부를 확인합니다.")
    @GetMapping("/auth/{nickname}/check")
    public ApiResponse<NicknameCheckResDTO> checkNickname(
            @PathVariable String nickname
    ) {
        NicknameCheckResDTO unavailable = NicknameCheckResDTO.builder()
                .isAvailableNickname(false)
                .build();

        if (!authService.isValidNicknameFormat(nickname)) {
            return ApiResponse.onFailure(AuthErrorCode.NICKNAME_400, unavailable);
        }

        if (authService.isNicknameDuplicated(nickname)) {
            return ApiResponse.onFailure(AuthErrorCode.NICKNAME_409, unavailable);
        }

        NicknameCheckResDTO available = NicknameCheckResDTO.builder()
                .isAvailableNickname(true)
                .nickname(nickname)
                .build();

        return ApiResponse.onSuccess(AuthSuccessCode.NICKNAME_200, available);
    }

    @Operation(summary = "회원탈퇴", description = "인증된 사용자의 Redis 토큰·OAuth·User 레코드를 모두 삭제합니다.")
    @DeleteMapping("/auth/signout")
    public ApiResponse<Void> signout(@AuthenticationPrincipal String uid) {
        authService.signout(uid);
        return ApiResponse.onSuccess(AuthSuccessCode.SIGNOUT_200, null);
    }

    @Operation(summary = "로그아웃", description = "인증된 사용자의 Redis refresh token을 삭제합니다.")
    @PostMapping("/auth/logout")
    public ApiResponse<Void> logout(@AuthenticationPrincipal String uid) {
        authService.logout(uid);
        return ApiResponse.onSuccess(AuthSuccessCode.LOGOUT_200, null);
    }

    @Operation(summary = "약관 동의",
            description = "SERVICE, PRIVACY(필수), LOCATION, MARKETING(선택) 약관 동의 정보를 저장합니다.")
    @PostMapping("/auth/term")
    public ApiResponse<TermResDTO> agreeTerm(@AuthenticationPrincipal String uid,
                                             @Valid @RequestBody TermReqDTO request) {
        TermResDTO result = userCommandService.agreeTerms(uid, request);
        return ApiResponse.onSuccess(AuthSuccessCode.TERM_200, result);
    }

    @Operation(summary = "전화번호 인증번호 전송",
            description = "입력한 전화번호로 6자리 SMS 인증번호를 전송합니다.")
    @PostMapping("/auth/phone/send")
    public ApiResponse<Void> sendPhoneCode(@AuthenticationPrincipal String uid,
                                           @Valid @RequestBody PhoneSendReqDTO request) {
        phoneVerificationService.sendCode(request.getPhone());
        return ApiResponse.onSuccess(AuthSuccessCode.PHONE_SEND_200, null);
    }

    @Operation(summary = "전화번호 인증",
            description = """
                    SMS 인증번호를 검증하고 전화번호 중복 여부를 확인합니다.
                    - 중복 없음 → PHONE_200 (전화번호 인증 성공)
                    - 중복 있음 + 닉네임 인증 완료(is_available_nickname=true) → PHONE_201 (로그인 연동 단계)
                    - 중복 있음 + 닉네임 미인증(is_available_nickname=false) → 400 에러
                    """)
    @PostMapping("/auth/phone")
    public ApiResponse<Void> verifyPhone(@AuthenticationPrincipal String uid,
                                         @Valid @RequestBody PhoneVerifyReqDTO request) {
        boolean isAvailableNickname = Boolean.TRUE.equals(request.getIsAvailableNickname());
        AuthSuccessCode result = phoneVerificationService.verifyAndCheckDuplicate(
                request.getPhone(), request.getCode(), isAvailableNickname);
        return ApiResponse.onSuccess(result, null);
    }

    @Operation(summary = "로그인 연동",
            description = """
                    이번 로그인 시도(소셜 타입)를 기존 계정과 연동합니다.
                    임시 uid 사용자를 완전 제거하고, 기존 계정에 새 소셜 타입을 추가합니다.
                    Redis의 refresh token도 기존 uid로 갱신됩니다.
                    """)
    @PostMapping("/auth/login/link")
    public ApiResponse<LoginLinkResDTO> loginLink(@AuthenticationPrincipal String uid,
                                                  @Valid @RequestBody LoginLinkReqDTO request) {
        LoginLinkResDTO result = loginLinkService.link(uid, request.getSocialType(), request.getPhone());
        return ApiResponse.onSuccess(AuthSuccessCode.LOGIN_LINK_200, result);
    }
}
