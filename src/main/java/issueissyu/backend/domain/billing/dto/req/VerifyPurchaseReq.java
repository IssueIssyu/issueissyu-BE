package issueissyu.backend.domain.billing.dto.req;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class VerifyPurchaseReq {

    @NotBlank
    @JsonProperty("productId")
    private String productId;

    @NotBlank
    @JsonProperty("purchaseToken")
    private String purchaseToken;
}
