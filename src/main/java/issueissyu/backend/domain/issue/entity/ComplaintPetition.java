package issueissyu.backend.domain.issue.entity;

import issueissyu.backend.domain.issue.enums.ComplaintPetitionStatus;
import issueissyu.backend.domain.location.entity.LocationDepartment;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "complaint_petition")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class ComplaintPetition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "complaint_petition_id")
    private Long complaintPetitionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_department_id", nullable = false)
    @ToString.Exclude
    private LocationDepartment locationDepartment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "issue_pin_id", nullable = false)
    @ToString.Exclude
    private IssuePin issuePin;

    @Column(name = "pdf_s3_key", columnDefinition = "text")
    private String pdfS3Key;

    @Column(name = "pdf_s3_url", columnDefinition = "text")
    private String pdfS3Url;

    @Column(name = "email_subject", columnDefinition = "text")
    private String emailSubject;

    @Column(name = "generated_on", nullable = false)
    private LocalDate generatedOn;

    @Column(name = "email_body", columnDefinition = "text")
    private String emailBody;

    @Column(name = "reliability_score")
    private Double reliabilityScore;

    @Column(name = "reliability_basis", columnDefinition = "text")
    private String reliabilityBasis;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ComplaintPetitionStatus status;
}
