package vn.utetra.controller.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import vn.utetra.repository.OrderRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin")
public class AdminDashboardController {

    @Autowired private OrderRepository orderRepo;

    @GetMapping({"", "/dashboard"})
    public String dashboard(Model model) {
        // ===== Thống kê hôm nay =====
        LocalDateTime startToday = LocalDate.now().atStartOfDay();
        LocalDateTime endToday   = LocalDate.now().atTime(LocalTime.MAX);
        var todayOrders = orderRepo.findBetween(startToday, endToday);

        BigDecimal todayRevenue = todayOrders.stream()
                .filter(o -> !"CANCELLED".equals(o.getStatus()))
                .map(o -> o.getTotalAmount() == null ? BigDecimal.ZERO : o.getTotalAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // ===== Trạng thái đơn =====
        Map<String, Long> statusCount = new HashMap<>();
        for (Object[] row : orderRepo.countByStatus())
            statusCount.put((String) row[0], (Long) row[1]);

        // ===== Chart doanh thu 7 ngày gần nhất =====
        LocalDate end7 = LocalDate.now();
        LocalDate start7 = end7.minusDays(6);
        List<Object[]> revenueRaw = orderRepo.revenueByDay(
                start7.atStartOfDay(), end7.atTime(LocalTime.MAX));

        Map<String, BigDecimal> revMap = new HashMap<>();
        for (Object[] row : revenueRaw) {
            String day = row[0].toString(); // yyyy-MM-dd
            revMap.put(day, (BigDecimal) row[1]);
        }

        List<String> labels7 = new ArrayList<>();
        List<BigDecimal> data7 = new ArrayList<>();
        DateTimeFormatter fmtVN = DateTimeFormatter.ofPattern("dd/MM");
        for (int i = 0; i < 7; i++) {
            LocalDate d = start7.plusDays(i);
            labels7.add(d.format(fmtVN));
            data7.add(revMap.getOrDefault(d.toString(), BigDecimal.ZERO));
        }

        // ===== Chart doanh thu 12 tháng gần nhất =====
        YearMonth current = YearMonth.now();
        YearMonth startMonth = current.minusMonths(11);
        LocalDateTime from = startMonth.atDay(1).atStartOfDay();
        LocalDateTime to = current.atEndOfMonth().atTime(LocalTime.MAX);
        List<Object[]> revMonth = orderRepo.revenueByMonth(from, to);

        Map<String, BigDecimal> revMonthMap = new HashMap<>();
        for (Object[] row : revMonth) {
            int y = ((Number) row[0]).intValue();
            int m = ((Number) row[1]).intValue();
            revMonthMap.put(y + "-" + m, (BigDecimal) row[2]);
        }
        List<String> labels12 = new ArrayList<>();
        List<BigDecimal> data12 = new ArrayList<>();
        for (int i = 11; i >= 0; i--) {
            YearMonth ym = current.minusMonths(i);
            labels12.add(ym.getMonthValue() + "/" + ym.getYear());
            data12.add(revMonthMap.getOrDefault(
                    ym.getYear() + "-" + ym.getMonthValue(), BigDecimal.ZERO));
        }

        // ===== Đưa vào model =====
        model.addAttribute("todayOrderCount", todayOrders.size());
        model.addAttribute("todayRevenue", todayRevenue);
        model.addAttribute("statusCount", statusCount);
        model.addAttribute("topProducts", orderRepo.topSellingProducts());
        model.addAttribute("topToppings", orderRepo.topSellingToppings());

        model.addAttribute("labels7", labels7);
        model.addAttribute("data7", data7);
        model.addAttribute("labels12", labels12);
        model.addAttribute("data12", data12);

        return "admin/dashboard";
    }
}