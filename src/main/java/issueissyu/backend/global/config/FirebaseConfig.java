package issueissyu.backend.global.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class FirebaseConfig {

    private static final String SERVICE_ACCOUNT_RESOURCE = "firebase-service-account.json";

    private final ObjectMapper objectMapper;

    public FirebaseConfig(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    public void init() {
        try (InputStream serviceAccount =
                getClass().getClassLoader().getResourceAsStream(SERVICE_ACCOUNT_RESOURCE)) {
            if (serviceAccount == null) {
                log.warn(
                        "{} not found in classpath; FCM will be disabled. "
                                + "Download a Service Account key from Firebase Console "
                                + "(Project settings > Service accounts > Generate new private key).",
                        SERVICE_ACCOUNT_RESOURCE);
                return;
            }

            byte[] credentialsBytes = serviceAccount.readAllBytes();
            JsonNode credentialsJson = objectMapper.readTree(credentialsBytes);
            if (!credentialsJson.has("type")
                    || !"service_account".equals(credentialsJson.get("type").asText())) {
                log.error(
                        "Invalid Firebase credentials file: '{}' must be a Service Account JSON "
                                + "with \"type\": \"service_account\". "
                                + "google-services.json (Android/iOS client config) cannot be used on the server.",
                        SERVICE_ACCOUNT_RESOURCE);
                return;
            }

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(new ByteArrayInputStream(credentialsBytes)))
                    .build();
            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
                log.info("Firebase Admin SDK initialized.");
            }
        } catch (Exception e) {
            log.error("Firebase initialization failed: {}", e.getMessage(), e);
        }
    }
}
