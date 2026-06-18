package issueissyu.backend.domain.issue.service.command;

import issueissyu.backend.domain.issue.dto.res.GoNowResDTO;
import issueissyu.backend.domain.issue.dto.res.ProblemSolverCheckResDTO;
import issueissyu.backend.domain.issue.dto.res.ProblemSolverPhotoResDTO;
import issueissyu.backend.domain.issue.entity.IssuePin;
import issueissyu.backend.domain.issue.entity.ProblemSolver;
import issueissyu.backend.domain.issue.entity.ProblemSolverImage;
import issueissyu.backend.domain.issue.enums.ProblemSolveState;
import issueissyu.backend.domain.issue.exception.ProblemSolverException;
import issueissyu.backend.domain.issue.exception.code.IssueErrorCode;
import issueissyu.backend.domain.issue.repository.IssuePinRepository;
import issueissyu.backend.domain.issue.repository.ProblemSolverImageRepository;
import issueissyu.backend.domain.issue.repository.ProblemSolverRepository;
import issueissyu.backend.domain.pin.entity.Pin;
import issueissyu.backend.domain.pin.enums.PinType;
import issueissyu.backend.domain.pin.repository.PinRepository;
import issueissyu.backend.domain.user.repository.UserRepository;
import issueissyu.backend.global.api.code.GeneralErrorCode;
import issueissyu.backend.global.exception.GeneralException;
import issueissyu.backend.utils.Image.ImageUtil;
import issueissyu.backend.utils.S3.S3Dto;
import issueissyu.backend.utils.S3.S3Utils;
import issueissyu.backend.utils.exception.UtilException;
import issueissyu.backend.utils.exception.UtilException.Reason;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProblemSolverCommandServiceImpl implements ProblemSolverCommandService {

    private static final long MAX_SOLVER_PHOTO_BYTES = 50L * 1024 * 1024;
    private static final String SOLVER_PHOTO_PREFIX = "solver-photos";

    private final ProblemSolverRepository problemSolverRepository;
    private final ProblemSolverImageRepository problemSolverImageRepository;
    private final IssuePinRepository issuePinRepository;
    private final PinRepository pinRepository;
    private final UserRepository userRepository;
    private final S3Utils s3Utils;
    private final ImageUtil imageUtil;

    @Override
    @Transactional
    public GoNowResDTO participateGoNow(Long pinId, String uid) {
        var user =
                userRepository.findById(uid).orElseThrow(() -> GeneralException.of(GeneralErrorCode.USER_NOT_FOUND));

        IssuePin issuePin =
                issuePinRepository
                        .findWithPessimisticWriteByPinId(pinId)
                        .orElseThrow(
                                () -> {
                                    Pin pin =
                                            pinRepository
                                                    .findById(pinId)
                                                    .orElseThrow(
                                                            () ->
                                                                    ProblemSolverException.of(
                                                                            IssueErrorCode.GO_NOW_404));
                                    return (pin.getPinType() != PinType.ISSUE)
                                            ? ProblemSolverException.of(IssueErrorCode.GO_NOW_400_2)
                                            : ProblemSolverException.of(IssueErrorCode.GO_NOW_404);
                                });

        if (issuePin.getPin().getPinType() != PinType.ISSUE) {
            throw ProblemSolverException.of(IssueErrorCode.GO_NOW_400_2);
        }

        if (issuePin.getPin().getUser().getUid().equals(uid)) {
            throw ProblemSolverException.of(IssueErrorCode.PETITION_400_3);
        }

        if (problemSolverRepository.existsByIssuePin_Pin_PinIdAndUser_Uid(pinId, uid)) {
            throw ProblemSolverException.of(IssueErrorCode.GO_NOW_400_1);
        }

        ProblemSolver saved;
        try {
            saved =
                    problemSolverRepository.saveAndFlush(
                            ProblemSolver.builder()
                                    .user(user)
                                    .issuePin(issuePin)
                                    .build());
        } catch (DataIntegrityViolationException e) {
            throw ProblemSolverException.of(IssueErrorCode.GO_NOW_400_1);
        }

        issuePin.markInProgressIfBeforeProgress();

        return GoNowResDTO.builder()
                .pinId(pinId)
                .problemSolverId(saved.getProblemSolverId())
                .problemSolveState(saved.getProblemSolveState().name())
                .build();
    }

    @Override
    @Transactional
    public ProblemSolverPhotoResDTO attachVerificationPhoto(
            Long problemSolverId, String uid, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw ProblemSolverException.of(IssueErrorCode.PROBLEM_SOLVER_PHOTO_400_2);
        }
        if (file.getSize() > MAX_SOLVER_PHOTO_BYTES) {
            throw ProblemSolverException.of(IssueErrorCode.PROBLEM_SOLVER_PHOTO_400_1);
        }
        try {
            imageUtil.validateImage(file);
        } catch (UtilException e) {
            if (e.getReason() == Reason.FILE_TOO_LARGE) {
                throw ProblemSolverException.of(IssueErrorCode.PROBLEM_SOLVER_PHOTO_400_1);
            }
            throw ProblemSolverException.of(IssueErrorCode.PROBLEM_SOLVER_PHOTO_400_2);
        }

        ProblemSolver solver =
                problemSolverRepository
                        .fetchWithUserAndImage(problemSolverId)
                        .orElseThrow(() -> ProblemSolverException.of(IssueErrorCode.PROBLEM_SOLVER_PHOTO_404));

        if (!solver.getUser().getUid().equals(uid)) {
            throw ProblemSolverException.of(IssueErrorCode.PROBLEM_SOLVER_PHOTO_400_2);
        }

        if (solver.getProblemSolveState() != ProblemSolveState.EN_ROUTE) {
            throw ProblemSolverException.of(IssueErrorCode.PROBLEM_SOLVER_PHOTO_400_2);
        }

        S3Dto uploaded;
        try {
            uploaded = s3Utils.uploadMultipartUnderDirectory(file, SOLVER_PHOTO_PREFIX);
        } catch (UtilException e) {
            log.warn("S3 upload failed for problemSolverId={}", problemSolverId, e);
            throw ProblemSolverException.of(IssueErrorCode.PROBLEM_SOLVER_PHOTO_400_2);
        }

        ProblemSolverImage image =
                problemSolverImageRepository
                        .findByProblemSolver_ProblemSolverId(problemSolverId)
                        .map(
                                entity -> {
                                    String previousKey = entity.getProblemSolverImageS3Key();
                                    if (previousKey != null && !previousKey.isBlank()) {
                                        try {
                                            s3Utils.deleteFile(previousKey);
                                        } catch (UtilException ex) {
                                            log.warn(
                                                    "Failed to delete previous solver image key={}",
                                                    previousKey,
                                                    ex);
                                        }
                                    }
                                    entity.updateStorage(uploaded.getUrl(), uploaded.getKey());
                                    return problemSolverImageRepository.save(entity);
                                })
                        .orElseGet(
                                () ->
                                        problemSolverImageRepository.save(
                                                ProblemSolverImage.builder()
                                                        .problemSolver(solver)
                                                        .problemSolverImageS3Url(uploaded.getUrl())
                                                        .problemSolverImageS3Key(uploaded.getKey())
                                                        .build()));

        solver.markVerified();
        problemSolverRepository.save(solver);

        return ProblemSolverPhotoResDTO.builder()
                .photoId(image.getProblemSolverImageId())
                .photoUrl(uploaded.getUrl())
                .problemSolveState(ProblemSolveState.VERIFIED.name())
                .build();
    }

    @Override
    @Transactional
    public ProblemSolverCheckResDTO resolveCitizenVerification(Long problemSolverId, String uid) {
        ProblemSolver solver =
                problemSolverRepository
                        .fetchWithPinOwnerAndSolver(problemSolverId)
                        .orElseThrow(() -> ProblemSolverException.of(IssueErrorCode.PROBLEM_SOLVER_CHECK_404));

        ProblemSolveState state = solver.getProblemSolveState();
        if (state == ProblemSolveState.EN_ROUTE || state == ProblemSolveState.RESOLVED) {
            throw ProblemSolverException.of(IssueErrorCode.PROBLEM_SOLVER_CHECK_400_1);
        }

        if (!solver.getIssuePin().getPin().getUser().getUid().equals(uid)) {
            throw ProblemSolverException.of(IssueErrorCode.PROBLEM_SOLVER_CHECK_400_2);
        }

        solver.markResolved();
        problemSolverRepository.save(solver);

        solver.getIssuePin().markResolvedIfInProgress();

        return ProblemSolverCheckResDTO.builder()
                .problemSolveState(ProblemSolveState.RESOLVED.name())
                .build();
    }
}
