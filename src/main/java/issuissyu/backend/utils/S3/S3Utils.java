package issuissyu.backend.utils.S3;

import issuissyu.backend.global.config.AmazonConfig;
import issuissyu.backend.utils.exception.UtilException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.InputStream;
import java.util.UUID;

import static issuissyu.backend.utils.exception.UtilException.Reason.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class S3Utils {

    private final S3Client s3Client;
    private final AmazonConfig config;

    // 파일 키 생성 (원본 파일명 그대로 사용)
    private String generateFileKey(String fileName) {
        return UUID.randomUUID() + "-" + fileName;
    }

    private String getUrl(String key) {
        return "https://" + config.getBucket()
                + ".s3." + config.getRegion()
                + ".amazonaws.com/" + key;
    }

    // MultipartFile 그대로 업로드 (범용)
    public S3Dto uploadFile(MultipartFile file) {

        if (file == null || file.isEmpty() || file.getSize() <= 0) {
            throw new UtilException(FILE_EMPTY);
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            throw new UtilException(FILE_EMPTY);
        }

        String key = generateFileKey(originalFilename);

        String contentType = (file.getContentType() != null)
                ? file.getContentType()
                : "application/octet-stream";

        PutObjectRequest req = PutObjectRequest.builder()
                .bucket(config.getBucket())
                .key(key)
                .contentType(contentType)
                .build();

        try (InputStream is = file.getInputStream()) {
            s3Client.putObject(req, RequestBody.fromInputStream(is, file.getSize()));
            return new S3Dto(getUrl(key), key);   // (url, key) 순서 통일
        } catch (SdkClientException e) {
            throw new UtilException(S3_UPLOAD_FAILED, e);
        } catch (Exception e) {
            // IOException 포함
            throw new UtilException(S3_UPLOAD_FAILED, e);
        }
    }

    /** 가공된 바이트 업로드 (프로필 리사이즈 결과 등) Image/ImageUtill 클래스와 조합해서 사용할 것! */
    public S3Dto uploadBytes(byte[] bytes, String fileName, String contentType) {

        if (bytes == null || bytes.length == 0) {
            throw new UtilException(FILE_EMPTY);
        }

        if (fileName == null || fileName.isBlank()) {
            throw new UtilException(FILE_EMPTY);
        }

        String key = generateFileKey(fileName);

        String ct = (contentType != null && !contentType.isBlank())
                ? contentType
                : "application/octet-stream";

        PutObjectRequest req = PutObjectRequest.builder()
                .bucket(config.getBucket())
                .key(key)
                .contentType(ct)
                .build();

        try {
            s3Client.putObject(req, RequestBody.fromBytes(bytes));
            return new S3Dto(getUrl(key), key);   // (url, key) 순서 통일
        } catch (SdkClientException e) {
            throw new UtilException(S3_UPLOAD_FAILED, e);
        } catch (Exception e) {
            throw new UtilException(S3_UPLOAD_FAILED, e);
        }
    }

    public void deleteFile(String key) {

        if (key == null || key.isBlank()) {
            throw new UtilException(S3_DELETE_FAILED);
        }

        try {
            DeleteObjectRequest req = DeleteObjectRequest.builder()
                    .bucket(config.getBucket())
                    .key(key)
                    .build();
            s3Client.deleteObject(req);
            log.info("Deleted file from S3: {}", key);
        } catch (SdkClientException e) {
            log.error("Error occurred while deleting file from S3: {}", key, e);
            throw new UtilException(S3_DELETE_FAILED, e);
        } catch (Exception e) {
            throw new UtilException(S3_DELETE_FAILED, e);
        }
    }
}
