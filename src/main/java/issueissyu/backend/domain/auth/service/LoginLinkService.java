package issueissyu.backend.domain.auth.service;

import issueissyu.backend.domain.auth.dto.res.LoginLinkResDTO;
import issueissyu.backend.domain.auth.exception.AuthException;
import issueissyu.backend.domain.auth.exception.code.AuthErrorCode;
import issueissyu.backend.domain.user.entity.OAuth;
import issueissyu.backend.domain.user.entity.User;
import issueissyu.backend.domain.user.enums.SocialType;
import issueissyu.backend.domain.user.repository.OAuthRepository;
import issueissyu.backend.domain.user.repository.UserRepository;
import issueissyu.backend.domain.user.repository.UserTermRepository;
import issueissyu.backend.global.redis.RefreshTokenRedisStore;
import issueissyu.backend.global.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoginLinkService {

    private final UserRepository userRepository;
    private final OAuthRepository oAuthRepository;
    private final UserTermRepository userTermRepository;
    private final RefreshTokenRedisStore refreshTokenRedisStore;
    private final JwtTokenProvider jwtTokenProvider;

    // 로그인 연동 처리
    // - tempUid: 이번 로그인에서 새로 발급된 임시 uid (예: 78901)
    // - socialType: 연동하려는 소셜 타입 (예: NAVER, LOCAL)
    // - phone: 이미 DB에 존재하는 전화번호

    // 처리 순서:
    // 1. 전화번호로 기존 사용자(existingUser) 조회
    // 2. LOCAL이면 임시 유저의 providerId(로그인 아이디)·password를 삭제 전에 보존
    // 3. tempUid 사용자의 OAuth 레코드 삭제 + User 레코드 삭제
    // 4. existingUser에 새 socialType OAuth 추가 (이미 있으면 스킵)
    // 5. Redis에서 tempUid:socialType 토큰 삭제 후 existingUid:socialType 토큰 신규 발급
    // 6. existingUser 정보 반환

    @Transactional
    public LoginLinkResDTO link(String tempUid, SocialType socialType, String phone) {
        // 1. 전화번호로 기존 계정 조회
        User existingUser = userRepository.findByPhone(phone)
                .orElseThrow(() -> AuthException.of(AuthErrorCode.LOGIN_LINK_400_1));

        String existingUid = existingUser.getUid();

        // 2. 현재 토큰의 uid와 기존 계정이 동일하면 연동 불필요
        if (existingUid.equals(tempUid)) {
            throw AuthException.of(AuthErrorCode.LOGIN_LINK_400_2);
        }

        // 3. 이미 해당 소셜 타입이 기존 계정에 연동되어 있으면 중복 연동 방지
        if (oAuthRepository.existsByUser_UidAndSocialType(existingUid, socialType)) {
            throw AuthException.of(AuthErrorCode.LOGIN_LINK_400_3);
        }

        // 4. LOCAL 연동: 삭제 전에 로그인 아이디(providerId)와 BCrypt 해시 비밀번호를 보존
        String oauthProviderId = tempUid;
        String oauthPassword   = null;
        if (socialType == SocialType.LOCAL) {
            Optional<OAuth> tempLocalOAuth =
                    oAuthRepository.findByUser_UidAndSocialType(tempUid, SocialType.LOCAL);
            if (tempLocalOAuth.isPresent()) {
                oauthProviderId = tempLocalOAuth.get().getProviderId();
                oauthPassword   = tempLocalOAuth.get().getPassword();
            }
        }

        // 5. tempUid 사용자 완전 제거 (OAuth → UserTerm → User 순서)
        oAuthRepository.deleteByUserUid(tempUid);
        userTermRepository.deleteByUserUid(tempUid);
        userRepository.deleteById(tempUid);

        // 6. existingUser에 새 socialType OAuth 추가
        oAuthRepository.save(OAuth.builder()
                .user(existingUser)
                .providerId(oauthProviderId)
                .socialType(socialType)
                .password(oauthPassword)
                .build());

        // Redis 토큰 갱신: tempUid 키 삭제 후 existingUid 키로 새 토큰 발급
        String provider = socialType.name().toLowerCase();
        refreshTokenRedisStore.delete(tempUid, provider);

        String newRefreshToken = jwtTokenProvider.createRefreshToken(existingUid, provider);
        refreshTokenRedisStore.save(existingUid, provider, newRefreshToken,
                Duration.ofMillis(jwtTokenProvider.getRefreshExpMs()));

        log.info("로그인 연동 완료: tempUid={} → existingUid={}, socialType={}", tempUid, existingUid, socialType);

        return LoginLinkResDTO.builder()
                .uuid(existingUid)
                .socialType(socialType.name())
                .nickname(existingUser.getNickname())
                .email(existingUser.getEmail())
                .phone(existingUser.getPhone())
                .eventAlarmActive(existingUser.isEventAlarmActive())
                .likeAlarmActive(existingUser.isLikeAlarmActive())
                .hotAlarmActive(existingUser.isHotAlarmActive())
                .storeAlarmActive(existingUser.isStoreAlarmActive())
                .isAvailableNickname(true)
                .userPhoneVerified(true)
                .build();
    }
}
