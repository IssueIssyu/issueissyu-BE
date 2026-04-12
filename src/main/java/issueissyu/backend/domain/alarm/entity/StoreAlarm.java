package issueissyu.backend.domain.alarm.entity;

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
@Table(name = "store_alarm")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class StoreAlarm {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "store_alarm_id")
    private Long storeAlarmId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_alarm_id", nullable = false)
    @ToString.Exclude
    private UserAlarm userAlarm;

    @Column(name = "store_alarm_body", nullable = false, length = 255)
    private String storeAlarmBody;

    @Column(name = "store_alarm_title", nullable = false, length = 100)
    private String storeAlarmTitle;
}
