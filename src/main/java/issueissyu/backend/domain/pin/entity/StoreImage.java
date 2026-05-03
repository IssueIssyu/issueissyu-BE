package issueissyu.backend.domain.pin.entity;

import issueissyu.backend.global.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "store_image")
@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class StoreImage extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "store_image_id")
    private Long storeImageId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_pin_id", nullable = false, unique = true)
    @ToString.Exclude
    private EventPin eventPin;

    @Column(name = "image_s3_key", nullable = false, length = 500)
    private String imageS3Key;

    @Column(name = "image_s3_url", nullable = false, length = 500)
    private String imageS3Url;
}
