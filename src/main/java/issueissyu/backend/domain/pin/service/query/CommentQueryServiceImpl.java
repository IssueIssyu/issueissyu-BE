package issueissyu.backend.domain.pin.service.query;

import issueissyu.backend.domain.pin.converter.CommentConverter;
import issueissyu.backend.domain.pin.dto.res.CommentResDTO;
import issueissyu.backend.domain.pin.exception.code.PinErrorCode;
import issueissyu.backend.domain.pin.repository.CommentRepository;
import issueissyu.backend.domain.pin.repository.PinRepository;
import issueissyu.backend.global.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentQueryServiceImpl implements CommentQueryService {
    private final CommentRepository commentRepository;
    private final PinRepository pinRepository;

    @Override
    public List<CommentResDTO> getComments(Long pinId, String uid) {
        if (!pinRepository.existsById(pinId)) {
            throw GeneralException.of(PinErrorCode.PIN_NOT_FOUND);
        }

        return commentRepository.findAllByPinPinIdOrderByCreatedAtAsc(pinId).stream()
                .map(comment -> CommentConverter.toCommentResDTO(comment, uid))
                .toList();
    }
}
