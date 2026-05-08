package issueissyu.backend.domain.pin.service.query;

import issueissyu.backend.domain.pin.converter.CommentConverter;
import issueissyu.backend.domain.pin.dto.res.CommentResDTO;
import issueissyu.backend.domain.pin.entity.Comment;
import issueissyu.backend.domain.pin.exception.PinException;
import issueissyu.backend.domain.pin.exception.code.PinErrorCode;
import issueissyu.backend.domain.pin.repository.CommentRepository;
import issueissyu.backend.domain.pin.repository.PinRepository;
import issueissyu.backend.domain.user.service.query.UserProfileImageQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentQueryServiceImpl implements CommentQueryService {
    private final CommentRepository commentRepository;
    private final PinRepository pinRepository;
    private final UserProfileImageQueryService userProfileImageQueryService;

    @Override
    public List<CommentResDTO> getComments(Long pinId, String uid) {
        if (!pinRepository.existsById(pinId)) {
            throw PinException.of(PinErrorCode.PIN_NOT_FOUND);
        }

        // 댓글 정렬 기준은 최신순(createdAt DESC)으로 고정
        List<Comment> comments =
                commentRepository.findAllByPinPinIdOrderByCreatedAtDesc(pinId);
        Set<String> authorUids =
                comments.stream().map(c -> c.getUser().getUid()).collect(Collectors.toSet());
        Map<String, String> profileUrlByUid = userProfileImageQueryService.findUrlsByUserUids(authorUids);
        return comments.stream()
                .map(
                        comment ->
                                CommentConverter.toCommentResDTO(
                                        comment,
                                        uid,
                                        profileUrlByUid.get(comment.getUser().getUid())))
                .toList();
    }
}
