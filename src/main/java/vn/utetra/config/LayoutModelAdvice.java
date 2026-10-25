package vn.utetra.config;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import vn.utetra.entity.Branch;
import vn.utetra.entity.Category;
import vn.utetra.service.BranchService;
import vn.utetra.service.CategoryService;

import java.util.List;

/**
 * Cấp dữ liệu dùng chung cho header của trang khách (mega-menu danh mục, danh sách cửa hàng cho popup nhận hàng).
 * Bỏ qua trang quản trị và API để không tốn truy vấn.
 */
@ControllerAdvice
public class LayoutModelAdvice {

    @Autowired private CategoryService categoryService;
    @Autowired private BranchService branchService;
    @Autowired private vn.utetra.repository.FavoriteRepository favoriteRepo;

    private static boolean customerPage(HttpServletRequest req) {
        String uri = req.getRequestURI();
        return !(uri.startsWith("/admin") || uri.startsWith("/api") || uri.startsWith("/ws")
                || uri.startsWith("/order/voucher") || uri.startsWith("/products/suggest")
                || uri.startsWith("/receive/nearest") || uri.startsWith("/payment/vnpay-ipn"));
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
