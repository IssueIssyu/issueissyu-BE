package issueissyu.backend.domain.issue.entity;

import issueissyu.backend.domain.user.entity.UserEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "issue_petition")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class IssuePetition {

    @Id
    @Column(name = "petition_id", length = 255)
    private String petitionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uid", nullable = false)
    @ToString.Exclude
    private UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "issue_pin_id", nullable = false)
    @ToString.Exclude
    private IssuePin issuePin;
}
