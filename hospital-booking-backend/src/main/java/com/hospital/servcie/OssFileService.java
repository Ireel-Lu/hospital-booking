package com.hospital.servcie;

import com.aliyun.oss.ClientBuilderConfiguration;
import com.aliyun.oss.ClientException;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.OSSException;
import com.aliyun.oss.model.ObjectMetadata;
import com.hospital.config.OssProperties;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OssFileService {

    private static final long AVATAR_MAX_SIZE = 2L * 1024 * 1024;
    private static final long CASE_IMAGE_MAX_SIZE = 5L * 1024 * 1024;

    private final OssProperties properties;
    private volatile OSS ossClient;

    public String uploadAvatar(MultipartFile file, Long userId) {
        return uploadImage(file, properties.getAvatarDir(), userId, AVATAR_MAX_SIZE, "头像");
    }

    public String uploadCaseImage(MultipartFile file, Long recordId) {
        return uploadImage(file, properties.getCaseDir(), recordId, CASE_IMAGE_MAX_SIZE, "病历图片");
    }

    private String uploadImage(MultipartFile file, String directory, Long ownerId,
                               long maxSize, String fileLabel) {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("请选择要上传的图片");
        }
        if (file.getSize() > maxSize) {
            throw new RuntimeException(fileLabel + "不能超过 " + (maxSize / 1024 / 1024) + "MB");
        }

        String extension = getExtension(file.getOriginalFilename());
        byte[] bytes;
        try {
            bytes = file.getBytes();
        } catch (IOException e) {
            throw new RuntimeException("读取上传文件失败", e);
        }
        ImageFormat format = validateImage(bytes, extension, file.getContentType());

        String objectKey = normalizeDirectory(directory) + "/" + ownerId + "/"
                + UUID.randomUUID().toString().replace("-", "") + format.extension;
        putWithRetry(objectKey, bytes, format.mimeTypes.get(0));
        return buildUrl(objectKey);
    }

    private ImageFormat validateImage(byte[] bytes, String extension, String contentType) {
        ImageFormat format = detectFormat(bytes);
        if (format == null) {
            throw new RuntimeException("文件内容不是有效的 PNG/JPG/JPEG/GIF/WebP 图片");
        }
        if (!format.extensions.contains(extension)) {
            throw new RuntimeException("图片扩展名与实际内容不一致");
        }
        String normalizedType = contentType == null ? "" : contentType.toLowerCase(Locale.ROOT);
        if (!format.mimeTypes.contains(normalizedType)) {
            throw new RuntimeException("图片 Content-Type 与实际内容不一致");
        }

        // JDK 自带解码器可校验 PNG/JPEG/GIF 的完整内容；WebP 使用文件头校验。
        if (format != ImageFormat.WEBP) {
            try {
                if (ImageIO.read(new ByteArrayInputStream(bytes)) == null) {
                    throw new RuntimeException("图片内容损坏或格式不受支持");
                }
            } catch (IOException e) {
                throw new RuntimeException("图片内容校验失败", e);
            }
        }
        return format;
    }

    private ImageFormat detectFormat(byte[] bytes) {
        if (bytes.length >= 8
                && (bytes[0] & 0xff) == 0x89 && bytes[1] == 0x50 && bytes[2] == 0x4e && bytes[3] == 0x47
                && bytes[4] == 0x0d && bytes[5] == 0x0a && bytes[6] == 0x1a && bytes[7] == 0x0a) {
            return ImageFormat.PNG;
        }
        if (bytes.length >= 3
                && (bytes[0] & 0xff) == 0xff && (bytes[1] & 0xff) == 0xd8 && (bytes[2] & 0xff) == 0xff) {
            return ImageFormat.JPEG;
        }
        if (bytes.length >= 6) {
            String header = new String(bytes, 0, 6, java.nio.charset.StandardCharsets.US_ASCII);
            if ("GIF87a".equals(header) || "GIF89a".equals(header)) {
                return ImageFormat.GIF;
            }
        }
        if (bytes.length >= 12
                && bytes[0] == 'R' && bytes[1] == 'I' && bytes[2] == 'F' && bytes[3] == 'F'
                && bytes[8] == 'W' && bytes[9] == 'E' && bytes[10] == 'B' && bytes[11] == 'P') {
            return ImageFormat.WEBP;
        }
        return null;
    }

    private String getExtension(String filename) {
        if (!StringUtils.hasText(filename) || !filename.contains(".")) {
            throw new RuntimeException("图片必须带有 png/jpg/jpeg/gif/webp 扩展名");
        }
        String extension = filename.substring(filename.lastIndexOf('.')).toLowerCase(Locale.ROOT);
        if (!ImageFormat.ALL_EXTENSIONS.contains(extension)) {
            throw new RuntimeException("仅支持 png/jpg/jpeg/gif/webp 格式");
        }
        return extension;
    }

    private void putWithRetry(String objectKey, byte[] bytes, String contentType) {
        int maxAttempts = Math.max(1, properties.getMaxAttempts());
        RuntimeException lastException = null;

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                ObjectMetadata metadata = new ObjectMetadata();
                metadata.setContentLength(bytes.length);
                metadata.setContentType(contentType);
                getOssClient().putObject(properties.getBucket(), objectKey,
                        new ByteArrayInputStream(bytes), metadata);
                return;
            } catch (OSSException e) {
                if (!isRetryable(e)) {
                    log.error("OSS 上传被拒绝，requestId={}, code={}, message={}",
                            e.getRequestId(), e.getErrorCode(), e.getErrorMessage());
                    throw new RuntimeException("OSS 上传失败：" + e.getErrorCode()
                            + (StringUtils.hasText(e.getErrorMessage()) ? "，" + e.getErrorMessage() : ""), e);
                }
                lastException = e;
                log.warn("OSS 上传服务异常，objectKey={}, attempt={}/{}, code={}",
                        objectKey, attempt, maxAttempts, e.getErrorCode());
            } catch (ClientException e) {
                lastException = e;
                log.warn("OSS 上传网络异常，objectKey={}, attempt={}/{}, message={}",
                        objectKey, attempt, maxAttempts, e.getMessage());
            }

            if (attempt < maxAttempts) {
                sleepBeforeRetry(attempt);
            }
        }
        throw new RuntimeException("OSS 上传失败，已自动重试 " + (maxAttempts - 1) + " 次，请稍后重试", lastException);
    }

    private boolean isRetryable(OSSException e) {
        String errorCode = e.getErrorCode();
        return errorCode != null && java.util.Set.of(
                "InternalError", "ServiceUnavailable", "RequestTimeout",
                "SlowDown", "Throttling", "TooManyRequests"
        ).contains(errorCode);
    }

    private void sleepBeforeRetry(int attempt) {
        try {
            Thread.sleep(Math.max(0, properties.getRetryIntervalMs()) * attempt);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("OSS 上传重试已中断", e);
        }
    }

    private OSS getOssClient() {
        if (ossClient == null) {
            synchronized (this) {
                if (ossClient == null) {
                    if (!StringUtils.hasText(properties.getAccessKeyId())
                            || !StringUtils.hasText(properties.getAccessKeySecret())) {
                        throw new RuntimeException("未配置 OSS AccessKey（aliyun.oss.access-key-id / access-key-secret）");
                    }
                    ClientBuilderConfiguration configuration = new ClientBuilderConfiguration();
                    configuration.setMaxErrorRetry(0);
                    configuration.setConnectionTimeout(properties.getConnectionTimeoutMs());
                    configuration.setSocketTimeout(properties.getSocketTimeoutMs());
                    ossClient = new OSSClientBuilder().build(properties.getEndpoint(),
                            properties.getAccessKeyId(), properties.getAccessKeySecret(), configuration);
                }
            }
        }
        return ossClient;
    }

    private String buildUrl(String objectKey) {
        String host = properties.getEndpoint().trim()
                .replaceFirst("^https?://", "").replaceAll("/+$", "");
        return "https://" + properties.getBucket() + "." + host + "/" + objectKey;
    }

    /**
     * 数据库保存固定 OSS 地址，返回前端时生成限时签名地址，兼容私有 Bucket。
     * 非本 Bucket 地址（例如历史本地上传路径）原样返回。
     */
    public String toSignedUrl(String url) {
        String objectKey = extractObjectKey(url);
        if (objectKey == null) {
            return url;
        }
        long expireMillis = Math.max(60, properties.getSignedUrlExpireSeconds()) * 1000;
        Date expiration = new Date(System.currentTimeMillis() + expireMillis);
        return getOssClient().generatePresignedUrl(properties.getBucket(), objectKey, expiration).toString();
    }

    private String extractObjectKey(String url) {
        if (!StringUtils.hasText(url)) {
            return null;
        }
        try {
            URI uri = URI.create(url);
            String endpointHost = URI.create(properties.getEndpoint()).getHost();
            String expectedHost = properties.getBucket() + "." + endpointHost;
            if (!expectedHost.equalsIgnoreCase(uri.getHost())) {
                return null;
            }
            String path = uri.getPath();
            if (!StringUtils.hasText(path) || "/".equals(path)) {
                return null;
            }
            return path.startsWith("/") ? path.substring(1) : path;
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private String normalizeDirectory(String directory) {
        String value = StringUtils.hasText(directory) ? directory.trim() : "uploads";
        return value.replaceAll("^/+|/+$", "");
    }

    public void deleteByUrl(String url) {
        String objectKey = extractObjectKey(url);
        if (objectKey == null) {
            return;
        }
        try {
            getOssClient().deleteObject(properties.getBucket(), objectKey);
        } catch (Exception e) {
            log.warn("清理未关联病历的 OSS 图片失败：{}", e.getMessage());
        }
    }

    @PreDestroy
    public void shutdown() {
        if (ossClient != null) {
            ossClient.shutdown();
        }
    }

    private enum ImageFormat {
        PNG(List.of(".png"), List.of("image/png"), ".png"),
        JPEG(List.of(".jpg", ".jpeg"), List.of("image/jpeg", "image/jpg"), ".jpg"),
        GIF(List.of(".gif"), List.of("image/gif"), ".gif"),
        WEBP(List.of(".webp"), List.of("image/webp"), ".webp");

        private static final List<String> ALL_EXTENSIONS = List.of(".png", ".jpg", ".jpeg", ".gif", ".webp");
        private final List<String> extensions;
        private final List<String> mimeTypes;
        private final String extension;

        ImageFormat(List<String> extensions, List<String> mimeTypes, String extension) {
            this.extensions = extensions;
            this.mimeTypes = mimeTypes;
            this.extension = extension;
        }
    }
}
