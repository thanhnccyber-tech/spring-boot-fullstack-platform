package vn.utetra.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.net.URI;
import java.util.Set;

/**
 * Chống CSRF cho các request ghi dữ liệu (POST/PUT/DELETE/PATCH).
 *
 * Dự án dùng cookie JWT và đã tắt CSRF token của Spring Security, nên cần kiểm
 * tra header Origin/Referer: nếu request đến từ trang web khác domain thì từ
 * chối. Kết hợp với cookie SameSite=Lax để phòng thủ nhiều lớp.
 *
 * Bỏ qua: request chỉ đọc (GET/HEAD/OPTIONS/TRACE), request mang header
 * Authorization (API, không dùng cookie) và kênh SockJS /ws.
 */
public class OriginCheckFilter extends OncePerRequestFilter {

	private static final Logger log = LoggerFactory.getLogger(OriginCheckFilter.class);
	private static final Set<String> SAFE_METHODS = Set.of("GET", "HEAD", "OPTIONS", "TRACE");

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
			throws ServletException, IOException {

		// Link GET nhưng làm thay đổi dữ liệu (xoá / khôi phục / bật tắt...): cookie
		// SameSite=Lax vẫn được gửi
		// khi bấm link từ trang web khác, nên phải chặn request đến từ nguồn khác
		// domain.
		if ("GET".equals(request.getMethod()) && isStateChangingGet(request)) {
			String site = request.getHeader("Sec-Fetch-Site");
			boolean crossSite = site != null && !site.equals("same-origin") && !site.equals("none");
			String from = request.getHeader("Origin");
			if (from == null || from.isBlank())
				from = request.getHeader("Referer");
			boolean foreignSource = from != null && !from.isBlank() && !isSameHost(from, effectiveHost(request));
			if (crossSite || foreignSource) {
				log.warn("Chặn GET đổi dữ liệu {} từ nguồn không hợp lệ: {}", request.getRequestURI(), from);
				response.sendError(HttpServletResponse.SC_FORBIDDEN, "Cross-site request blocked");
				return;
			}
			chain.doFilter(request, response);
			return;
		}

		if (SAFE_METHODS.contains(request.getMethod()) || request.getHeader("Authorization") != null
				|| request.getRequestURI().startsWith(request.getContextPath() + "/ws")) {
			chain.doFilter(request, response);
			return;
		}

		String origin = request.getHeader("Origin");
		String source = origin;
		if (source == null || source.isBlank()) {
			source = request.getHeader("Referer");
		}

		// Không có Origin lẫn Referer: không phải trình duyệt gửi chéo trang -> cho qua
		if (source == null || source.isBlank()) {
			chain.doFilter(request, response);
			return;
		}

		// Dùng host công khai mà trình duyệt đã truy cập. Với ngrok/reverse proxy,
		// Host nội bộ có thể khác X-Forwarded-Host.
		String effectiveHost = effectiveHost(request);

		if (isSameHost(source, effectiveHost)) {
			chain.doFilter(request, response);
			return;
		}

		log.warn("Chặn request {} {} từ nguồn không hợp lệ: {}", request.getMethod(), request.getRequestURI(), source);
		response.sendError(HttpServletResponse.SC_FORBIDDEN, "Cross-site request blocked");
	}

	private boolean isStateChangingGet(HttpServletRequest request) {
		String path = request.getRequestURI().substring(request.getContextPath().length());
		return path.contains("/delete/") || path.contains("/restore/") || path.contains("/remove/")
				|| path.contains("/toggle");
	}

	/**
	 * Returns the public host used by the browser. A reverse proxy such as ngrok
	 * can forward the request to localhost while preserving the external host.
	 */
	private String effectiveHost(HttpServletRequest request) {
		String forwardedHost = request.getHeader("X-Forwarded-Host");
		if (forwardedHost != null && !forwardedHost.isBlank()) {
			return forwardedHost.split(",")[0].trim();
		}
		String host = request.getHeader("Host");
		if (host != null && !host.isBlank()) {
			return host.trim();
		}
		return request.getServerName();
	}

	private boolean isSameHost(String source, String hostHeader) {
		if (source == null || source.isBlank() || hostHeader == null || hostHeader.isBlank())
			return false;
		try {
			URI sourceUri = URI.create(source.trim());
			if (sourceUri.getHost() == null || sourceUri.getScheme() == null)
				return false; // vd: Origin "null"

			// X-Forwarded-Host/Host thường chỉ chứa hostname, nhưng có thể chứa port
			// (ví dụ example.ngrok-free.dev:443). Chuẩn hoá để cổng mặc định của
			// HTTPS/HTTP không làm request cùng origin bị nhận nhầm là cross-site.
			String candidate = hostHeader.trim().split(",", 2)[0].trim();
			URI hostUri = URI.create(candidate.contains("://") ? candidate : "http://" + candidate);
			if (hostUri.getHost() == null)
				return false;

			String sourceHost = sourceUri.getHost();
			String candidateHost = hostUri.getHost();
			int sourcePort = sourceUri.getPort() >= 0 ? sourceUri.getPort() : defaultPort(sourceUri.getScheme());
			int candidatePort = hostUri.getPort() >= 0 ? hostUri.getPort() : sourcePort;
			return sourceHost.equalsIgnoreCase(candidateHost) && sourcePort == candidatePort;
		} catch (IllegalArgumentException e) {
			return false;
		}
	}

	private int defaultPort(String scheme) {
		if ("https".equalsIgnoreCase(scheme)) return 443;
		if ("http".equalsIgnoreCase(scheme)) return 80;
		return -1;
	}
}
