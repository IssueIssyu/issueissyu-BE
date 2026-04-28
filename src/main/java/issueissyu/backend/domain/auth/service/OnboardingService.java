package issueissyu.backend.domain.auth.service;

import issueissyu.backend.domain.auth.dto.req.OnboardingReqDTO;
import issueissyu.backend.domain.auth.dto.res.OnboardingResDTO;
import issueissyu.backend.domain.auth.exception.AuthException;
import issueissyu.backend.domain.auth.exception.code.AuthErrorCode;
import issueissyu.backend.domain.user.entity.OAuth;
import issueissyu.backend.domain.user.entity.User;
import issueissyu.backend.domain.user.repository.OAuthRepository;
import issueissyu.backend.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OnboardingService {

    private final UserRepository userRepository;
    private final OAuthRepository oAuthRepository;

    @Transactional
    public OnboardingResDTO onboard(String uid, OnboardingReqDTO request) {
        User user = userRepository.findById(uid)
                .orElseThrow(() -> AuthException.of(AuthErrorCode.ONBOAREDING_400));

        if (userRepository.existsByNickname(request.getNickname())) {
            throw AuthException.of(AuthErrorCode.ONBOAREDING_400);
        }

        if (userRepository.existsByPhone(request.getPhone())) {
            throw AuthException.of(AuthErrorCode.ONBOAREDING_400);
        }

        OAuth oauth = oAuthRepository.findFirstByUser_Uid(uid)
                .orElseThrow(() -> AuthException.of(AuthErrorCode.ONBOAREDING_400));

        user.onboard(request.getNickname(), request.getEmail(), request.getPhone());

        return OnboardingResDTO.builder()
                .uuid(user.getUid())
                .socialType(oauth.getSocialType().name())
                .build();
    }
}
