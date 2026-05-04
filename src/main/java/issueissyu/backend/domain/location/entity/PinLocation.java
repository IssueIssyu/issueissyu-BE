package issueissyu.backend.domain.location.entity;

import issueissyu.backend.domain.pin.entity.Pin;
import issueissyu.backend.global.entity.BaseEntity;
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
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.locationtech.jts.geom.Point;

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

    @JdbcTypeCode(SqlTypes.GEOMETRY)
    @Column(name = "pin_point", nullable = false, columnDefinition = "geometry(Point,4326)") // 4326은 WGS84 좌표계(GPS가 사용하는 전 세계 표준 위경도 시스템)를 의미합니다. 네이버 지도 API가 해당 좌표계를 이용하므로 반드시 맞추어야 합니다.
    private Point pinPoint;

    @Column(name = "detail_address", nullable = false, length = 150)
    private String detailAddress;
}
