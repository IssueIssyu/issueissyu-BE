package issueissyu.backend.domain.issue.entity;

import issueissyu.backend.domain.issue.enums.IssuePinState;
import issueissyu.backend.domain.pin.entity.Pin;
import issueissyu.backend.global.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "issue_pin")
@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class IssuePin extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "issue_pin_id")
    private Long issuePinId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pin_id", nullable = false, unique = true)
    @ToString.Exclude
    private Pin pin;

    @Enumerated(EnumType.STRING)
    @Column(name = "issue_pin_state", nullable = false)
    private IssuePinState issuePinState;

    @Builder.Default
    @Column(name = "petition_count", nullable = false)
    private int petitionCount = 0;

    @Builder.Default
    @Column(name = "declaration_count", nullable = false)
    private int declarationCount = 0;

    public void incrementPetitionCount() {
        this.petitionCount++;
    }
}
