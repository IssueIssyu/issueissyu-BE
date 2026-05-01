package issueissyu.backend.domain.pin.service.query;

import issueissyu.backend.domain.pin.converter.CommentConverter;
import issueissyu.backend.domain.pin.dto.res.CommentResDTO;
import issueissyu.backend.domain.pin.exception.PinException;
import issueissyu.backend.domain.pin.exception.code.PinErrorCode;
import issueissyu.backend.domain.pin.repository.CommentRepository;
import issueissyu.backend.domain.pin.repository.PinRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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

        // 댓글 정렬 기준 최신순으로 고정
        Pageable latestPageable = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        return commentRepository.findAllByPinPinId(pinId, latestPageable)
                .map(comment -> CommentConverter.toCommentResDTO(comment, uid));
    }
}
