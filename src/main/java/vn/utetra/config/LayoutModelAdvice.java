package vn.utetra.config;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import vn.utetra.entity.Branch;
import vn.utetra.entity.Category;
import vn.utetra.entity.Notification;
import vn.utetra.repository.NotificationRepository;
import vn.utetra.service.BranchService;
import vn.utetra.service.CategoryService;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Cấp dữ liệu dùng chung cho header của trang khách (mega-menu danh mục, danh sách cửa hàng cho popup nhận hàng).
 * Bỏ qua trang quản trị và API để không tốn truy vấn.
 */
@ControllerAdvice
public class LayoutModelAdvice {

    @Autowired private CategoryService categoryService;
    @Autowired private BranchService branchService;
    @Autowired private vn.utetra.repository.FavoriteRepository favoriteRepo;
    @Autowired private NotificationRepository notificationRepo;

    private static boolean customerPage(HttpServletRequest req) {
        String uri = req.getRequestURI().substring(req.getContextPath().length());
        return !(uri.startsWith("/admin") || uri.startsWith("/shipper") || uri.startsWith("/api") || uri.startsWith("/ws")
                || uri.startsWith("/order/voucher") || uri.startsWith("/products/suggest")
                || uri.startsWith("/receive/nearest") || uri.startsWith("/payment/vnpay-ipn"));
    }

    /** Dữ liệu cho bảng thông báo ở chuông header: 10 tin mới nhất của từng nhóm. */
    @ModelAttribute("notificationDrawer")
    public Map<String, Object> notificationDrawer(HttpServletRequest req) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("news", List.of());
        data.put("orders", List.of());
        data.put("unread", 0L);
        if (!customerPage(req)) return data;
        jakarta.servlet.http.HttpSession session = req.getSession(false);
        Object value = session == null ? null : session.getAttribute("userId");
        if (!(value instanceof Number)) return data;
        Long userId = ((Number) value).longValue();
        List<Notification> all = notificationRepo.findByUserIdOrderByCreatedAtDesc(userId);
        List<Notification> news = new ArrayList<>();
        List<Notification> orders = new ArrayList<>();
        for (Notification n : all) {
            if ("ORDER".equalsIgnoreCase(n.getType())) {
                if (orders.size() < 10) orders.add(n);
            } else if (news.size() < 10) {
                news.add(n);
            }
            if (news.size() >= 10 && orders.size() >= 10) break;
        }
        data.put("news", news);
        data.put("orders", orders);
        data.put("unread", notificationRepo.countByUserIdAndIsReadFalse(userId));
        return data;
    }

    @ModelAttribute("menuCategories")
    public List<Category> menuCategories(HttpServletRequest req) {
        return customerPage(req) ? categoryService.active() : List.of();
    }

    /** Id các món khách đã thả tim (để tô đỏ ♥ trên thẻ sản phẩm). Chỉ tính ở trang có danh sách món. */
    @ModelAttribute("favIds")
    public java.util.Set<Long> favIds(HttpServletRequest req) {
        String uri = req.getRequestURI();
        boolean listPage = uri.equals("/") || uri.equals("/home") || uri.equals("/products");
        if (!listPage) return java.util.Set.of();
        jakarta.servlet.http.HttpSession s = req.getSession(false);
        Object uid = s == null ? null : s.getAttribute("userId");
        if (!(uid instanceof Long id)) return java.util.Set.of();
        return new java.util.HashSet<>(favoriteRepo.findProductIdsByUserId(id));
    }

    @ModelAttribute("receiveBranches")
    public List<Branch> receiveBranches(HttpServletRequest req) {
        return customerPage(req) ? branchService.active() : List.of();
    }
}
