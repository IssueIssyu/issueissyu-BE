package issueissyu.backend.domain.user.repository;

import issueissyu.backend.domain.user.entity.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, String> {

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
}
