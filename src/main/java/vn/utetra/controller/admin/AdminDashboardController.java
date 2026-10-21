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
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/admin")
public class AdminDashboardController {

    @Autowired private OrderRepository orderRepo;

    @GetMapping({"", "/dashboard"})
    public String dashboard(Model model) {
        LocalDateTime start = LocalDate.now().atStartOfDay();
        LocalDateTime end   = LocalDate.now().atTime(LocalTime.MAX);
        var todayOrders = orderRepo.findBetween(start, end);

        BigDecimal revenue = todayOrders.stream()
                .filter(o -> !"CANCELLED".equals(o.getStatus()))
                .map(o -> o.getTotalAmount() == null ? BigDecimal.ZERO : o.getTotalAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, Long> statusCount = new HashMap<>();
        for (Object[] row : orderRepo.countByStatus())
            statusCount.put((String) row[0], (Long) row[1]);

        model.addAttribute("todayOrderCount", todayOrders.size());
        model.addAttribute("todayRevenue", revenue);
        model.addAttribute("statusCount", statusCount);
        model.addAttribute("topProducts", orderRepo.topSellingProducts());
        return "admin/dashboard";
    }
}