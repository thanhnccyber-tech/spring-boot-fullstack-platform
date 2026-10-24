package vn.utetra.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Chống rò rỉ thông tin qua thông báo lỗi.
 * Lỗi do chính ứng dụng ném ra (RuntimeException / IllegalArgumentException / IllegalStateException
 * với câu chữ tiếng Việt) được hiển thị cho người dùng. Mọi lỗi khác (SQL, IO, thư viện...) chỉ được
 * ghi vào log, người dùng chỉ thấy thông báo chung.
 */
public final class ErrorMessages {

    private static final Logger log = LoggerFactory.getLogger(ErrorMessages.class);

    public static final String GENERIC = "Đã xảy ra lỗi, vui lòng thử lại sau.";

    private ErrorMessages() {}

    public static String safe(Throwable e) {
        Class<?> c = e.getClass();
        boolean own = c == RuntimeException.class
                || c == IllegalArgumentException.class
                || c == IllegalStateException.class;
        if (own && e.getMessage() != null && !e.getMessage().isBlank()) {
            return e.getMessage();
        }
        log.error("Lỗi không mong đợi", e);
        return GENERIC;
    }
}
