package issueissyu.backend.domain.pin.repository;

import issueissyu.backend.domain.pin.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    Page<Comment> findAllByPinPinId(Long pinId, Pageable pageable);

    Optional<Comment> findByCommentIdAndPinPinId(Long commentId, Long pinId);
}
