package issueissyu.backend.domain.issue.service.command;

import issueissyu.backend.domain.issue.dto.res.GoNowResDTO;
import issueissyu.backend.domain.issue.dto.res.ProblemSolverCheckResDTO;
import issueissyu.backend.domain.issue.dto.res.ProblemSolverPhotoResDTO;
import org.springframework.web.multipart.MultipartFile;

public interface ProblemSolverCommandService {

    GoNowResDTO participateGoNow(Long pinId, String uid);

    ProblemSolverPhotoResDTO attachVerificationPhoto(Long problemSolverId, String uid, MultipartFile file);

    ProblemSolverCheckResDTO resolveCitizenVerification(Long problemSolverId, String uid);
}
