package issuissyu.backend.domain.location.entity;

import issuissyu.backend.domain.pin.entity.Pin;
import issuissyu.backend.global.entity.BaseEntity;
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
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "pin_location")
@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class PinLocation extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pin_location_id")
    private Long pinLocationId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pin_id", nullable = false)
    @ToString.Exclude
    private Pin pin;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id", nullable = false)
    @ToString.Exclude
    private Location location;

    @Column(name = "pin_point", nullable = false, columnDefinition = "text")
    private String pinPoint;

    @Column(name = "detail_address", nullable = false, length = 150)
    private String detailAddress;
}
