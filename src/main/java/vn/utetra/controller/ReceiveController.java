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
import vn.utetra.util.Geo;

/**
 * Popup "Chọn phương thức nhận hàng" trên header: khách chọn Giao hàng / Đến lấy,
 * chi nhánh và địa chỉ trước khi mua. Lựa chọn lưu trong phiên và tự điền ở trang thanh toán.
 * Không cần đăng nhập (xem SecurityConfig).
 */
@Controller
@RequestMapping("/receive")
public class ReceiveController {

    @Autowired private BranchService branchService;

    /** Cửa hàng đang hoạt động gần vị trí khách nhất (chỉ xét cửa hàng đã có toạ độ). */
    @GetMapping("/nearest")
    @ResponseBody
    public java.util.Map<String, Object> nearest(@RequestParam Double lat, @RequestParam Double lng) {
        java.util.Map<String, Object> res = new java.util.LinkedHashMap<>();
        if (!Geo.validLat(lat) || !Geo.validLon(lng)) { res.put("found", false); return res; }
        Branch best = null;
        double bestKm = Double.MAX_VALUE;
        for (Branch b : branchService.active()) {
            if (!b.hasCoords()) continue;
            double km = Geo.km(b.getLatitude(), b.getLongitude(), lat, lng);
            if (km < bestKm) { bestKm = km; best = b; }
        }
        if (best == null) { res.put("found", false); return res; }
        res.put("found", true);
        res.put("id", best.getId());
        res.put("name", best.getName());
        res.put("km", Math.round(bestKm * 10) / 10.0);
        return res;
    }

    @PostMapping("/set")
    public String set(@RequestParam(defaultValue = "DELIVERY") String method,
                      @RequestParam(required = false) Long branchId,
                      @RequestParam(required = false) String address,
                      @RequestParam(required = false) String returnUrl,
                      @RequestParam(required = false) Double lat,
                      @RequestParam(required = false) Double lng,
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
        // Toạ độ chỉ có khi khách bấm "Vị trí hiện tại"; dùng gợi ý cửa hàng gần nhất + tính phí ship theo km
        if (Geo.validLat(lat) && Geo.validLon(lng)) {
            session.setAttribute("RECV_LAT", lat);
            session.setAttribute("RECV_LNG", lng);
        } else {
            session.removeAttribute("RECV_LAT");
            session.removeAttribute("RECV_LNG");
        }
        ra.addFlashAttribute("success", Order.METHOD_PICKUP.equals(m)
                ? "Bạn sẽ đến lấy tại " + b.getName() + "."
                : "Giao hàng từ " + b.getName() + " tới địa chỉ của bạn.");
        return "redirect:" + back;
    }
}
