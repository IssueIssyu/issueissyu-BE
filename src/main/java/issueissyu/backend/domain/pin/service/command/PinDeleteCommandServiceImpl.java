package issueissyu.backend.domain.pin.service.command;

import issueissyu.backend.domain.community.repository.CardnewsImageS3Repository;
import issueissyu.backend.domain.community.repository.CommunityRepository;
import issueissyu.backend.domain.issue.repository.ComplaintPetitionRepository;
import issueissyu.backend.domain.issue.repository.IssuePinRepository;
import issueissyu.backend.domain.issue.repository.IssuePetitionRepository;
import issueissyu.backend.domain.issue.repository.ProblemSolverImageRepository;
import issueissyu.backend.domain.issue.repository.ProblemSolverRepository;
import issueissyu.backend.domain.map.repository.NoticeRepository;
import issueissyu.backend.domain.pin.entity.Pin;
import issueissyu.backend.domain.pin.enums.PinType;
import issueissyu.backend.domain.pin.exception.PinException;
import issueissyu.backend.domain.pin.exception.code.PinErrorCode;
import issueissyu.backend.domain.pin.repository.CommentRepository;
import issueissyu.backend.domain.pin.repository.CommunicationPinRepository;
import issueissyu.backend.domain.pin.repository.DeclarationRepository;
import issueissyu.backend.domain.pin.repository.EventPinRepository;
import issueissyu.backend.domain.pin.repository.PinEmojiRepository;
import issueissyu.backend.domain.pin.repository.PinImageRepository;
import issueissyu.backend.domain.pin.repository.PinLikeRepository;
import issueissyu.backend.domain.pin.repository.PinRepository;
import issueissyu.backend.domain.pin.repository.StoreImageRepository;
import issueissyu.backend.domain.location.repository.PinLocationRepository;
import issueissyu.backend.domain.user.enums.UserRole;
import issueissyu.backend.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class PinDeleteCommandServiceImpl implements PinDeleteCommandService {

    private final PinRepository pinRepository;
    private final CommunityRepository communityRepository;
    private final CardnewsImageS3Repository cardnewsImageS3Repository;
    private final NoticeRepository noticeRepository;
    private final CommentRepository commentRepository;
    private final DeclarationRepository declarationRepository;
    private final PinLikeRepository pinLikeRepository;
    private final PinEmojiRepository pinEmojiRepository;
    private final PinImageRepository pinImageRepository;
    private final IssuePinRepository issuePinRepository;
    private final IssuePetitionRepository issuePetitionRepository;
    private final ComplaintPetitionRepository complaintPetitionRepository;
    private final ProblemSolverRepository problemSolverRepository;
    private final ProblemSolverImageRepository problemSolverImageRepository;
    private final CommunicationPinRepository communicationPinRepository;
    private final EventPinRepository eventPinRepository;
    private final StoreImageRepository storeImageRepository;
    private final PinLocationRepository pinLocationRepository;
    private final UserRepository userRepository;

    @Override
    public void deletePin(String uid, Long pinId) {
        Pin pin =
                pinRepository
                        .fetchDetailWithAuthor(pinId)
                        .orElseThrow(() -> PinException.of(PinErrorCode.PIN_DELETE_400_2));
        boolean isAdmin =
                userRepository
                        .findById(uid)
                        .map(user -> user.getRole() == UserRole.ADMIN)
                        .orElse(false);

        if (!isAdmin && !pin.getUser().getUid().equals(uid)) {
            throw PinException.of(PinErrorCode.PIN_DELETE_400_3);
        }
        if (!isAdmin
                && pin.getPinType() == PinType.ISSUE
                && communityRepository.existsByPin_PinId(pinId)) {
            throw PinException.of(PinErrorCode.PIN_DELETE_400_1);
        }

        communityRepository
                .findByPin_PinId(pinId)
                .ifPresent(
                        c ->
                                cardnewsImageS3Repository.deleteByCommunity_CommunityId(
                                        c.getCommunityId()));
        communityRepository.deleteByPin_PinId(pinId);

        issuePinRepository
                .findByPin_PinId(pinId)
                .ifPresent(issuePin -> deleteIssueAssociations(issuePin.getIssuePinId()));
        issuePinRepository.deleteByPin_PinId(pinId);

        storeImageRepository.deleteByEventPin_Pin_PinId(pinId);
        eventPinRepository.deleteByPin_PinId(pinId);

        noticeRepository.deleteByPin_PinId(pinId);
        commentRepository.deleteByPin_PinId(pinId);
        declarationRepository.deleteByPin_PinId(pinId);
        pinLikeRepository.deleteByPin_PinId(pinId);
        pinEmojiRepository.deleteByPin_PinId(pinId);
        communicationPinRepository.deleteByPin_PinId(pinId);
        pinLocationRepository.deleteByPin_PinId(pinId);
        pinImageRepository.deleteByPin_PinId(pinId);
        pinRepository.deleteById(pinId);
    }

    private void deleteIssueAssociations(Long issuePinId) {
        issuePetitionRepository.deleteByIssuePin_IssuePinId(issuePinId);
        complaintPetitionRepository.deleteByIssuePin_IssuePinId(issuePinId);

        List<Long> solverIds = problemSolverRepository.findAllProblemSolverIdsByIssuePin_IssuePinId(issuePinId);
        if (!solverIds.isEmpty()) {
            problemSolverImageRepository.deleteAllByProblemSolver_ProblemSolverIdIn(solverIds);
        }
        problemSolverRepository.deleteAllByIssuePin_IssuePinId(issuePinId);
    }
}
