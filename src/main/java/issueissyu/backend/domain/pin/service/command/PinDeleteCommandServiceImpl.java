package issueissyu.backend.domain.pin.service.command;

import issueissyu.backend.domain.community.repository.CardnewsImageS3Repository;
import issueissyu.backend.domain.community.repository.CommunityRepository;
import issueissyu.backend.domain.issue.entity.IssuePin;
import issueissyu.backend.domain.issue.entity.ProblemSolver;
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
import issueissyu.backend.domain.pin.repository.PinLikeRepository;
import issueissyu.backend.domain.pin.repository.PinRepository;
import issueissyu.backend.domain.pin.repository.StoreImageRepository;
import issueissyu.backend.domain.location.repository.PinLocationRepository;
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
    private final IssuePinRepository issuePinRepository;
    private final IssuePetitionRepository issuePetitionRepository;
    private final ProblemSolverRepository problemSolverRepository;
    private final ProblemSolverImageRepository problemSolverImageRepository;
    private final CommunicationPinRepository communicationPinRepository;
    private final EventPinRepository eventPinRepository;
    private final StoreImageRepository storeImageRepository;
    private final PinLocationRepository pinLocationRepository;

    @Override
    public void deletePin(String uid, Long pinId) {
        try {
            Pin pin = pinRepository.findById(pinId).orElseThrow(() -> PinException.of(PinErrorCode.PIN_DELETE_400_2));
            if (!pin.getUser().getUid().equals(uid)) {
                throw PinException.of(PinErrorCode.PIN_DELETE_400_3);
            }
            if (pin.getPinType() == PinType.ISSUE && communityRepository.existsByPin_PinId(pinId)) {
                throw PinException.of(PinErrorCode.PIN_DELETE_400_1);
            }

            communityRepository
                    .findByPin_PinId(pinId)
                    .ifPresent(
                            c -> {
                                cardnewsImageS3Repository.deleteByCommunity_CommunityId(c.getCommunityId());
                                communityRepository.delete(c);
                            });

            noticeRepository.deleteByPin_PinId(pinId);
            commentRepository.deleteByPin_PinId(pinId);
            declarationRepository.deleteByPin_PinId(pinId);
            pinLikeRepository.deleteByPin_PinId(pinId);
            pinEmojiRepository.deleteByPin_PinId(pinId);

            issuePinRepository
                    .findByPin_PinId(pinId)
                    .ifPresent(
                            issuePin -> {
                                deleteIssueAssociations(issuePin);
                                issuePinRepository.delete(issuePin);
                            });

            communicationPinRepository.deleteByPin_PinId(pinId);

            eventPinRepository
                    .findByPin_PinId(pinId)
                    .ifPresent(
                            ep -> {
                                storeImageRepository
                                        .findByEventPin_Pin_PinId(pinId)
                                        .ifPresent(storeImageRepository::delete);
                                eventPinRepository.delete(ep);
                            });

            pinLocationRepository.deleteByPin_PinId(pinId);
            pinRepository.delete(pin);
        } catch (PinException e) {
            throw e;
        } catch (Exception e) {
            throw PinException.of(PinErrorCode.PIN_DELETE_400_4);
        }
    }

    private void deleteIssueAssociations(IssuePin issuePin) {
        Long issuePinId = issuePin.getIssuePinId();
        issuePetitionRepository.deleteByIssuePin_IssuePinId(issuePinId);

        List<ProblemSolver> solvers = problemSolverRepository.findAllByIssuePin_IssuePinId(issuePinId);
        for (ProblemSolver ps : solvers) {
            problemSolverImageRepository
                    .findByProblemSolver_ProblemSolverId(ps.getProblemSolverId())
                    .ifPresent(problemSolverImageRepository::delete);
            problemSolverRepository.delete(ps);
        }
    }
}
