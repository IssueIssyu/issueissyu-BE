package issueissyu.backend.domain.pin.repository;

import issueissyu.backend.domain.pin.entity.Comment;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    @EntityGraph(attributePaths = {"user"})
    List<Comment> findAllByPinPinIdOrderByCreatedAtDesc(Long pinId);

    Optional<Comment> findByCommentIdAndPinPinId(Long commentId, Long pinId);

    @Query("""
            select c
            from Comment c
            join fetch c.user
            join fetch c.pin
            where c.commentId = :commentId
            """)
    Optional<Comment> findByIdWithUserAndPin(@Param("commentId") Long commentId);

    void deleteByPin_PinId(Long pinId);
}
