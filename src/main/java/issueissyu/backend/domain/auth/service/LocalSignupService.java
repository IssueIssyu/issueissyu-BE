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

@Slf4j
@Service
@RequiredArgsConstructor
public class LocalSignupService {

    private final UserRepository userRepository;
    private final OAuthRepository oAuthRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public LocalSignupResDTO signup(LocalSignupReqDTO req) {
        String email = req.getEmail().trim().toLowerCase();
        String password = req.getPassword();

        // 이메일 중복 확인 (providerId = email, socialType = LOCAL)
        if (oAuthRepository.existsByProviderIdAndSocialType(email, SocialType.LOCAL)) {
            throw AuthException.of(AuthErrorCode.LOCAL_SIGNUP_409_1);
        }

        // 사용자 생성 (userName 초기값 고정: localDefault)
        String uid = AppUuid.newUid();
        String userName = "localDefault";

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
