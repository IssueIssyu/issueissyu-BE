package issueissyu.backend.domain.pin.entity;

import issueissyu.backend.domain.pin.enums.PinType;
import issueissyu.backend.domain.pin.enums.ToneType;
import issueissyu.backend.global.entity.BaseEntity;
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
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

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
    @Column(name = "visibility_status", nullable = true)
    private Boolean visibilityStatus = Boolean.TRUE;
}
