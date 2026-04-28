package issueissyu.backend.domain.pin.entity;

import issueissyu.backend.domain.pin.enums.PinType;
import issueissyu.backend.domain.pin.enums.ToneType;
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

    @Column(name = "pin_title", nullable = false, length = 100)
    private String pinTitle;

    @Column(name = "pin_content", nullable = false, columnDefinition = "text")
    private String pinContent;

    @Enumerated(EnumType.STRING)
    @Column(name = "tone_type")
    private ToneType toneType;

    @Builder.Default
    @Column(name = "visibility_status")
    private Boolean visibilityStatus = Boolean.TRUE;

    @Builder.Default
    @OneToMany(mappedBy = "pin", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    private List<PinImage> pinImages = new ArrayList<>();

    public void addPinImage(PinImage pinImage) {
        pinImage.assignPin(this);
        pinImages.add(pinImage);
    }
}
