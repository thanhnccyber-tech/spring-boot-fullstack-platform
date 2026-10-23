package vn.utetra.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

@Service
public class FileValidator {

    public static final long MAX_IMAGE_SIZE = 5L * 1024 * 1024;

    public static final Set<String> ALLOWED_IMAGE_MIME = Set.of(
            "image/jpeg",
            "image/jpg",
            "image/png",
            "image/gif",
            "image/webp"
    );

    public void validateImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return;
        }
        if (file.getSize() > MAX_IMAGE_SIZE) {
            throw new IllegalArgumentException(
                    "Ảnh không được vượt quá " + (MAX_IMAGE_SIZE / (1024 * 1024)) + " MB.");
        }
        if (file.getSize() < 16) {
            throw new IllegalArgumentException("Tệp ảnh không hợp lệ.");
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_IMAGE_MIME.contains(contentType.toLowerCase())) {
            throw new IllegalArgumentException(
                    "Chỉ chấp nhận ảnh định dạng JPG, PNG, GIF hoặc WEBP.");
        }
        if (!hasValidImageSignature(file)) {
            throw new IllegalArgumentException(
                    "Nội dung tệp không phải là ảnh hợp lệ.");
        }
    }

    private boolean hasValidImageSignature(MultipartFile file) {
        try (InputStream in = file.getInputStream()) {
            byte[] header = new byte[12];
            int read = in.read(header);
            if (read < 3) return false;

            if ((header[0] & 0xFF) == 0xFF
                    && (header[1] & 0xFF) == 0xD8
                    && (header[2] & 0xFF) == 0xFF) {
                return true;
            }
            if (read >= 8
                    && (header[0] & 0xFF) == 0x89
                    && header[1] == 'P' && header[2] == 'N' && header[3] == 'G'
                    && (header[4] & 0xFF) == 0x0D
                    && (header[5] & 0xFF) == 0x0A
                    && (header[6] & 0xFF) == 0x1A
                    && (header[7] & 0xFF) == 0x0A) {
                return true;
            }
            if (read >= 4
                    && header[0] == 'G' && header[1] == 'I'
                    && header[2] == 'F' && header[3] == '8') {
                return true;
            }
            if (read >= 12
                    && header[0] == 'R' && header[1] == 'I'
                    && header[2] == 'F' && header[3] == 'F'
                    && header[8] == 'W' && header[9] == 'E'
                    && header[10] == 'B' && header[11] == 'P') {
                return true;
            }
            return false;
        } catch (IOException e) {
            throw new IllegalArgumentException(
                    "Không đọc được nội dung tệp: " + e.getMessage());
        }
    }
}