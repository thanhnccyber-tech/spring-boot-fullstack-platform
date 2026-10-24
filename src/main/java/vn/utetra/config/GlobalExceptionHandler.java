package vn.utetra.config;

import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.TypeMismatchException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.ModelAndView;

import java.util.NoSuchElementException;

/**
 * Xử lý lỗi tập trung: người dùng chỉ thấy trang lỗi thân thiện,
 * không bao giờ thấy stack trace hay nội dung lỗi SQL (chống lộ thông tin hệ thống).
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(NoSuchElementException.class)
    public ModelAndView notFound(HttpServletResponse response) {
        return view(response, 404, null);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ModelAndView tooLarge(HttpServletResponse response) {
        return view(response, 413, "Tệp tải lên quá lớn.");
    }

    @ExceptionHandler({TypeMismatchException.class, IllegalArgumentException.class})
    public ModelAndView badRequest(Exception e, HttpServletResponse response) {
        // IllegalArgumentException do ứng dụng ném ra mang câu chữ an toàn; lỗi kiểu dữ liệu thì không hiển thị
        String msg = (e instanceof IllegalArgumentException && e.getClass() == IllegalArgumentException.class)
                ? e.getMessage() : null;
        return view(response, 400, msg);
    }

    @ExceptionHandler(Exception.class)
    public ModelAndView other(Exception e, HttpServletResponse response) throws Exception {
        // Để Spring Security và Spring MVC tự xử lý các lỗi 401/403/404/405/400 chuẩn
        if (e instanceof AccessDeniedException || e instanceof AuthenticationException
                || e instanceof ErrorResponse) {
            throw e;
        }
        log.error("Lỗi không mong đợi", e);
        return view(response, 500, null);
    }

    private ModelAndView view(HttpServletResponse response, int status, String message) {
        response.setStatus(status);
        ModelAndView mv = new ModelAndView("error");
        mv.addObject("status", status);
        if (message != null) mv.addObject("userMessage", message);
        return mv;
    }
}
