package issueissyu.backend.domain.pin.entity;

import issueissyu.backend.domain.pin.entity.mapping.PinLike;
import issueissyu.backend.domain.pin.enums.PinType;
import issueissyu.backend.domain.pin.enums.ToneType;
import issueissyu.backend.domain.user.entity.User;
import issueissyu.backend.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "pin")
@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Pin extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pin_id")
    private Long pinId;

    @Enumerated(EnumType.STRING)
    @Column(name = "pin_type", nullable = false)
    private PinType pinType;

    @Builder.Default
    @Column(name = "like_count", nullable = false)
    private int likeCount = 0;

    @Builder.Default
    @Column(name = "view_count", nullable = false)
    private int viewCount = 0;

    @Column(name = "pin_title", nullable = false, length = 100)
    private String pinTitle;

    @Column(name = "pin_content", nullable = false, columnDefinition = "text")
    private String pinContent;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "tone_type", nullable = false)
    private ToneType toneType = ToneType.NONE;

    @Builder.Default
    @OneToMany(mappedBy = "pin", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    private List<PinImage> pinImages = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "pin", fetch = FetchType.LAZY)
    @ToString.Exclude
    private List<PinLike> pinLikes = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uid", nullable = false)
    @ToString.Exclude
    private User user;

    public void addPinImage(PinImage pinImage) {
        pinImage.assignPin(this);
        pinImages.add(pinImage);
    }

    public void assignUser(User user) {
        this.user = user;
    }

    public void incrementLikeCount() {
        this.likeCount++;
    }

    public void updatePinDetails(String pinTitle, String pinContent) {
        this.pinTitle = pinTitle;
        this.pinContent = pinContent;
    }
}
