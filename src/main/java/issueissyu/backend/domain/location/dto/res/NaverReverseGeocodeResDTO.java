package issueissyu.backend.domain.location.dto.res;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@JsonIgnoreProperties(ignoreUnknown = true)
public record NaverReverseGeocodeResDTO(
        Status status,
        List<ResultItem> results
) {
    public Optional<String> firstRoadAddress() {
        if (results == null || results.isEmpty()) {
            return Optional.empty();
        }

        return results.stream()
                .filter(item -> "roadaddr".equalsIgnoreCase(item.name()))
                .map(ResultItem::land)
                .filter(land -> land != null)
                .map(land -> {
                    String roadName = land.name() == null ? "" : land.name();
                    String number1 = land.number1() == null ? "" : land.number1();
                    String number2 = (land.number2() == null || land.number2().isBlank()) ? "" : "-" + land.number2();
                    String full = (roadName + " " + number1 + number2).trim();
                    return full.isBlank() ? null : full;
                })
                .filter(address -> address != null && !address.isBlank())
                .findFirst();
    }

    /**
     * {@code name=legalcode} 결과의 법정동코드(행정안전부 법정동 10자리) {@link Code#id}를 반환합니다.
     * {@code code.type}이 {@code L}(법정동)인 항목만 사용합니다.
     */
    public Optional<String> legalDistrictCode() {
        if (results == null || results.isEmpty()) {
            return Optional.empty();
        }
        return results.stream()
                .filter(item -> "legalcode".equalsIgnoreCase(item.name()))
                .map(ResultItem::code)
                .filter(Objects::nonNull)
                .filter(code -> code.type() == null || "L".equalsIgnoreCase(code.type()))
                .map(Code::id)
                .filter(id -> id != null && !id.isBlank())
                .findFirst();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Status(
            int code,
            String name,
            String message
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ResultItem(
            String name,
            Code code,
            Region region,
            Land land
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Code(
            String id,
            String type,
            String mappingId
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Region(
            Area area0,
            Area area1,
            Area area2,
            Area area3,
            Area area4
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Area(
            String name,
            String alias,
            Coords coords
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Land(
            String type,
            String name,
            String number1,
            String number2,
            Coords coords,
            Addition addition0,
            Addition addition1,
            Addition addition2,
            Addition addition3,
            Addition addition4
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Addition(
            String type,
            String value
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Coords(
            Center center
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Center(
            String crs,
            Double x,
            Double y
    ) {
    }
}
