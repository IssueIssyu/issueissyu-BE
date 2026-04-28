package issueissyu.backend.domain.auth.service;

import issueissyu.backend.domain.auth.exception.AuthException;
import issueissyu.backend.domain.auth.exception.code.AuthErrorCode;
import issueissyu.backend.utils.redis.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import net.nurigo.sdk.NurigoApp;
import net.nurigo.sdk.message.model.Message;
import net.nurigo.sdk.message.request.SingleMessageSendingRequest;
import net.nurigo.sdk.message.response.SingleMessageSentResponse;
import net.nurigo.sdk.message.service.DefaultMessageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;

@Slf4j
@Service
public class SmsService {

    private static final String SMS_CODE_KEY_PREFIX = "SMS_CODE:";
    private static final long SMS_CODE_TTL_SECONDS = 300; // 5분
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final DefaultMessageService messageService;
    private final String fromPhone;
    private final RedisUtil redisUtil;

    public SmsService(
            @Value("${solapi.api-key}") String apiKey,
            @Value("${solapi.api-secret-key}") String apiSecretKey,
            @Value("${solapi.from-phone}") String fromPhone,
            RedisUtil redisUtil) {
        this.messageService = NurigoApp.INSTANCE.initialize(apiKey, apiSecretKey, "https://api.solapi.com");
        this.fromPhone = fromPhone;
        this.redisUtil = redisUtil;
    }

    // 인증번호 SMS 발송 (Redis에 저장)
    public void sendVerificationCode(String phone) {
        String code = generateCode();
        String redisKey = SMS_CODE_KEY_PREFIX + phone;

        try {
            Message message = new Message();
            message.setFrom(fromPhone.replaceAll("-", ""));
            message.setTo(phone.replaceAll("-", ""));
            message.setText("이슈있슈 SMS 문자 인증.\n인증번호는 [" + code + "] 입니다.");

            SingleMessageSentResponse response =
                    messageService.sendOne(new SingleMessageSendingRequest(message));

            log.debug("SMS 발송 성공: phone={}, messageId={}", phone, response.getMessageId());
        } catch (Exception e) {
            log.error("SMS 발송 실패: phone={}", phone, e);
            throw AuthException.of(AuthErrorCode.PHONE_SEND_FAILED);
        }

        redisUtil.setDataExpire(redisKey, code, SMS_CODE_TTL_SECONDS);
    }

    // 인증번호 검증
    public boolean verifyCode(String phone, String inputCode) {
        String redisKey = SMS_CODE_KEY_PREFIX + phone;
        String storedCode = redisUtil.getData(redisKey);

        if (storedCode == null || !storedCode.equals(inputCode)) {
            return false;
        }

        redisUtil.deleteData(redisKey);
        return true;
    }

    private String generateCode() {
        int code = 100000 + SECURE_RANDOM.nextInt(900000);
        return String.valueOf(code);
    }
}
