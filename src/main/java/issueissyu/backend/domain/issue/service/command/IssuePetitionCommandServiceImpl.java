package issueissyu.backend.domain.issue.service.command;

import issueissyu.backend.domain.issue.dto.res.PetitionSubmitResDTO;
import issueissyu.backend.domain.issue.entity.IssuePetition;
import issueissyu.backend.domain.issue.entity.IssuePin;
import issueissyu.backend.domain.issue.exception.PetitionException;
import issueissyu.backend.domain.issue.exception.code.IssueErrorCode;
import issueissyu.backend.domain.issue.repository.IssuePetitionRepository;
import issueissyu.backend.domain.issue.repository.IssuePinRepository;
import issueissyu.backend.domain.pin.entity.Pin;
import issueissyu.backend.domain.pin.enums.PinType;
import issueissyu.backend.domain.pin.repository.PinRepository;
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

    private final PinRepository pinRepository;
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
                .orElseThrow(() -> {
                    Pin pin = pinRepository.findById(pinId)
                            .orElseThrow(() -> PetitionException.of(IssueErrorCode.PETITION_404));
                    return (pin.getPinType() != PinType.ISSUE)
                            ? PetitionException.of(IssueErrorCode.PETITION_400_2)
                            : PetitionException.of(IssueErrorCode.PETITION_404);
                });

        if (issuePin.getPin().getPinType() != PinType.ISSUE) {
            throw PetitionException.of(IssueErrorCode.PETITION_400_2);
        }

        IssuePetition petition = IssuePetition.builder().user(user).issuePin(issuePin).build();
        try {
            issuePetitionRepository.saveAndFlush(petition);
        } catch (DataIntegrityViolationException e) {
            throw PetitionException.of(IssueErrorCode.PETITION_400_1);
        }

        issuePin.incrementPetitionCount();

        return PetitionSubmitResDTO.builder()
                .pinId(pinId)
                .petitionCount(issuePin.getPetitionCount())
                .isPetition(true)
                .build();
    }
}
