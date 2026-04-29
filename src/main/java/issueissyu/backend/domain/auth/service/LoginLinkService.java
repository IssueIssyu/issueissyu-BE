package issueissyu.backend.domain.auth.service;

import issueissyu.backend.domain.auth.dto.res.LoginLinkResDTO;
import issueissyu.backend.domain.auth.exception.AuthException;
import issueissyu.backend.domain.auth.exception.code.AuthErrorCode;
import issueissyu.backend.domain.user.entity.OAuth;
import issueissyu.backend.domain.user.entity.User;
import issueissyu.backend.domain.user.enums.SocialType;
import issueissyu.backend.domain.user.repository.OAuthRepository;
import issueissyu.backend.domain.user.repository.UserRepository;
import issueissyu.backend.global.redis.RefreshTokenRedisStore;
import issueissyu.backend.global.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoginLinkService {

    private final UserRepository userRepository;
    private final OAuthRepository oAuthRepository;
    private final RefreshTokenRedisStore refreshTokenRedisStore;
    private final JwtTokenProvider jwtTokenProvider;

    // 로그인 연동 처리
    // - tempUid: 이번 로그인에서 새로 발급된 임시 uid (예: 78901)
    // - socialType: 연동하려는 소셜 타입 (예: NAVER)
    // - phone: 이미 DB에 존재하는 전화번호

    // 처리 순서:
    // 1. 전화번호로 기존 사용자(existingUser) 조회
    // 2. tempUid 사용자의 OAuth 레코드 삭제 + User 레코드 삭제
    // 3. existingUser에 새 socialType OAuth 추가 (이미 있으면 스킵)
    // 4. Redis에서 tempUid:socialType 토큰 삭제 후 existingUid:socialType 토큰 신규 발급
    // 5. existingUser 정보 반환
    
    @Transactional
    public LoginLinkResDTO link(String tempUid, SocialType socialType, String phone) {
        User existingUser = userRepository.findByPhone(phone)
                .orElseThrow(() -> AuthException.of(AuthErrorCode.LOGIN_LINK_400));

        String existingUid = existingUser.getUid();

        if (existingUid.equals(tempUid)) {
            throw AuthException.of(AuthErrorCode.LOGIN_LINK_400);
        }

        // tempUid 사용자 완전 제거 (OAuth → User 순서)
        oAuthRepository.deleteByUserUid(tempUid);
        userRepository.deleteById(tempUid);

        // existingUser에 새 socialType이 없으면 추가
        boolean alreadyLinked = oAuthRepository.existsByUser_UidAndSocialType(existingUid, socialType);
        if (!alreadyLinked) {
            oAuthRepository.save(OAuth.builder()
                    .user(existingUser)
                    .providerId(tempUid) // temp_uid를 providerId로 임시 사용 (실제론 소셜 provider id 필요)
                    .socialType(socialType)
                    .build());
        }

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
