package issueissyu.backend.domain.auth.service;

import issueissyu.backend.domain.auth.exception.AuthException;
import issueissyu.backend.domain.auth.exception.code.AuthErrorCode;
import issueissyu.backend.domain.auth.exception.code.AuthSuccessCode;
import issueissyu.backend.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PhoneVerificationService {

    private final SmsService smsService;
    private final UserRepository userRepository;

    // 인증번호 SMS 발송
    public void sendCode(String phone) {
        smsService.sendVerificationCode(phone);
    }

    // 인증번호 검증 + 전화번호 중복 확인
    // 반환값: PHONE_200(중복 없음), PHONE_201(중복 있고 닉네임 인증 완료)
    // 예외: PHONE_CODE_INVALID(인증번호 불일치), PHONE_400(중복이고 닉네임 미인증)
    @Transactional(readOnly = true)
    public AuthSuccessCode verifyAndCheckDuplicate(String phone, String code, boolean isAvailableNickname) {
        boolean codeValid = smsService.verifyCode(phone, code);
        if (!codeValid) {
            throw AuthException.of(AuthErrorCode.PHONE_CODE_INVALID);
        }

        boolean isDuplicate = userRepository.existsByPhone(phone);

        if (!isDuplicate) {
            return AuthSuccessCode.PHONE_200;
        }

        // 중복 전화번호인 경우 닉네임 인증 여부에 따라 분기
        if (Boolean.TRUE.equals(isAvailableNickname)) {
            return AuthSuccessCode.PHONE_201;
        }

        throw AuthException.of(AuthErrorCode.PHONE_400);
    }
}
