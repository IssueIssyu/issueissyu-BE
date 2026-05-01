package issueissyu.backend.domain.location.dto.res;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.postgresql.geometric.PGpoint;

import java.util.List;
import java.util.Optional;

@JsonIgnoreProperties(ignoreUnknown = true)
public record NaverGeocodeResDTO(
        String status,
        Meta meta,
        List<Address> addresses,
        String errorMessage
) {
    public Optional<PGpoint> firstPoint() {
        if (addresses == null || addresses.isEmpty()) {
            return Optional.empty();
        }

        Address first = addresses.get(0);
        try {
            return Optional.of(new PGpoint(Double.parseDouble(first.x()), Double.parseDouble(first.y())));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Meta(
            int totalCount,
            int page,
            int count
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Address(
            String roadAddress,
            String jibunAddress,
            String englishAddress,
            List<AddressElement> addressElements,
            String x,
            String y,
            Double distance
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record AddressElement(
            List<String> types,
            String longName,
            String shortName,
            String code
    ) {
    }
}
