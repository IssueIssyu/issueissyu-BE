package issueissyu.backend.domain.map.entity;

import issueissyu.backend.domain.pin.entity.Pin;
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
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "notice")
@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Notice extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notice_id")
    private Long noticeId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pin_id", nullable = false, unique = true)
    @ToString.Exclude
    private Pin pin;

    @Column(name = "notice_start_time", nullable = false)
    private LocalDateTime noticeStartTime;

    @Column(name = "notice_end_time", nullable = false)
    private LocalDateTime noticeEndTime;

    @Column(name = "notice_content", nullable = false, length = 255)
    private String noticeContent;
}
