package issuissyu.backend.domain.collection.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "custom_collection")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class CustomCollection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "custom_collection_id")
    private Long customCollectionId;

    @Column(name = "custom_collection_name", nullable = false, length = 50)
    private String customCollectionName;

    @Column(name = "custom_collection_s3_key", nullable = false, length = 255)
    private String customCollectionS3Key;

    @Column(name = "custom_collection_s3_url", nullable = false, length = 255)
    private String customCollectionS3Url;
}
