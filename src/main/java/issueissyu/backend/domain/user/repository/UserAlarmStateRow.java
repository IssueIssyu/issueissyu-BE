package issueissyu.backend.domain.user.repository;

public interface UserAlarmStateRow {

    Boolean getLikeAlarmActive();

    Boolean getEventAlarmActive();

    Boolean getHotAlarmActive();

    Boolean getStoreAlarmActive();
}
