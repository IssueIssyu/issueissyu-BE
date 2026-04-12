package issueissyu.backend.domain.user.entity;

import issueissyu.backend.global.entity.BaseEntity;
import issueissyu.backend.global.persistence.PGpointUserType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.Type;
import org.postgresql.geometric.PGpoint;

@Entity(name = "AppUser")
@Table(name = "\"user\"")
@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@ToString
public class User extends BaseEntity {

    @Id
    @Column(name = "uid", nullable = false, length = 36)
    private String uid;

    @Column(unique = true, length = 13)
    private String phone;

    @Column(length = 15)
    private String nickname;

    @Type(PGpointUserType.class)
    @Column(name = "user_point", columnDefinition = "point")
    private PGpoint userPoint;

    @Column(length = 255)
    private String email;

    @Builder.Default
    @Column(name = "event_alarm_active", nullable = false)
    private boolean eventAlarmActive = false;

    @Builder.Default
    @Column(name = "like_alarm_active", nullable = false)
    private boolean likeAlarmActive = false;

    @Builder.Default
    @Column(name = "hot_alarm_active", nullable = false)
    private boolean hotAlarmActive = false;

    @Builder.Default
    @Column(name = "store_alarm_active", nullable = false)
    private boolean storeAlarmActive = false;
}
