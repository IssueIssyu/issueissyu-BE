package issueissyu.backend.domain.pin.service.query;

import issueissyu.backend.domain.pin.converter.CommentConverter;
import issueissyu.backend.domain.pin.dto.res.CommentResDTO;
import issueissyu.backend.domain.pin.exception.PinException;
import issueissyu.backend.domain.pin.exception.code.PinErrorCode;
import issueissyu.backend.domain.pin.repository.CommentRepository;
import issueissyu.backend.domain.pin.repository.PinRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentQueryServiceImpl implements CommentQueryService {
    private final CommentRepository commentRepository;
    private final PinRepository pinRepository;

    @Override
    public Page<CommentResDTO> getComments(Long pinId, String uid, Pageable pageable) {
        if (!pinRepository.existsById(pinId)) {
            throw PinException.of(PinErrorCode.PIN_NOT_FOUND);
        }

        return commentRepository.findAllByPinPinIdOrderByCreatedAtAsc(pinId, pageable)
                .map(comment -> CommentConverter.toCommentResDTO(comment, uid));
    }
}
