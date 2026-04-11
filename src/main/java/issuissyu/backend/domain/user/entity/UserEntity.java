package issuissyu.backend.domain.user.entity;

import issuissyu.backend.global.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Entity(name = "AppUser")
@Table(name = "\"user\"")
@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@ToString
public class UserEntity extends BaseEntity {

    @Id
    @Column(name = "uid", nullable = false, length = 36)
    private String uid;

    @Column(nullable = false, length = 13)
    private String phone;

    @Column(nullable = false, length = 15)
    private String nickname;

    @Column(name = "user_point", columnDefinition = "text")
    private String userPoint;

    @Column(nullable = false, length = 30)
    private String email;

    @Column(name = "is_agreed", nullable = false)
    private boolean isAgreed;

    @Column(name = "event_alarm_active", nullable = false)
    private boolean eventAlarmActive;

    @Column(name = "like_alarm_active", nullable = false)
    private boolean likeAlarmActive;

    @Column(name = "hot_alarm_active", nullable = false)
    private boolean hotAlarmActive;

    @Column(name = "store_alarm_active", nullable = false)
    private boolean storeAlarmActive;
}
