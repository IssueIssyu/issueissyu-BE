package issuissyu.backend.domain.alarm.entity;

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
@Table(name = "like_alarm")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class LikeAlarm {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "like_alarm_id")
    private Long likeAlarmId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_alarm_id", nullable = false)
    @ToString.Exclude
    private UserAlarm userAlarm;

    @Column(name = "like_alarm_body", nullable = false, length = 255)
    private String likeAlarmBody;

    @Column(name = "like_alarm_title", nullable = false, length = 100)
    private String likeAlarmTitle;
}
