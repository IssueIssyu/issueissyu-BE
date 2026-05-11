package issueissyu.backend.domain.user.service.query;

import issueissyu.backend.domain.collection.entity.mapping.UserCustomCollection;
import issueissyu.backend.domain.collection.repository.UserCustomCollectionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserProfileImageQueryServiceImpl implements UserProfileImageQueryService {

    private final UserCustomCollectionRepository userCustomCollectionRepository;

    @Override
    public Optional<String> findUrlByUserUid(String uid) {
        return userCustomCollectionRepository
                .fetchProfileMarkedForUser(uid)
                .map(ucc -> ucc.getCustomCollection().getCustomCollectionS3Url());
    }

    @Override
    public Map<String, String> findUrlsByUserUids(Collection<String> uids) {
        if (uids == null || uids.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<String, String> result = new HashMap<>();
        for (UserCustomCollection ucc : userCustomCollectionRepository.findProfilesByUserUidIn(uids)) {
            result.put(
                    ucc.getUser().getUid(),
                    ucc.getCustomCollection().getCustomCollectionS3Url());
        }
        return result;
    }
}
