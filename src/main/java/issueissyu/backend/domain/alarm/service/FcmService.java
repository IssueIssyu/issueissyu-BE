package issueissyu.backend.domain.alarm.service;

import com.google.api.core.ApiFuture;
import com.google.api.core.ApiFutureCallback;
import com.google.api.core.ApiFutures;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.firebase.messaging.BatchResponse;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import com.google.firebase.messaging.SendResponse;
import issueissyu.backend.domain.alarm.dto.FcmNotificationPayload;
import issueissyu.backend.global.config.AsyncConfig;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class FcmService {

    private static final int FCM_BATCH_SIZE = 500;
    private static final long SEND_TIMEOUT_SECONDS = 30;

    // 단건 전송 (API 응답용). 내부적으로 sendAsync 후 결과 대기
    public String sendNotification(String targetToken, String title, String body, Map<String, String> data) {
        try {
            return sendNotificationAsync(targetToken, title, body, data)
                    .get(SEND_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.error("FCM send failed token={}: {}", targetToken, e.getMessage(), e);
            throw new RuntimeException("FCM send failed: " + e.getMessage(), e);
        }
    }

    public CompletableFuture<String> sendNotificationAsync(
            String targetToken, String title, String body, Map<String, String> data) {
        Message message = buildMessage(targetToken, title, body, data);
        ApiFuture<String> apiFuture = FirebaseMessaging.getInstance().sendAsync(message);
        return toCompletableFuture(apiFuture);
    }

    // 다건 일괄 전송 (동기). Firebase sendEach 사용, 500건 단위 분할
    public void sendNotificationsBatch(List<FcmNotificationPayload> payloads) {
        if (payloads == null || payloads.isEmpty()) {
            return;
        }

        for (int offset = 0; offset < payloads.size(); offset += FCM_BATCH_SIZE) {
            int end = Math.min(offset + FCM_BATCH_SIZE, payloads.size());
            List<FcmNotificationPayload> chunk = payloads.subList(offset, end);
            List<Message> messages = chunk.stream().map(this::toMessage).toList();

            try {
                BatchResponse response = FirebaseMessaging.getInstance().sendEach(messages);
                logBatchResult(response, chunk.size());
            } catch (Exception e) {
                log.error("FCM batch send failed offset={} size={}: {}", offset, chunk.size(), e.getMessage(), e);
            }
        }
    }

    // 다건 일괄 전송 (비동기, fire-and-forget) — sendEachAsync 사용
    @Async(AsyncConfig.ALARM_TASK_EXECUTOR)
    public void sendNotificationsBatchAsync(List<FcmNotificationPayload> payloads) {
        if (payloads == null || payloads.isEmpty()) {
            return;
        }

        for (int offset = 0; offset < payloads.size(); offset += FCM_BATCH_SIZE) {
            int end = Math.min(offset + FCM_BATCH_SIZE, payloads.size());
            List<FcmNotificationPayload> chunk = payloads.subList(offset, end);
            List<Message> messages = chunk.stream().map(this::toMessage).toList();

            ApiFuture<BatchResponse> apiFuture = FirebaseMessaging.getInstance().sendEachAsync(messages);
            int finalOffset = offset;
            toCompletableFuture(apiFuture)
                    .thenAccept(response -> logBatchResult(response, chunk.size()))
                    .exceptionally(ex -> {
                        log.error(
                                "FCM async batch send failed offset={} size={}: {}",
                                finalOffset,
                                chunk.size(),
                                ex.getMessage());
                        return null;
                    });
        }
    }

    private Message buildMessage(String targetToken, String title, String body, Map<String, String> data) {
        Message.Builder builder = Message.builder()
                .setToken(targetToken)
                .setNotification(
                        Notification.builder().setTitle(title).setBody(body).build());
        if (data != null && !data.isEmpty()) {
            builder.putAllData(data);
        }
        return builder.build();
    }

    private Message toMessage(FcmNotificationPayload payload) {
        return buildMessage(payload.token(), payload.title(), payload.body(), payload.data());
    }

    private void logBatchResult(BatchResponse response, int chunkSize) {
        log.info(
                "FCM batch sent: success={}, failure={}, total={}",
                response.getSuccessCount(),
                response.getFailureCount(),
                chunkSize);
        if (response.getFailureCount() > 0) {
            List<SendResponse> responses = response.getResponses();
            for (int i = 0; i < responses.size(); i++) {
                SendResponse sendResponse = responses.get(i);
                if (!sendResponse.isSuccessful()) {
                    log.warn(
                            "FCM batch item failed index={}: {}",
                            i,
                            sendResponse.getException() != null
                                    ? sendResponse.getException().getMessage()
                                    : "unknown");
                }
            }
        }
    }

    private static <T> CompletableFuture<T> toCompletableFuture(ApiFuture<T> apiFuture) {
        CompletableFuture<T> future = new CompletableFuture<>();
        ApiFutures.addCallback(
                apiFuture,
                new ApiFutureCallback<T>() {
                    @Override
                    public void onSuccess(T result) {
                        future.complete(result);
                    }

                    @Override
                    public void onFailure(Throwable t) {
                        future.completeExceptionally(t);
                    }
                },
                MoreExecutors.directExecutor());
        return future;
    }
}
