package issueissyu.backend.domain.collection.dto.req;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CollectionBookmarkUpdateReqDTO {

    @NotNull
    private Boolean isBookmarked;
}
