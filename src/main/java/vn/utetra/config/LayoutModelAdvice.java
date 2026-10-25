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

    private static boolean customerPage(HttpServletRequest req) {
        String uri = req.getRequestURI();
        return !(uri.startsWith("/admin") || uri.startsWith("/api") || uri.startsWith("/ws")
                || uri.startsWith("/order/voucher"));
    }

    @ModelAttribute("menuCategories")
    public List<Category> menuCategories(HttpServletRequest req) {
        return customerPage(req) ? categoryService.active() : List.of();
    }

    @ModelAttribute("receiveBranches")
    public List<Branch> receiveBranches(HttpServletRequest req) {
        return customerPage(req) ? branchService.active() : List.of();
    }
}
