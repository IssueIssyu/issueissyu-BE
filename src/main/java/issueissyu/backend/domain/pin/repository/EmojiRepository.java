package issueissyu.backend.domain.pin.repository;

import issueissyu.backend.domain.pin.entity.Emoji;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EmojiRepository extends JpaRepository<Emoji, Long> {
    Optional<Emoji> findByProductId(String productId);

    List<Emoji> findAllByOrderByEmojiIdAsc();

    List<Emoji> findAllByIsDefaultTrueOrderByEmojiIdAsc();

    List<Emoji> findAllByIsDefaultFalseOrderByEmojiIdAsc();
}
