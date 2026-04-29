package issueissyu.backend.domain.pin.repository;

import issueissyu.backend.domain.pin.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findAllByPinPinIdOrderByCreatedAtAsc(Long pinId);

    Optional<Comment> findByCommentIdAndPinPinId(Long commentId, Long pinId);
}
