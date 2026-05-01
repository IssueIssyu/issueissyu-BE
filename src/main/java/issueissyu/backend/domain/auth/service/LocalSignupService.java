package issueissyu.backend.domain.auth.service;

import issueissyu.backend.domain.auth.dto.req.LocalSignupReqDTO;
import issueissyu.backend.domain.auth.dto.res.LocalSignupResDTO;
import issueissyu.backend.domain.auth.exception.AuthException;
import issueissyu.backend.domain.auth.exception.code.AuthErrorCode;
import issueissyu.backend.domain.user.entity.OAuth;
import issueissyu.backend.domain.user.entity.User;
import issueissyu.backend.domain.user.enums.SocialType;
import issueissyu.backend.domain.user.repository.OAuthRepository;
import issueissyu.backend.domain.user.repository.UserRepository;
import issueissyu.backend.domain.user.util.AppUuid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class LocalSignupService {

    // 이메일 형식 검증
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    // 비밀번호: 8~20자, 영문·숫자·특수문자 각 1개 이상
    private static final Pattern PASSWORD_PATTERN =
            Pattern.compile("^(?=.*[A-Za-z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?]).{8,20}$");

    private final UserRepository userRepository;
    private final OAuthRepository oAuthRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public LocalSignupResDTO signup(LocalSignupReqDTO req) {
        String email = req.getEmail().trim().toLowerCase();
        String password = req.getPassword();

        // 이메일·비밀번호 형식 검증
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw AuthException.of(AuthErrorCode.LOCAL_SIGNUP_400_1);
        }
        if (!PASSWORD_PATTERN.matcher(password).matches()) {
            throw AuthException.of(AuthErrorCode.LOCAL_SIGNUP_400_1);
        }

        // 이메일 중복 확인 (providerId = email, socialType = LOCAL)
        if (oAuthRepository.existsByProviderIdAndSocialType(email, SocialType.LOCAL)) {
            throw AuthException.of(AuthErrorCode.LOCAL_SIGNUP_409_1);
        }

        // 사용자 생성 (userName 초기값 고정: localdefault)
        String uid = AppUuid.newUid();
        String userName = "localdefault";

        User user = userRepository.save(User.builder()
                .uid(uid)
                .userName(userName)
                .build());

        // 로컬 OAuth 레코드 생성 (providerId = email, password = BCrypt 해시)
        oAuthRepository.save(OAuth.builder()
                .user(user)
                .providerId(email)
                .socialType(SocialType.LOCAL)
                .password(passwordEncoder.encode(password))
                .build());

        log.debug("로컬 회원가입 완료: uid={}, email={}", uid, email);
        return LocalSignupResDTO.builder().email(email).build();
    }
}
