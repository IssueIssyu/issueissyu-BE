package issueissyu.backend.domain.pin.service.command;

import issueissyu.backend.domain.community.exception.CommunityException;
import issueissyu.backend.domain.community.exception.code.CommunityErrorCode;
import issueissyu.backend.domain.community.repository.CommunityRepository;
import issueissyu.backend.domain.pin.entity.Declaration;
import issueissyu.backend.domain.pin.entity.Pin;
import issueissyu.backend.domain.pin.enums.DeclarationReason;
import issueissyu.backend.domain.pin.repository.DeclarationRepository;
import issueissyu.backend.domain.user.entity.User;
import issueissyu.backend.domain.user.repository.UserRepository;
import issueissyu.backend.global.api.code.GeneralErrorCode;
import issueissyu.backend.global.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class DeclarationCommandServiceImpl implements DeclarationCommandService {

    private final CommunityRepository communityRepository;
    private final DeclarationRepository declarationRepository;
    private final UserRepository userRepository;

    @Override
    public void declareCommunity(Long communityId, String uid, int reasonIndex) {
        Pin pin = communityRepository.findDetailById(communityId)
                .orElseThrow(() -> CommunityException.of(CommunityErrorCode.COMMUNITY_404_1))
                .getPin();

        if (declarationRepository.existsByPin_PinIdAndUser_Uid(pin.getPinId(), uid)) {
            throw CommunityException.of(CommunityErrorCode.COMMUNITY_409_1);
        }

        DeclarationReason.fromIndex(reasonIndex); // 유효 범위 검증

        User user = userRepository.findById(uid)
                .orElseThrow(() -> GeneralException.of(GeneralErrorCode.USER_NOT_FOUND));

        declarationRepository.save(Declaration.builder()
                .pin(pin)
                .user(user)
                .declarationReason(reasonIndex)
                .build());
    }
}
