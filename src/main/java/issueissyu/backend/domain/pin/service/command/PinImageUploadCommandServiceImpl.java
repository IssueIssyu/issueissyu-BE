package issueissyu.backend.domain.pin.service.command;

import issueissyu.backend.domain.pin.exception.PinException;
import issueissyu.backend.domain.pin.exception.code.PinErrorCode;
import issueissyu.backend.utils.S3.S3Dto;
import issueissyu.backend.utils.S3.S3Utils;
import issueissyu.backend.utils.exception.UtilException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class PinImageUploadCommandServiceImpl implements PinImageUploadCommandService {

    private static final int MAX_FILES = 5;
    private static final long MAX_TOTAL_BYTES = 50L * 1024 * 1024;
    private static final String S3_DIR = "pins";
    private static final Set<String> ALLOWED_EXT =
            Set.of(
                    ".jpg",
                    ".jpeg",
                    ".png",
                    ".gif",
                    ".webp",
                    ".webq",
                    ".heic",
                    ".heif",
                    ".avif");
    private static final Set<String> ALLOWED_CONTENT_TYPES =
            Set.of(
                    "image/jpeg",
                    "image/png",
                    "image/gif",
                    "image/webp",
                    "image/heic",
                    "image/heif",
                    "image/heic-sequence",
                    "image/heif-sequence",
                    "image/avif");

    private final S3Utils s3Utils;

    @Override
    public List<String> uploadPinImages(List<MultipartFile> photos) {
        if (photos == null || photos.isEmpty()) {
            throw PinException.of(PinErrorCode.PIN_IMAGE_400_2);
        }
        if (photos.size() > MAX_FILES) {
            throw PinException.of(PinErrorCode.PIN_IMAGE_400_3);
        }
        long total = photos.stream().mapToLong(MultipartFile::getSize).sum();
        if (total > MAX_TOTAL_BYTES) {
            throw PinException.of(PinErrorCode.PIN_IMAGE_400_1);
        }

        List<String> uploadedKeys = new ArrayList<>();
        List<String> urls = new ArrayList<>();
        try {
            for (MultipartFile file : photos) {
                validateImageFile(file);
                S3Dto dto = s3Utils.uploadMultipartUnderDirectory(file, S3_DIR);
                uploadedKeys.add(dto.getKey());
                urls.add(dto.getUrl());
            }
            return urls;
        } catch (PinException e) {
            rollback(uploadedKeys);
            throw e;
        } catch (UtilException e) {
            rollback(uploadedKeys);
            throw PinException.of(PinErrorCode.PIN_IMAGE_400_2);
        } catch (Exception e) {
            log.warn("pin image upload failed: {}", e.getMessage());
            rollback(uploadedKeys);
            throw PinException.of(PinErrorCode.PIN_IMAGE_400_2);
        }
    }

    private void rollback(List<String> uploadedKeys) {
        for (String key : uploadedKeys) {
            try {
                s3Utils.deleteFile(key);
            } catch (Exception ex) {
                log.warn("rollback delete failed for key={}: {}", key, ex.getMessage());
            }
        }
    }

    private static void validateImageFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw PinException.of(PinErrorCode.PIN_IMAGE_400_2);
        }
        String original = file.getOriginalFilename();
        if (!StringUtils.hasText(original)) {
            throw PinException.of(PinErrorCode.PIN_IMAGE_400_2);
        }
        String lower = original.toLowerCase(Locale.ROOT);
        String ext =
                lower.contains(".") ? lower.substring(lower.lastIndexOf('.')) : "";
        String contentType = file.getContentType();
        String normalizedContentType =
                contentType == null ? "" : contentType.toLowerCase(Locale.ROOT);

        boolean allowedByExt = ALLOWED_EXT.contains(ext);
        boolean allowedByContentType = ALLOWED_CONTENT_TYPES.contains(normalizedContentType);

        if (!allowedByExt && !allowedByContentType) {
            throw PinException.of(PinErrorCode.PIN_IMAGE_400_2);
        }
    }
}
