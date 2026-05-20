package issueissyu.backend.domain.issue.service.query;

import issueissyu.backend.domain.issue.dto.res.PetitionStatusResDTO;
import issueissyu.backend.domain.issue.entity.IssuePin;
import issueissyu.backend.domain.issue.exception.PetitionException;
import issueissyu.backend.domain.issue.exception.code.IssueErrorCode;
import issueissyu.backend.domain.issue.repository.IssuePetitionRepository;
import issueissyu.backend.domain.issue.repository.IssuePinRepository;
import issueissyu.backend.domain.location.service.query.LocationTargetQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class IssuePetitionQueryServiceImpl implements IssuePetitionQueryService {

    private final IssuePinRepository issuePinRepository;
    private final IssuePetitionRepository issuePetitionRepository;
    private final LocationTargetQueryService locationTargetQueryService;

    @Override
    public PetitionStatusResDTO getPetitionStatus(Long pinId, String uid) {
        IssuePin issuePin = issuePinRepository
                .findByPin_PinId(pinId)
                .orElseThrow(() -> PetitionException.of(IssueErrorCode.PETITION_STATUS_404));
        boolean isPetitioned =
                issuePetitionRepository.existsByIssuePin_Pin_PinIdAndUser_Uid(pinId, uid);

        int targetPetition = locationTargetQueryService.getTargetPetitionByPinId(pinId);

        return PetitionStatusResDTO.builder()
                .pinId(pinId)
                .petitionCount(issuePin.getPetitionCount())
                .isPetitioned(isPetitioned)
                .targetPetition(targetPetition)
                .build();
    }
}
