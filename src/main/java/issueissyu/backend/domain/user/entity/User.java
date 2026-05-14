package issueissyu.backend.domain.user.entity;

import issueissyu.backend.domain.location.entity.Location;
import issueissyu.backend.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.postgresql.geometric.PGpoint;

import java.util.ArrayList;
import java.util.List;

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

    @Column(name = "user_name", nullable = false, length = 100)
    private String userName;

    @Column(unique = true, length = 13)
    private String phone;

    @Column(unique = true, length = 15)
    private String nickname;

    @Embedded
    private UserLocation userLocation;

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

    @Builder.Default
    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    @ToString.Exclude
    private List<OAuth> oauths = new ArrayList<>();

    public void setUserLocation(Location location, PGpoint point) {
        this.userLocation = UserLocation.builder()
                .userPoint(point)
                .location(location)
                .build();
    }

    public void onboard(String nickname, String email, String phone) {
        this.nickname = nickname;
        this.email = email;
        this.phone = phone;
    }

    public void updateAlarmAgreement(boolean active) {
        this.eventAlarmActive = active;
        this.likeAlarmActive = active;
        this.hotAlarmActive = active;
        this.storeAlarmActive = active;
    }

    public void updateNickname(String nickname) {
        this.nickname = nickname;
    }

    public void toggleLikeAlarm() {
        this.likeAlarmActive = !this.likeAlarmActive;
    }

    public void toggleEventAlarm() {
        this.eventAlarmActive = !this.eventAlarmActive;
    }

    public void toggleHotAlarm() {
        this.hotAlarmActive = !this.hotAlarmActive;
    }

    public void toggleStoreAlarm() {
        this.storeAlarmActive = !this.storeAlarmActive;
    }

    // isNew 판별
    public boolean needsLoginOnboarding() {
        return phone == null || nickname == null;
    }
}
