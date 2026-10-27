package vn.utetra.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Thêm các HTTP security header bổ sung (Spring Security đã tự thêm
 * X-Content-Type-Options, X-Frame-Options, Cache-Control).
 *
 * CSP cố ý để mở script/style từ https: và inline vì các trang JSP đang dùng
 * CDN (Bootstrap, SockJS...) và script inline. Các chỉ thị còn lại vẫn chặn
 * nhúng trang vào iframe, chèn thẻ base/object và gửi form ra ngoài domain.
 *
 * form-action phải liệt kê cổng VNPay: trình duyệt (Chrome/Edge) áp form-action cả lên
 * các bước redirect sau khi gửi form, nên POST /order/place -> 302 sang VNPay sẽ bị chặn
 * (nút quay mãi, không tới trang VNPay) nếu thiếu.
 */
public class SecurityHeadersFilter extends OncePerRequestFilter {

    private static final String CSP =
            "default-src 'self' https: data: blob: 'unsafe-inline'; "
          + "connect-src 'self' ws: wss: https:; "
          + "object-src 'none'; base-uri 'self'; form-action 'self' https://sandbox.vnpayment.vn https://pay.vnpay.vn; frame-ancestors 'none'";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        response.setHeader("Referrer-Policy", "strict-origin-when-cross-origin");
        response.setHeader("Permissions-Policy", "camera=(), microphone=(), geolocation=(self), payment=()");
        response.setHeader("Content-Security-Policy", CSP);
        response.setHeader("X-XSS-Protection", "0");
        chain.doFilter(request, response);
    }
}
