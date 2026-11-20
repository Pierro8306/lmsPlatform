package pierro.lmsPlatform.service.learnning;

import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import pierro.lmsPlatform.security.exception.ApiException;
import pierro.lmsPlatform.security.exception.BadRequestException;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import static java.util.Map.entry;

@Slf4j
@Service
public class CloudStorageService {

    public enum Kind { VIDEO, ATTACHMENT }

    public record StoredObject(String key, String contentType, long size) {}

    // Allow-list by extension. The Content-Type comes from here, never from the client.
    // html/svg/js are deliberately absent: served inline they would run script on the storage origin.
    private static final Map<String, String> VIDEO_TYPES = Map.of(
            "mp4", "video/mp4",
            "m4v", "video/mp4",
            "webm", "video/webm",
            "ogv", "video/ogg");

    private static final Map<String, String> ATTACHMENT_TYPES = Map.ofEntries(
            entry("pdf", "application/pdf"),
            entry("png", "image/png"),
            entry("jpg", "image/jpeg"),
            entry("jpeg", "image/jpeg"),
            entry("gif", "image/gif"),
            entry("webp", "image/webp"),
            entry("txt", "text/plain; charset=utf-8"),
            entry("csv", "text/csv; charset=utf-8"),
            entry("doc", "application/msword"),
            entry("docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document"),
            entry("xls", "application/vnd.ms-excel"),
            entry("xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"),
            entry("ppt", "application/vnd.ms-powerpoint"),
            entry("pptx", "application/vnd.openxmlformats-officedocument.presentationml.presentation"));

    private final String endpoint;
    private final String bucket;
    private final String region;
    private final String accessKey;
    private final String secretKey;
    private final boolean pathStyle;

    private volatile S3Client s3;

    public CloudStorageService(@Value("${CLOUD_STORAGE_ENDPOINT:}") String endpoint,
                               @Value("${CLOUD_STORAGE_BUCKET:}") String bucket,
                               @Value("${CLOUD_STORAGE_REGION:us-east-1}") String region,
                               @Value("${CLOUD_STORAGE_ACCESS_KEY:}") String accessKey,
                               @Value("${CLOUD_STORAGE_SECRET_KEY:}") String secretKey,
                               @Value("${CLOUD_STORAGE_PATH_STYLE:false}") boolean pathStyle) {
        this.endpoint = endpoint;
        this.bucket = bucket;
        this.region = region;
        this.accessKey = accessKey;
        this.secretKey = secretKey;
        this.pathStyle = pathStyle;
    }

    public StoredObject upload(MultipartFile file, Kind kind) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Tệp tải lên đang trống");
        }
        String original = baseName(file.getOriginalFilename());
        String ext = extensionOf(original);
        String contentType = (kind == Kind.VIDEO ? VIDEO_TYPES : ATTACHMENT_TYPES).get(ext);
        if (contentType == null) {
            throw new BadRequestException("Định dạng tệp không được hỗ trợ: ." + ext);
        }

        String key = "lessons/" + (kind == Kind.VIDEO ? "video" : "attachments")
                + "/" + UUID.randomUUID() + "/" + safeName(original, ext);

        S3Client client = client();
        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType(contentType)
                .contentDisposition(inlineDisposition(original, key))
                .cacheControl("public, max-age=31536000, immutable")   // key is unique per upload
                .build();

        try (InputStream in = file.getInputStream()) {
            client.putObject(request, RequestBody.fromInputStream(in, file.getSize()));
        } catch (IOException | SdkException e) {
            log.error("Cloud storage upload failed for key {}", key, e);
            throw new ApiException(HttpStatus.BAD_GATEWAY, "Không thể tải tệp lên kho lưu trữ");
        }
        return new StoredObject(key, contentType, file.getSize());
    }

    // ---------------------------------------------------------------- helpers

    private S3Client client() {
        S3Client c = s3;
        if (c != null) return c;
        synchronized (this) {
            if (s3 == null) s3 = buildClient();
            return s3;
        }
    }

    private S3Client buildClient() {
        if (!StringUtils.hasText(bucket)) {
            throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE, "Kho lưu trữ chưa được cấu hình");
        }
        S3ClientBuilder b = S3Client.builder()
                .region(Region.of(region))
                .forcePathStyle(pathStyle);
        if (StringUtils.hasText(endpoint)) b.endpointOverride(URI.create(endpoint.trim()));
        if (StringUtils.hasText(accessKey) && StringUtils.hasText(secretKey)) {
            b.credentialsProvider(StaticCredentialsProvider.create(
                    AwsBasicCredentials.create(accessKey.trim(), secretKey.trim())));
        } else {
            b.credentialsProvider(DefaultCredentialsProvider.create());
        }
        return b.build();
    }

    @PreDestroy
    void close() {
        S3Client c = s3;
        if (c != null) c.close();
    }

    private static String inlineDisposition(String original, String key) {
        String ascii = key.substring(key.lastIndexOf('/') + 1);
        String utf8 = URLEncoder.encode(original, StandardCharsets.UTF_8).replace("+", "%20");
        return "inline; filename=\"" + ascii + "\"; filename*=UTF-8''" + utf8;
    }

    private static String baseName(String name) {
        if (!StringUtils.hasText(name)) return "file";
        String n = name.replace('\\', '/');
        return n.substring(n.lastIndexOf('/') + 1).trim();
    }

    private static String extensionOf(String name) {
        int dot = name.lastIndexOf('.');
        return dot < 0 ? "" : name.substring(dot + 1).toLowerCase(Locale.ROOT);
    }

    private static String safeName(String original, String ext) {
        String stem = original.contains(".") ? original.substring(0, original.lastIndexOf('.')) : original;
        stem = Normalizer.normalize(stem.replace('đ', 'd').replace('Đ', 'D'), Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "")
                .replaceAll("[^A-Za-z0-9_-]+", "_")
                .replaceAll("^_+|_+$", "");
        if (stem.length() > 80) stem = stem.substring(0, 80);
        if (stem.isEmpty()) stem = "file";
        return stem + "." + ext;
    }
}
