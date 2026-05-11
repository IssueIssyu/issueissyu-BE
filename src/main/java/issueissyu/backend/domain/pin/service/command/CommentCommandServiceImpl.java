package issueissyu.backend.domain.pin.service.command;

import issueissyu.backend.domain.pin.converter.CommentConverter;
import issueissyu.backend.domain.pin.dto.req.CommentReqDTO;
import issueissyu.backend.domain.pin.dto.res.CommentResDTO;
import issueissyu.backend.domain.pin.entity.Comment;
import issueissyu.backend.domain.pin.entity.Pin;
import issueissyu.backend.domain.pin.exception.PinException;
import issueissyu.backend.domain.pin.exception.code.PinErrorCode;
import issueissyu.backend.domain.pin.repository.CommentRepository;
import issueissyu.backend.domain.pin.repository.PinRepository;
import issueissyu.backend.domain.user.entity.User;
import issueissyu.backend.domain.user.repository.UserRepository;
import issueissyu.backend.domain.user.service.query.UserProfileImageQueryService;
import issueissyu.backend.global.api.code.GeneralErrorCode;
import issueissyu.backend.global.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class CommentCommandServiceImpl implements CommentCommandService {
    private final CommentRepository commentRepository;
    private final PinRepository pinRepository;
    private final UserRepository userRepository;
    private final CommunicationPinActivityMarker communicationPinActivityMarker;
    private final UserProfileImageQueryService userProfileImageQueryService;

    @Override
    public CommentResDTO createComment(Long pinId, String uid, CommentReqDTO request) {
        Pin pin = pinRepository.findById(pinId)
                .orElseThrow(() -> PinException.of(PinErrorCode.PIN_NOT_FOUND));
        User user = userRepository.findById(uid)
                .orElseThrow(() -> GeneralException.of(GeneralErrorCode.USER_NOT_FOUND));

        Comment saved = commentRepository.save(Comment.builder()
                .pin(pin)
                .user(user)
                .commentContent(request.getCommentContent())
                .build());

        communicationPinActivityMarker.markReactionOrComment(pinId);

        String profileImageUrl =
                userProfileImageQueryService.findUrlByUserUid(saved.getUser().getUid()).orElse(null);
        return CommentConverter.toCommentResDTO(saved, uid, profileImageUrl);
    }

    @Override
    public CommentResDTO updateComment(Long commentId, String uid, CommentReqDTO request) {
        Comment comment = commentRepository.findByIdWithUserAndPin(commentId)
                .orElseThrow(() -> PinException.of(PinErrorCode.COMMENT_NOT_FOUND));

        if (!comment.getUser().getUid().equals(uid)) {
            throw PinException.of(PinErrorCode.COMMENT_FORBIDDEN);
        }

        comment.updateContent(request.getCommentContent());
        commentRepository.flush();
        communicationPinActivityMarker.markReactionOrComment(comment.getPin().getPinId());
        String profileImageUrl =
                userProfileImageQueryService.findUrlByUserUid(comment.getUser().getUid()).orElse(null);
        return CommentConverter.toCommentResDTO(comment, uid, profileImageUrl);
    }

    @Override
    public void deleteComment(Long commentId, String uid) {
        Comment comment = commentRepository.findByIdWithUserAndPin(commentId)
                .orElseThrow(() -> PinException.of(PinErrorCode.COMMENT_NOT_FOUND));

        if (!comment.getUser().getUid().equals(uid)) {
            throw PinException.of(PinErrorCode.COMMENT_FORBIDDEN);
        }
        commentRepository.delete(comment);
    }
}
