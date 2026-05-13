package issueissyu.backend.domain.user.repository;

import issueissyu.backend.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


public interface UserRepository extends JpaRepository<User, String> {

    boolean existsByUserName(String userName);

    boolean existsByNickname(String nickname);
    boolean existsByPhone(String phone);
    Optional<User> findByPhone(String phone);
}
