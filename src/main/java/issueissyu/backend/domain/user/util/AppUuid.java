package issueissyu.backend.domain.user.util;

import java.util.UUID;

public final class AppUuid {

    private AppUuid() {
    }

    public static String newUid() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
