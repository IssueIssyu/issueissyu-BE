package issueissyu.backend.domain.user.entity;

import issueissyu.backend.global.persistence.PGpointUserType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;
import org.postgresql.geometric.PGpoint;
import issueissyu.backend.domain.location.entity.Location;

@Embeddable
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserLocation {

    @Type(PGpointUserType.class)
    @Column(name = "user_point", columnDefinition = "point")
    private PGpoint userPoint;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id")
    private Location location;

}
