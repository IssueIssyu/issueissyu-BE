package issueissyu.backend.domain.issue.service.query;

import issueissyu.backend.domain.issue.dto.res.PetitionStatusResDTO;
import issueissyu.backend.domain.issue.entity.IssuePin;
import issueissyu.backend.domain.issue.exception.PetitionException;
import issueissyu.backend.domain.issue.exception.code.IssueErrorCode;
import issueissyu.backend.domain.issue.repository.IssuePetitionRepository;
import issueissyu.backend.domain.issue.repository.IssuePinRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class IssuePetitionQueryServiceImpl implements IssuePetitionQueryService {

    // To-do: location.target_petition 연동 전 임시 고정값
    private static final int TARGET_PETITION_PLACEHOLDER = 30;

    private final IssuePinRepository issuePinRepository;
    private final IssuePetitionRepository issuePetitionRepository;

    @Override
    public PetitionStatusResDTO getPetitionStatus(Long pinId, String uid) {
        IssuePin issuePin = issuePinRepository
                .findByPin_PinId(pinId)
                .orElseThrow(() -> PetitionException.of(IssueErrorCode.PETITION_STATUS_404));
        boolean isPetitioned =
                issuePetitionRepository.existsByIssuePin_Pin_PinIdAndUser_Uid(pinId, uid);
        return PetitionStatusResDTO.builder()
                .pinId(pinId)
                .petitionCount(issuePin.getPetitionCount())
                .isPetitioned(isPetitioned)
                .targetPetition(TARGET_PETITION_PLACEHOLDER)
                .build();
    }
}
