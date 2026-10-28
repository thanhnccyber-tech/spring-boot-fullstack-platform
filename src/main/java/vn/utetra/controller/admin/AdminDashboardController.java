package vn.utetra.controller.admin;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.utetra.entity.Order;
import vn.utetra.entity.User;
import vn.utetra.entity.Voucher;
import vn.utetra.repository.OrderRepository;
import vn.utetra.repository.UserRepository;
import vn.utetra.repository.VoucherRepository;
import vn.utetra.service.BranchService;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Dashboard quản trị.
 *  - ADMIN: xem toàn hệ thống hoặc lọc theo bất kỳ chi nhánh.
 *  - MANAGER: chỉ xem dữ liệu của chi nhánh mình (branchId bị ép ở server).
 */
@Controller
@RequestMapping("/admin")
public class AdminDashboardController {

    private static final ZoneId VN = ZoneId.of("Asia/Ho_Chi_Minh");
    private static final DateTimeFormatter DAY = DateTimeFormatter.ofPattern("dd/MM");
    private static final int MAX_DAYS = 366;

    private static final String[] STATUSES = {
            Order.STATUS_PENDING, Order.STATUS_PREPARING, Order.STATUS_DELIVERING,
            Order.STATUS_COMPLETED, Order.STATUS_CANCELLED, Order.STATUS_FAILED };

    @Autowired private OrderRepository orderRepo;
    @Autowired private UserRepository userRepo;
    @Autowired private VoucherRepository voucherRepo;
    @Autowired private BranchService branchService;

    private record Period(String range, LocalDate from, LocalDate to) {
        LocalDateTime start() { return from.atStartOfDay(); }
        LocalDateTime end() { return to.atTime(LocalTime.MAX); }
        long days() { return ChronoUnit.DAYS.between(from, to) + 1; }
    }

    private static Period resolve(String range, LocalDate from, LocalDate to) {
        LocalDate today = LocalDate.now(VN);
        String r = range == null ? "7d" : range;
        switch (r) {
            case "today": return new Period("today", today, today);
            case "30d":   return new Period("30d", today.minusDays(29), today);
            case "month": return new Period("month", today.withDayOfMonth(1), today);
            case "custom": {
                LocalDate f = from == null ? today.minusDays(6) : from;
                LocalDate t = to == null ? today : to;
                if (f.isAfter(t)) { LocalDate x = f; f = t; t = x; }
                if (ChronoUnit.DAYS.between(f, t) + 1 > MAX_DAYS) f = t.minusDays(MAX_DAYS - 1L);
                return new Period("custom", f, t);
            }
            default: return new Period("7d", today.minusDays(6), today);
        }
    }

    private static BigDecimal bd(Object o) {
        if (o == null) return BigDecimal.ZERO;
        if (o instanceof BigDecimal b) return b;
        return new BigDecimal(o.toString());
    }

    private static long lng(Object o) { return o == null ? 0L : ((Number) o).longValue(); }

    private static Object[] firstRow(List<Object[]> rows) {
        return rows == null || rows.isEmpty() ? new Object[] { null, 0L } : rows.get(0);
    }

    private BigDecimal collected(LocalDateTime f, LocalDateTime t, Long branchId) {
        return bd(firstRow(orderRepo.collectedTotals(f, t, branchId))[0]);
    }

    private static String methodLabel(String m) {
        if (m == null) return "Khác";
        return switch (m) {
            case "COD" -> "Thanh toán khi nhận hàng (COD)";
            case "VNPAY" -> "VNPay";
            case "BANK" -> "Chuyển khoản";
            case "CASH" -> "Tiền mặt tại quầy";
            default -> m;
        };
    }

    private static String statusLabel(String s) {
        return switch (s) {
            case Order.STATUS_PENDING -> "Chờ xác nhận";
            case Order.STATUS_PREPARING -> "Đang pha chế";
            case Order.STATUS_DELIVERING -> "Đang giao / sẵn sàng";
            case Order.STATUS_COMPLETED -> "Hoàn thành";
            case Order.STATUS_CANCELLED -> "Đã hủy";
            case Order.STATUS_FAILED -> "Giao thất bại";
            default -> s;
        };
    }

    private static Map<String, String> alert(String level, String text, String link) {
        Map<String, String> m = new LinkedHashMap<>();
        m.put("level", level);
        m.put("text", text);
        m.put("link", link);
        return m;
    }

    private static List<Long> perDay(List<Object[]> rows, Period p) {
        Map<String, Long> m = new HashMap<>();
        for (Object[] r : rows) m.put(r[0].toString(), bd(r[1]).longValue());
        List<Long> out = new ArrayList<>();
        for (LocalDate d = p.from(); !d.isAfter(p.to()); d = d.plusDays(1)) out.add(m.getOrDefault(d.toString(), 0L));
        return out;
    }

    private static List<Long> perMonth(List<Object[]> rows, YearMonth first) {
        Map<String, Long> m = new HashMap<>();
        for (Object[] r : rows) m.put(((Number) r[0]).intValue() + "-" + ((Number) r[1]).intValue(), bd(r[2]).longValue());
        List<Long> out = new ArrayList<>();
        for (int i = 0; i < 12; i++) {
            YearMonth ym = first.plusMonths(i);
            out.add(m.getOrDefault(ym.getYear() + "-" + ym.getMonthValue(), 0L));
        }
        return out;
    }

    /**
     * Trả về branchId hiệu lực cho request:
     *  - ADMIN: dùng branchId client gửi (null = toàn hệ thống).
     *  - MANAGER: ép = chi nhánh của mình. Nếu chưa được gán chi nhánh thì trả về -1
     *    (không có chi nhánh nào khớp → dữ liệu rỗng, an toàn).
     *
     * Lưu ý: dùng findByEmailWithBranch để load sẵn branch (LEFT JOIN FETCH u.branch),
     * tránh LazyInitializationException khi spring.jpa.open-in-view=false.
     */
    private Long effectiveBranchId(Authentication auth, Long requested) {
        User me = userRepo.findByEmailWithBranch(auth.getName()).orElseThrow();
        if (me.isAdmin()) return requested;
        if (me.getBranch() == null) return -1L;
        return me.getBranch().getId();
    }

    private boolean isAdmin(Authentication auth) {
        // isAdmin() chỉ đọc user.roles (FetchType.EAGER) nên không cần fetch branch;
        // vẫn dùng findByEmailWithBranch để đồng bộ cách truy vấn.
        return userRepo.findByEmailWithBranch(auth.getName()).map(User::isAdmin).orElse(false);
    }

    @GetMapping({"", "/dashboard"})
    public String dashboard(@RequestParam(required = false) String range,
                            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
                            @RequestParam(required = false) Long branchId,
                            Authentication auth,
                            Model model,
                            RedirectAttributes ra) {
        // Ép chi nhánh cho Manager trước khi làm bất kỳ truy vấn nào.
        branchId = effectiveBranchId(auth, branchId);
        boolean admin = isAdmin(auth);

        if (!admin && branchId != null && branchId == -1L) {
            ra.addFlashAttribute("error",
                    "Tài khoản Manager chưa được gán chi nhánh. Vui lòng liên hệ Admin.");
            return "redirect:/admin/orders";
        }

        Period p = resolve(range, from, to);
        LocalDateTime f = p.start(), t = p.end();
        LocalDate today = LocalDate.now(VN);
        LocalDateTime now = LocalDateTime.now(VN);

        LocalDateTime t0 = today.atStartOfDay(), t1 = today.atTime(LocalTime.MAX);
        long todayOrders = 0;
        for (Object[] r : orderRepo.statusCountBetween(t0, t1, branchId)) todayOrders += lng(r[1]);
        Object[] todayLive = firstRow(orderRepo.liveTotals(t0, t1, branchId));
        model.addAttribute("todayOrderCount", todayOrders);
        model.addAttribute("todayExpected", bd(todayLive[0]));
        model.addAttribute("todayCollected", collected(t0, t1, branchId));

        Map<String, Long> statusCount = new LinkedHashMap<>();
        for (String s : STATUSES) statusCount.put(s, 0L);
        long totalOrders = 0;
        for (Object[] r : orderRepo.statusCountBetween(f, t, branchId)) {
            long c = lng(r[1]);
            totalOrders += c;
            statusCount.merge((String) r[0], c, Long::sum);
        }
        Object[] live = firstRow(orderRepo.liveTotals(f, t, branchId));
        BigDecimal expected = bd(live[0]);
        long liveCount = lng(live[1]);
        Object[] col = firstRow(orderRepo.collectedTotals(f, t, branchId));
        BigDecimal collected = bd(col[0]);
        BigDecimal avg = liveCount == 0 ? BigDecimal.ZERO : expected.divide(BigDecimal.valueOf(liveCount), 0, RoundingMode.HALF_UP);
        double cancelRate = totalOrders == 0 ? 0 : statusCount.get(Order.STATUS_CANCELLED) * 100.0 / totalOrders;

        long len = p.days();
        BigDecimal prev = collected(f.minusDays(len), f.minusNanos(1), branchId);
        Double periodGrowth = prev.signum() == 0 ? null
                : collected.subtract(prev).multiply(BigDecimal.valueOf(100)).divide(prev, 1, RoundingMode.HALF_UP).doubleValue();

        YearMonth ym = YearMonth.from(today);
        BigDecimal thisMonth = collected(ym.atDay(1).atStartOfDay(), t1, branchId);
        BigDecimal lastMonth = collected(ym.minusMonths(1).atDay(1).atStartOfDay(),
                ym.minusMonths(1).atEndOfMonth().atTime(LocalTime.MAX), branchId);
        Double monthGrowth = lastMonth.signum() == 0 ? null
                : thisMonth.subtract(lastMonth).multiply(BigDecimal.valueOf(100)).divide(lastMonth, 1, RoundingMode.HALF_UP).doubleValue();

        model.addAttribute("totalOrders", totalOrders);
        model.addAttribute("liveCount", liveCount);
        model.addAttribute("expected", expected);
        model.addAttribute("collected", collected);
        model.addAttribute("uncollected", expected.subtract(collected));
        model.addAttribute("avgOrder", avg);
        model.addAttribute("cancelRate", Math.round(cancelRate * 10) / 10.0);
        model.addAttribute("newCustomers", userRepo.countNewBetween(f, t));
        model.addAttribute("periodGrowth", periodGrowth);
        model.addAttribute("thisMonth", thisMonth);
        model.addAttribute("lastMonth", lastMonth);
        model.addAttribute("monthGrowth", monthGrowth);
        model.addAttribute("statusCount", statusCount);

        List<String> statusLabels = new ArrayList<>();
        for (String s : STATUSES) statusLabels.add(statusLabel(s));
        model.addAttribute("statusLabels", statusLabels);
        model.addAttribute("statusData", new ArrayList<>(statusCount.values()));

        List<String> labelsDay = new ArrayList<>();
        for (LocalDate d = p.from(); !d.isAfter(p.to()); d = d.plusDays(1)) labelsDay.add(d.format(DAY));
        model.addAttribute("labelsDay", labelsDay);
        model.addAttribute("dataDayCollected", perDay(orderRepo.collectedByDay(f, t, branchId), p));
        model.addAttribute("dataDayExpected", perDay(orderRepo.liveByDay(f, t, branchId), p));

        YearMonth firstMonth = ym.minusMonths(11);
        LocalDateTime mf = firstMonth.atDay(1).atStartOfDay();
        LocalDateTime mt = ym.atEndOfMonth().atTime(LocalTime.MAX);
        List<String> labels12 = new ArrayList<>();
        for (int i = 0; i < 12; i++) {
            YearMonth m = firstMonth.plusMonths(i);
            labels12.add(m.getMonthValue() + "/" + m.getYear());
        }
        model.addAttribute("labels12", labels12);
        model.addAttribute("data12Collected", perMonth(orderRepo.collectedByMonth(mf, mt, branchId), firstMonth));
        model.addAttribute("data12Expected", perMonth(orderRepo.liveByMonth(mf, mt, branchId), firstMonth));

        long[] hourOrders = new long[24];
        long[] hourRevenue = new long[24];
        for (Object[] r : orderRepo.liveByHour(f, t, branchId)) {
            int h = ((Number) r[0]).intValue();
            if (h >= 0 && h < 24) { hourOrders[h] = lng(r[1]); hourRevenue[h] = bd(r[2]).longValue(); }
        }
        List<String> hourLabels = new ArrayList<>();
        List<Long> hOrders = new ArrayList<>(), hRev = new ArrayList<>();
        for (int h = 0; h < 24; h++) { hourLabels.add(h + "h"); hOrders.add(hourOrders[h]); hRev.add(hourRevenue[h]); }
        model.addAttribute("hourLabels", hourLabels);
        model.addAttribute("hourOrders", hOrders);
        model.addAttribute("hourRevenue", hRev);

        List<Map<String, Object>> payments = new ArrayList<>();
        for (Object[] r : orderRepo.liveByPaymentMethod(f, t, branchId)) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("label", methodLabel((String) r[0]));
            m.put("count", lng(r[1]));
            m.put("total", bd(r[2]));
            payments.add(m);
        }
        model.addAttribute("payments", payments);
        List<String> payLabels = new ArrayList<>();
        List<Long> payTotals = new ArrayList<>();
        for (Map<String, Object> m : payments) { payLabels.add((String) m.get("label")); payTotals.add(((BigDecimal) m.get("total")).longValue()); }
        model.addAttribute("payLabels", payLabels);
        model.addAttribute("payTotals", payTotals);

        model.addAttribute("topProducts", orderRepo.topProductsBetween(f, t, branchId));
        model.addAttribute("topToppings", orderRepo.topToppingsBetween(f, t, branchId));

        List<Map<String, String>> alerts = new ArrayList<>();
        long pendingNow = orderRepo.countPending(branchId);
        long staleOnline = orderRepo.countStaleOnlinePayments(now.minusMinutes(15), branchId);
        long staleConfirm = orderRepo.countStaleUnconfirmed(now.minusMinutes(30), branchId);
        if (staleConfirm > 0) alerts.add(alert("danger", staleConfirm + " đơn chờ xác nhận quá 30 phút.", "/admin/orders?status=PENDING"));
        if (staleOnline > 0) alerts.add(alert("warning", staleOnline + " đơn VNPay chưa thanh toán quá 15 phút (khách có thể đã bỏ giữa chừng).", "/admin/orders?status=PENDING"));
        for (Voucher v : voucherRepo.findExpiringOrNearlyUsedUp(now, now.plusDays(3), 5)) {
            StringBuilder sb = new StringBuilder("Mã " + v.getCode());
            if (v.getUsageLimit() != null && v.getUsageLimit() - v.getUsedCount() <= 5)
                sb.append(" chỉ còn ").append(Math.max(0, v.getUsageLimit() - v.getUsedCount())).append(" lượt");
            if (v.getExpiresAt() != null && v.getExpiresAt().isBefore(now.plusDays(3)))
                sb.append(v.getUsageLimit() != null && v.getUsageLimit() - v.getUsedCount() <= 5 ? ", " : " ")
                  .append("hết hạn ").append(v.getExpiresAt().format(DateTimeFormatter.ofPattern("dd/MM HH:mm")));
            alerts.add(alert("info", sb.toString() + ".", "/admin/vouchers"));
        }
        model.addAttribute("alerts", alerts);
        model.addAttribute("pendingNow", pendingNow);

        model.addAttribute("range", p.range());
        model.addAttribute("fromDate", p.from().toString());
        model.addAttribute("toDate", p.to().toString());
        model.addAttribute("branchId", branchId);
        model.addAttribute("isAdmin", admin);

        // Manager chỉ thấy 1 chi nhánh của mình trong dropdown.
        // Dùng findByEmailWithBranch để load sẵn branch (LEFT JOIN FETCH u.branch),
        // tránh LazyInitializationException khi spring.jpa.open-in-view=false.
        if (admin) {
            model.addAttribute("branches", branchService.all());
            model.addAttribute("managerBranchName", null);
        } else {
            User me = userRepo.findByEmailWithBranch(auth.getName()).orElseThrow();
            model.addAttribute("branches", me.getBranch() != null ? List.of(me.getBranch()) : List.of());
            model.addAttribute("managerBranchName", me.getBranch() != null ? me.getBranch().getName() : null);
        }
        return "admin/dashboard";
    }

    @GetMapping("/dashboard/export")
    public void export(@RequestParam(required = false) String range,
                       @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                       @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
                       @RequestParam(required = false) Long branchId,
                       Authentication auth,
                       HttpServletResponse response) throws IOException {
        branchId = effectiveBranchId(auth, branchId);
        if (branchId != null && branchId == -1L) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        Period p = resolve(range, from, to);
        LocalDateTime f = p.start(), t = p.end();
        response.setContentType("text/csv;charset=UTF-8");
        response.setHeader("Content-Disposition",
                "attachment; filename=\"bao-cao-" + p.from() + "_" + p.to() + ".csv\"");
        PrintWriter w = new PrintWriter(new java.io.OutputStreamWriter(response.getOutputStream(), StandardCharsets.UTF_8));
        w.print('\uFEFF');

        w.println("BÁO CÁO DOANH THU");
        w.println("Từ ngày," + p.from() + ",Đến ngày," + p.to());
        w.println("Chi nhánh," + csv(branchId == null ? "Tất cả" : branchService.get(branchId).getName()));
        Object[] live = firstRow(orderRepo.liveTotals(f, t, branchId));
        w.println("Số đơn hợp lệ," + lng(live[1]));
        w.println("Doanh thu dự kiến," + bd(live[0]).toPlainString());
        w.println("Doanh thu đã thu," + collected(f, t, branchId).toPlainString());
        w.println();

        w.println("Theo ngày");
        w.println("Ngày,Doanh thu đã thu,Doanh thu dự kiến");
        List<Long> col = perDay(orderRepo.collectedByDay(f, t, branchId), p);
        List<Long> exp = perDay(orderRepo.liveByDay(f, t, branchId), p);
        int i = 0;
        for (LocalDate d = p.from(); !d.isAfter(p.to()); d = d.plusDays(1), i++)
            w.println(d + "," + col.get(i) + "," + exp.get(i));
        w.println();

        w.println("Theo phương thức thanh toán");
        w.println("Phương thức,Số đơn,Doanh thu");
        for (Object[] r : orderRepo.liveByPaymentMethod(f, t, branchId))
            w.println(csv(methodLabel((String) r[0])) + "," + lng(r[1]) + "," + bd(r[2]).toPlainString());
        w.println();

        w.println("Top sản phẩm");
        w.println("Sản phẩm,Số lượng");
        for (Object[] r : orderRepo.topProductsBetween(f, t, branchId)) w.println(csv(String.valueOf(r[0])) + "," + lng(r[1]));
        w.println();
        w.println("Top topping");
        w.println("Topping,Số lượng");
        for (Object[] r : orderRepo.topToppingsBetween(f, t, branchId)) w.println(csv(String.valueOf(r[0])) + "," + lng(r[1]));
        w.flush();
    }

    private static String csv(String s) {
        if (s == null) return "";
        String v = s;
        if (!v.isEmpty() && "=+-@".indexOf(v.charAt(0)) >= 0) v = "'" + v;
        return "\"" + v.replace("\"", "\"\"") + "\"";
    }
}