package issueissyu.backend.domain.pin.repository;

import issueissyu.backend.domain.pin.entity.mapping.PinEmoji;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PinEmojiRepository extends JpaRepository<PinEmoji, Long> {

    Optional<PinEmoji> findByPinPinIdAndUserUid(Long pinId, String uid);

    @Query("""
            SELECT pe.emoji.emojiId AS emojiId,
                   pe.emoji.emojiImageUrl AS emojiImageUrl,
                   COUNT(pe) AS count
            FROM PinEmoji pe
            WHERE pe.pin.pinId = :pinId
            GROUP BY pe.emoji.emojiId, pe.emoji.emojiImageUrl
            """)
    List<PinEmojiCountProjection> countByPinIdGroupByEmoji(@Param("pinId") Long pinId);

    interface PinEmojiCountProjection {
        Long getEmojiId();

        String getEmojiImageUrl();

        long getCount();
    }
}
