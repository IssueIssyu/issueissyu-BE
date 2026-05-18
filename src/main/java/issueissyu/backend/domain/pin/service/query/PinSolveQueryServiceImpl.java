package issueissyu.backend.domain.pin.service.query;

import issueissyu.backend.domain.issue.repository.IssuePetitionRepository;
import issueissyu.backend.domain.issue.repository.ProblemSolverRepository;
import issueissyu.backend.domain.pin.dto.res.PinSolveResDTO;
import issueissyu.backend.domain.pin.entity.Pin;
import issueissyu.backend.domain.pin.enums.PinType;
import issueissyu.backend.domain.pin.exception.PinException;
import issueissyu.backend.domain.pin.exception.code.PinErrorCode;
import issueissyu.backend.domain.pin.repository.PinRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PinSolveQueryServiceImpl implements PinSolveQueryService {

    /** TODO: issue_pin.reliability 컬럼 추가 후 엔티티/조회 로직으로 대체 */
    private static final int STUB_AI_RELIABILITY = 66;

    private final PinRepository pinRepository;
    private final IssuePetitionRepository issuePetitionRepository;
    private final ProblemSolverRepository problemSolverRepository;

    @Override
    public PinSolveResDTO getPinSolve(Long pinId, String uid) {
        try {
            Pin pin =
                    pinRepository
                            .findById(pinId)
                            .orElseThrow(() -> PinException.of(PinErrorCode.PIN_SOLVE_404));

            if (pin.getPinType() != PinType.ISSUE) {
                throw PinException.of(PinErrorCode.PIN_SOLVE_400);
            }

            boolean isPetitioned =
                    issuePetitionRepository.existsByIssuePin_Pin_PinIdAndUser_Uid(pinId, uid);
            boolean isProblemSolver =
                    problemSolverRepository.existsByIssuePin_Pin_PinIdAndUser_Uid(pinId, uid);

            return new PinSolveResDTO(isPetitioned, isProblemSolver, STUB_AI_RELIABILITY);
        } catch (PinException e) {
            throw e;
        } catch (Exception e) {
            throw PinException.of(PinErrorCode.PIN_SOLVE_400);
        }
    }
}
