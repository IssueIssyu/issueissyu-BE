package issueissyu.backend.domain.issue.service.command;

import issueissyu.backend.domain.issue.dto.res.PetitionSubmitResDTO;
import issueissyu.backend.domain.issue.entity.IssuePetition;
import issueissyu.backend.domain.issue.entity.IssuePin;
import issueissyu.backend.domain.issue.exception.PetitionException;
import issueissyu.backend.domain.issue.exception.code.IssueErrorCode;
import issueissyu.backend.domain.issue.repository.IssuePetitionRepository;
import issueissyu.backend.domain.issue.repository.IssuePinRepository;
import issueissyu.backend.domain.user.entity.User;
import issueissyu.backend.domain.user.repository.UserRepository;
import issueissyu.backend.global.api.code.GeneralErrorCode;
import issueissyu.backend.global.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class IssuePetitionCommandServiceImpl implements IssuePetitionCommandService {

    private final IssuePinRepository issuePinRepository;
    private final IssuePetitionRepository issuePetitionRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public PetitionSubmitResDTO submitPetition(Long pinId, String uid) {
        User user =
                userRepository.findById(uid).orElseThrow(() -> GeneralException.of(GeneralErrorCode.USER_NOT_FOUND));
        IssuePin issuePin = issuePinRepository
                .findWithPessimisticWriteByPinId(pinId)
                .orElseThrow(() -> PetitionException.of(IssueErrorCode.PETITION_404));

        if (issuePetitionRepository.existsByIssuePin_Pin_PinIdAndUser_Uid(pinId, uid)) {
            throw PetitionException.of(IssueErrorCode.PETITION_400);
        }

        IssuePetition petition = IssuePetition.builder().user(user).issuePin(issuePin).build();
        try {
            issuePetitionRepository.saveAndFlush(petition);
        } catch (DataIntegrityViolationException e) {
            throw PetitionException.of(IssueErrorCode.PETITION_400);
        }

        issuePin.incrementPetitionCount();

        return PetitionSubmitResDTO.builder()
                .pinId(pinId)
                .petitionCount(issuePin.getPetitionCount())
                .isPetition(true)
                .build();
    }
}
