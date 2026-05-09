package issueissyu.backend.domain.user.service.query;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

public interface UserProfileImageQueryService {

    Optional<String> findUrlByUserUid(String uid);

    Map<String, String> findUrlsByUserUids(Collection<String> uids);
}
