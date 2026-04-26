package issueissyu.backend.domain.pin.entity;

import issueissyu.backend.domain.pin.enums.EmojiType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@Table(name = "emogji")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Emogji {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "emoji_id")
    private Long emojiId;

    @Enumerated(EnumType.STRING)
    @Column(name = "emoji_type", nullable = false)
    private EmojiType emojiType;

    @Column(name = "product_id", length = 100, unique = true)
    private String productId;

    @Column(name = "emoji_image_url", length = 1000, nullable = false)
    private String emojiImageUrl;

    @Default
    @Column(name = "is_default", nullable = false)
    private boolean isDefault = false;
}
