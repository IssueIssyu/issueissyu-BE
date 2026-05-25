package issueissyu.backend.domain.auth.service;

import issueissyu.backend.domain.auth.dto.req.OnboardingReqDTO;
import issueissyu.backend.domain.auth.dto.res.OnboardingResDTO;
import issueissyu.backend.domain.auth.exception.AuthException;
import issueissyu.backend.domain.auth.exception.code.AuthErrorCode;
import issueissyu.backend.domain.collection.entity.CustomCollection;
import issueissyu.backend.domain.collection.entity.mapping.UserCustomCollection;
import issueissyu.backend.domain.collection.repository.CustomCollectionRepository;
import issueissyu.backend.domain.collection.repository.UserCustomCollectionRepository;
import issueissyu.backend.domain.user.entity.OAuth;
import issueissyu.backend.domain.user.entity.User;
import issueissyu.backend.domain.user.repository.OAuthRepository;
import issueissyu.backend.domain.user.repository.UserRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OnboardingService {

    private static final String DEFAULT_PROFILE_NAME = "default";
    private static final List<String> STARTER_COLLECTION_NAMES = List.of("무관심", "행복양");

    private final UserRepository userRepository;
    private final OAuthRepository oAuthRepository;
    private final CustomCollectionRepository customCollectionRepository;
    private final UserCustomCollectionRepository userCustomCollectionRepository;

    @Transactional
    public OnboardingResDTO onboard(String uid, OnboardingReqDTO request) {
        User user = userRepository.findById(uid)
                .orElseThrow(() -> AuthException.of(AuthErrorCode.ONBOARDING_400));

        if (userRepository.existsByNickname(request.getNickname())) {
            throw AuthException.of(AuthErrorCode.ONBOARDING_400);
        }

        if (userRepository.existsByPhone(request.getPhone())) {
            throw AuthException.of(AuthErrorCode.ONBOARDING_400);
        }

        OAuth oauth = oAuthRepository.findFirstByUser_Uid(uid)
                .orElseThrow(() -> AuthException.of(AuthErrorCode.ONBOARDING_400));

        user.onboard(request.getNickname(), request.getEmail(), request.getPhone());

        CustomCollection defaultProfile = customCollectionRepository
                .findByCustomCollectionName(DEFAULT_PROFILE_NAME)
                .orElseThrow(() -> AuthException.of(AuthErrorCode.ONBOARDING_400));

        UserCustomCollection savedProfile =
                userCustomCollectionRepository
                        .findByUser_UidAndCustomCollection_CustomCollectionName(
                                uid, DEFAULT_PROFILE_NAME)
                        .map(
                                existing -> {
                                    if (!existing.isProfile()) {
                                        existing.markAsProfile();
                                        return userCustomCollectionRepository.save(
                                                existing);
                                    }
                                    return existing;
                                })
                        .orElseGet(
                                () ->
                                        userCustomCollectionRepository.save(
                                                UserCustomCollection.builder()
                                                        .user(user)
                                                        .customCollection(
                                                                defaultProfile)
                                                        .isProfile(true)
                                                        .build()));

        for (String collectionName : STARTER_COLLECTION_NAMES) {
            CustomCollection starterCollection = customCollectionRepository
                    .findByCustomCollectionName(collectionName)
                    .orElseThrow(() -> AuthException.of(AuthErrorCode.ONBOARDING_400));
            userCustomCollectionRepository
                    .findByUser_UidAndCustomCollection_CustomCollectionName(uid, collectionName)
                    .orElseGet(
                            () ->
                                    userCustomCollectionRepository.save(
                                            UserCustomCollection.builder()
                                                    .user(user)
                                                    .customCollection(starterCollection)
                                                    .build()));
        }

        return OnboardingResDTO.builder()
                .uuid(user.getUid())
                .socialType(oauth.getSocialType().name())
                .userCustomCollectionId(savedProfile.getUserCustomCollectionId())
                .customCollectionId(defaultProfile.getCustomCollectionId())
                .customCollectionName(defaultProfile.getCustomCollectionName())
                .customCollectionUrl(defaultProfile.getCustomCollectionS3Url())
                .build();
    }
}
