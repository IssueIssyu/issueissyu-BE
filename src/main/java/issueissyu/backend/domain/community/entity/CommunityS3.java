package issueissyu.backend.domain.community.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
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
@Table(name = "community_s3")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class CommunityS3 {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "community_s3_id")
    private Long communityS3Id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "community_id", nullable = false)
    @ToString.Exclude
    private Community community;

    @Column(name = "s3_key", nullable = false, length = 500)
    private String s3Key;

    @Column(name = "s3_url", nullable = false, length = 500)
    private String s3Url;
}
