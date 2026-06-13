package issueissyu.backend.domain.map.enums;

import issueissyu.backend.domain.map.exception.MapException;
import issueissyu.backend.domain.map.exception.code.MapErrorCode;
import issueissyu.backend.domain.map.exception.code.MapSuccessCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Locale;
import java.util.Optional;

@Getter
@RequiredArgsConstructor
public enum MapPinCategory {

    // 쿼리는 소문자, DB는 대문자, 일반/클러스터링 성공코드
    ISSUE("issue", "ISSUE", MapSuccessCode.MAP_200_2, MapSuccessCode.CLUSTERING_200_2),
    COMMUNICATION("communication", "COMMUNICATION", MapSuccessCode.MAP_200_3, MapSuccessCode.CLUSTERING_200_3),
    STORE("store", "STORE", MapSuccessCode.MAP_200_4, MapSuccessCode.CLUSTERING_200_4),
    FESTIVAL("festival", "FESTIVAL", MapSuccessCode.MAP_200_5, MapSuccessCode.CLUSTERING_200_5);

    private final String queryValue;
    private final String pinType;
    private final MapSuccessCode successCode;
    private final MapSuccessCode clusteringSuccessCode;

    public static Optional<MapPinCategory> parse(String raw) {
        if (raw == null || raw.isBlank()) {
            return Optional.empty();
        }
        String v = raw.trim().toLowerCase(Locale.ROOT);
        for (MapPinCategory c : values()) {
            if (c.queryValue.equals(v)) {
                return Optional.of(c);
            }
        }
        throw MapException.of(MapErrorCode.MAP_400_2);
    }
}
