package vn.utetra.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.utetra.entity.Branch;
import vn.utetra.entity.Order;
import vn.utetra.security.SafeRedirect;
import vn.utetra.service.BranchService;

/**
 * Popup "Chọn phương thức nhận hàng" trên header: khách chọn Giao hàng / Đến lấy,
 * chi nhánh và địa chỉ trước khi mua. Lựa chọn lưu trong phiên và tự điền ở trang thanh toán.
 * Không cần đăng nhập (xem SecurityConfig).
 */
@Controller
@RequestMapping("/receive")
public class ReceiveController {

    @Autowired private BranchService branchService;

    @PostMapping("/set")
    public String set(@RequestParam(defaultValue = "DELIVERY") String method,
                      @RequestParam(required = false) Long branchId,
                      @RequestParam(required = false) String address,
                      @RequestParam(required = false) String returnUrl,
                      HttpSession session,
                      RedirectAttributes ra) {
        String back = SafeRedirect.localPath(returnUrl, "/products");
        String m = Order.METHOD_PICKUP.equals(method) ? Order.METHOD_PICKUP : Order.METHOD_DELIVERY;
        String addr = address == null ? "" : address.trim();
        if (addr.length() > 500) addr = addr.substring(0, 500);

        if (branchId == null) {
            ra.addFlashAttribute("error", "Vui lòng chọn cửa hàng.");
            return "redirect:" + back;
        }
        Branch b = branchService.active().stream()
                .filter(x -> x.getId().equals(branchId)).findFirst().orElse(null);
        if (b == null) {
            ra.addFlashAttribute("error", "Cửa hàng không tồn tại hoặc đã ngừng hoạt động.");
            return "redirect:" + back;
        }
        if (Order.METHOD_DELIVERY.equals(m) && addr.isEmpty()) {
            ra.addFlashAttribute("error", "Vui lòng nhập địa chỉ nhận hàng.");
            return "redirect:" + back;
        }
        session.setAttribute("RECV_METHOD", m);
        session.setAttribute("RECV_BRANCH", b.getId());
        session.setAttribute("RECV_BRANCH_NAME", b.getName());
        session.setAttribute("RECV_ADDR", addr);
        ra.addFlashAttribute("success", Order.METHOD_PICKUP.equals(m)
                ? "Bạn sẽ đến lấy tại " + b.getName() + "."
                : "Giao hàng từ " + b.getName() + " tới địa chỉ của bạn.");
        return "redirect:" + back;
    }
}
