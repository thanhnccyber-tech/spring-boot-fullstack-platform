package vn.utetra.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Locale;
import java.util.Map;

@Service
public class CloudinaryService {

    private static final String DEFAULT_IMAGE_URL = "https://res.cloudinary.com/demo/image/upload/sample.jpg";

    @Autowired
    private Cloudinary cloudinary;

    @Value("${cloudinary.cloud-name:}")
    private String cloudName;

    @Value("${cloudinary.api-key:}")
    private String apiKey;

    @Value("${cloudinary.api-secret:}")
    private String apiSecret;

    public String upload(MultipartFile file, String folder) {
        return upload(file, folder, "image");
    }

    public String uploadReviewMedia(MultipartFile file, String folder) {
        String resourceType = file.getContentType() != null
                && file.getContentType().toLowerCase(Locale.ROOT).startsWith("video/") ? "video" : "image";
        return upload(file, folder, resourceType);
    }

    private String upload(MultipartFile file, String folder, String resourceType) {
        if (file == null || file.isEmpty()) return null;
        if (!hasConfiguredCredentials()) {
            return DEFAULT_IMAGE_URL;
        }

        try {
            Map<?, ?> result = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "folder", "utetra/" + folder,
                            "resource_type", resourceType,
                            "quality", "auto"
                    ));
            return (String) result.get("secure_url");
        } catch (IOException | RuntimeException e) {
            return DEFAULT_IMAGE_URL;
        }
    }

    private boolean hasConfiguredCredentials() {
        return StringUtils.hasText(cloudName)
                && StringUtils.hasText(apiKey)
                && StringUtils.hasText(apiSecret);
    }
}