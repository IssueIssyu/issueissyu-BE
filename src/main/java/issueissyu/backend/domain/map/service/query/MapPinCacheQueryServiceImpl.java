package issueissyu.backend.domain.map.service.query;

import issueissyu.backend.domain.map.cache.PinGeoRedisService;
import issueissyu.backend.domain.map.dto.res.MapPinCacheStatusResDTO;
import issueissyu.backend.domain.map.exception.MapException;
import issueissyu.backend.domain.map.exception.code.MapErrorCode;
import issueissyu.backend.domain.user.entity.User;
import issueissyu.backend.domain.user.enums.UserRole;
import issueissyu.backend.domain.user.repository.UserRepository;
import issueissyu.backend.global.api.code.GeneralErrorCode;
import issueissyu.backend.global.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MapPinCacheQueryServiceImpl implements MapPinCacheQueryService {

    private final UserRepository userRepository;
    private final PinGeoRedisService pinGeoRedisService;

    @Override
    public MapPinCacheStatusResDTO getCacheStatus(String uid) {
        User user = userRepository.findById(uid)
                .orElseThrow(() -> GeneralException.of(GeneralErrorCode.USER_NOT_FOUND));
        if (user.getRole() != UserRole.ADMIN) {
            throw MapException.of(MapErrorCode.MAP_CACHE_403);
        }
        return new MapPinCacheStatusResDTO(pinGeoRedisService.getGeoPinCount());
    }
}
