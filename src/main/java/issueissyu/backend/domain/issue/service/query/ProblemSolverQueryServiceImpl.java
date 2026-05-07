package issueissyu.backend.domain.issue.service.query;

import issueissyu.backend.domain.collection.repository.UserCustomCollectionRepository;
import issueissyu.backend.domain.issue.dto.res.ProblemSolverItemResDTO;
import issueissyu.backend.domain.issue.dto.res.ProblemSolverListResDTO;
import issueissyu.backend.domain.issue.entity.ProblemSolver;
import issueissyu.backend.domain.issue.entity.ProblemSolverImage;
import issueissyu.backend.domain.issue.enums.ProblemSolveState;
import issueissyu.backend.domain.issue.exception.ProblemSolverException;
import issueissyu.backend.domain.issue.exception.code.IssueErrorCode;
import issueissyu.backend.domain.issue.exception.code.IssueSuccessCode;
import issueissyu.backend.domain.issue.repository.IssuePinRepository;
import issueissyu.backend.domain.issue.repository.ProblemSolverRepository;
import issueissyu.backend.domain.pin.entity.Pin;
import issueissyu.backend.domain.pin.enums.PinType;
import issueissyu.backend.domain.pin.repository.PinRepository;
import issueissyu.backend.domain.user.repository.UserRepository;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProblemSolverQueryServiceImpl implements ProblemSolverQueryService {

    private static final DateTimeFormatter CREATED_AT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS");

    private final PinRepository pinRepository;
    private final IssuePinRepository issuePinRepository;
    private final UserRepository userRepository;
    private final ProblemSolverRepository problemSolverRepository;
    private final UserCustomCollectionRepository userCustomCollectionRepository;

    @Override
    public ProblemSolverListQueryEnvelope findProblemSolverList(
            Long pinId, String pathUserUid, String authUid) {
        Pin pin =
                pinRepository
                        .findById(pinId)
                        .orElseThrow(() -> ProblemSolverException.of(IssueErrorCode.PROBLEM_SOLVER_404_1));

        if (pin.getPinType() != PinType.ISSUE) {
            throw ProblemSolverException.of(IssueErrorCode.PROBLEM_SOLVER_404_1);
        }

        issuePinRepository
                .findByPin_PinId(pinId)
                .orElseThrow(() -> ProblemSolverException.of(IssueErrorCode.PROBLEM_SOLVER_404_1));

        if (!userRepository.existsById(pathUserUid)) {
            throw ProblemSolverException.of(IssueErrorCode.PROBLEM_SOLVER_404_2);
        }

        boolean myPinContext = pin.getUser().getUid().equals(pathUserUid);
        IssueSuccessCode successCode =
                myPinContext ? IssueSuccessCode.PROBLEM_SOLVER_200_2 : IssueSuccessCode.PROBLEM_SOLVER_200_1;

        Boolean isGoNow =
                myPinContext
                        ? null
                        : problemSolverRepository.existsByIssuePin_Pin_PinIdAndUser_Uid(pinId, authUid);

        List<ProblemSolver> solvers = problemSolverRepository.findAllForPinWithAssociations(pinId);
        solvers.sort(
                Comparator.comparing(
                                (ProblemSolver ps) ->
                                        ps.getProblemSolveState() == ProblemSolveState.RESOLVED ? 0 : 1)
                        .thenComparing(
                                ProblemSolver::getCreatedAt,
                                Comparator.nullsLast(Comparator.naturalOrder())));

        if (solvers.isEmpty()) {
            return new ProblemSolverListQueryEnvelope(
                    successCode,
                    ProblemSolverListResDTO.builder().isGoNow(isGoNow).problemSolvers(null).build());
        }

        List<String> solverUids =
                solvers.stream().map(s -> s.getUser().getUid()).distinct().toList();

        Map<String, String> profileUrlByUid = new LinkedHashMap<>();
        if (!solverUids.isEmpty()) {
            userCustomCollectionRepository
                    .findProfilesByUserUidIn(solverUids)
                    .forEach(
                            ucc ->
                                    profileUrlByUid.putIfAbsent(
                                            ucc.getUser().getUid(),
                                            ucc.getCustomCollection().getCustomCollectionS3Url()));
        }

        List<ProblemSolverItemResDTO> items =
                solvers.stream()
                        .map(ps -> mapItem(ps, myPinContext, profileUrlByUid))
                        .collect(Collectors.toList());

        return new ProblemSolverListQueryEnvelope(
                successCode,
                ProblemSolverListResDTO.builder().isGoNow(isGoNow).problemSolvers(items).build());
    }

    private ProblemSolverItemResDTO mapItem(
            ProblemSolver ps, boolean myPinContext, Map<String, String> profileUrlByUid) {
        String imageUrl = null;
        if (ps.getProblemSolveState() != ProblemSolveState.EN_ROUTE) {
            ProblemSolverImage img = ps.getProblemSolverImage();
            if (img != null) {
                imageUrl = img.getProblemSolverImageS3Url();
            }
        }

        String checkAction = null;
        if (myPinContext && ps.getProblemSolveState() == ProblemSolveState.VERIFIED) {
            checkAction = "/api/pins/" + ps.getProblemSolverId();
        }

        LocalDateTime created = ps.getCreatedAt();
        String createdStr =
                created == null
                        ? null
                        : created.truncatedTo(ChronoUnit.MICROS).format(CREATED_AT);

        return ProblemSolverItemResDTO.builder()
                .problemSolverId(ps.getProblemSolverId())
                .problemSolveState(ps.getProblemSolveState().name())
                .problemSolverImageUrl(imageUrl)
                .nickname(ps.getUser().getNickname())
                .createdAt(createdStr)
                .profileUrl(profileUrlByUid.get(ps.getUser().getUid()))
                .checkAction(checkAction)
                .build();
    }
}
