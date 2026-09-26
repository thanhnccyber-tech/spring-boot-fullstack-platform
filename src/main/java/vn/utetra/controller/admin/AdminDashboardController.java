package vn.utetra.controller.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import vn.utetra.repository.OrderRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin")
public class AdminDashboardController {

    @Autowired private OrderRepository orderRepository;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().atTime(LocalTime.MAX);

        var todayOrders = orderRepository.findBetween(startOfDay, endOfDay);

        var todayRevenue = todayOrders.stream()
                .filter(o -> !"CANCELLED".equals(o.getStatus()))
                .map(o -> o.getTotalAmount())
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);

        var statusCount = new HashMap<String, Long>();
        for (Object[] row : orderRepository.countByStatus()) {
            statusCount.put((String) row[0], (Long) row[1]);
        }

        model.addAttribute("todayOrderCount", todayOrders.size());
        model.addAttribute("todayRevenue", todayRevenue);
        model.addAttribute("statusCount", statusCount);
        model.addAttribute("topProducts", orderRepository.topSellingProducts());
        return "admin/dashboard";
    }
}