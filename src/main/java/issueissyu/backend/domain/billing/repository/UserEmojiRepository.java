package issueissyu.backend.domain.billing.repository;

import issueissyu.backend.domain.pin.entity.mapping.UserEmoji;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserEmojiRepository extends JpaRepository<UserEmoji, Long> {

    boolean existsByUserUidAndEmojiEmojiId(String uid, Long emojiId);

    boolean existsByPurchaseToken(String purchaseToken);

    Optional<UserEmoji> findByUserUidAndEmojiEmojiId(String uid, Long emojiId);

    @Query("SELECT ue.emoji.emojiId FROM UserEmoji ue WHERE ue.user.uid = :uid")
    List<Long> findOwnedEmojiIdsByUid(@Param("uid") String uid);
}
