package vn.utetra.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Locale;
import java.util.Map;

@Service
public class CloudinaryService {

    @Autowired
    private Cloudinary cloudinary;

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
        try {
            Map<?, ?> result = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "folder", "utetra/" + folder,
                            "resource_type", resourceType,
                            "quality", "auto"
                    ));
            return (String) result.get("secure_url");
        } catch (IOException e) {
            throw new RuntimeException("Upload Cloudinary thất bại: " + e.getMessage(), e);
        }
    }
}