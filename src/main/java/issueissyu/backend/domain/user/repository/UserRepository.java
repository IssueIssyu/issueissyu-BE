package issueissyu.backend.domain.user.repository;

import issueissyu.backend.domain.user.entity.User;
import issueissyu.backend.domain.user.enums.UserRole;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, String> {

    Optional<User> findFirstByRole(UserRole role);

    boolean existsByUserName(String userName);

    boolean existsByNickname(String nickname);

    boolean existsByPhone(String phone);

    Optional<User> findByPhone(String phone);

    // 특정 location 에서 동네 인증했고 이벤트 알람 ON + push_token 보유 사용자 
    @Query("""
            select u from AppUser u
            where u.userLocation.location.locationId = :locationId
              and u.eventAlarmActive = true
              and u.pushToken is not null
            """)
    List<User> findEventAlarmEligibleByLocationId(@Param("locationId") Long locationId);

    // 특정 location 에서 동네 인증했고 가게 알람 ON + push_token 보유 사용자 
    @Query("""
            select u from AppUser u
            where u.userLocation.location.locationId = :locationId
              and u.storeAlarmActive = true
              and u.pushToken is not null
            """)
    List<User> findStoreAlarmEligibleByLocationId(@Param("locationId") Long locationId);

    @Query("""
            select u from AppUser u
            where u.userLocation.location.locationId = :locationId
              and u.hotAlarmActive = true
              and u.pushToken is not null
            """)
    List<User> findHotAlarmEligibleByLocationId(@Param("locationId") Long locationId);

    @Query("""
            select distinct u.userLocation.location.locationId from AppUser u
            where u.hotAlarmActive = true
              and u.pushToken is not null
              and u.userLocation is not null
              and u.userLocation.location is not null
            """)
    List<Long> findDistinctHotAlarmEligibleLocationIds();

    @Query(
            value =
                    """
                    SELECT like_alarm_active AS likeAlarmActive,
                           event_alarm_active AS eventAlarmActive,
                           hot_alarm_active AS hotAlarmActive,
                           store_alarm_active AS storeAlarmActive
                    FROM "user"
                    WHERE uid = :uid
                    """,
            nativeQuery = true)
    Optional<UserAlarmStateRow> findAlarmStateByUid(@Param("uid") String uid);
}
