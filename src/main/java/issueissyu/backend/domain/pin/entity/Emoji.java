package issueissyu.backend.domain.pin.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "emoji")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Emoji {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "emoji_id")
    private Long emojiId;

    @Column(name = "product_id", length = 100, unique = true)
    private String productId;

    @Column(name = "emoji_image_url", length = 1000, nullable = false)
    private String emojiImageUrl;

    @Default
    @Column(name = "is_default", nullable = false)
    private boolean isDefault = false;
}
