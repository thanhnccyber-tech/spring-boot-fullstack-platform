package vn.utetra.controller.admin;

import org.junit.jupiter.api.Test;
import org.springframework.ui.ConcurrentModel;
import vn.utetra.repository.OrderRepository;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AdminDashboardControllerTest {

    @Test
    void dashboardBuildsTwelveMondayToSundayRevenueBuckets() throws Exception {
        OrderRepository repository = mock(OrderRepository.class);
        AdminDashboardController controller = new AdminDashboardController();
        Field repositoryField = AdminDashboardController.class.getDeclaredField("orderRepo");
        repositoryField.setAccessible(true);
        repositoryField.set(controller, repository);
        when(repository.findBetween(any(LocalDateTime.class), any(LocalDateTime.class))).thenReturn(List.of());
        when(repository.countByStatus()).thenReturn(List.of());
        when(repository.revenueByMonth(any(LocalDateTime.class), any(LocalDateTime.class))).thenReturn(List.of());
        when(repository.topSellingProducts()).thenReturn(List.of());
        when(repository.topSellingToppings()).thenReturn(List.of());

        LocalDate today = LocalDate.now();
        LocalDate thisMonday = today.minusDays(today.getDayOfWeek().getValue() - 1L);
        LocalDate firstMonday = thisMonday.minusWeeks(11);
        LocalDate beforeRange = firstMonday.minusDays(1);
        LocalDate firstTuesday = firstMonday.plusDays(1);
        LocalDate nextMonday = thisMonday.plusWeeks(1);
        List<Object[]> dailyRevenue = List.of(
                new Object[]{firstMonday.toString(), new BigDecimal("100")},
                new Object[]{firstTuesday.toString(), new BigDecimal("250")},
                new Object[]{thisMonday.toString(), new BigDecimal("500")},
                new Object[]{nextMonday.toString(), new BigDecimal("900")},
                new Object[]{beforeRange.toString(), new BigDecimal("1000")});
        when(repository.revenueByDay(any(), any())).thenReturn(dailyRevenue);

        ConcurrentModel model = new ConcurrentModel();
        controller.dashboard(model);

        @SuppressWarnings("unchecked")
        List<String> labels = (List<String>) model.getAttribute("labels12Weeks");
        @SuppressWarnings("unchecked")
        List<BigDecimal> revenue = (List<BigDecimal>) model.getAttribute("data12Weeks");
        assertNotNull(labels);
        assertNotNull(revenue);
        assertEquals(12, labels.size());
        assertEquals(12, revenue.size());
        assertEquals(firstMonday.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM"))
                + "–" + firstMonday.plusDays(6).format(java.time.format.DateTimeFormatter.ofPattern("dd/MM")),
                labels.get(0));
        assertEquals(new BigDecimal("350"), revenue.get(0));
        assertEquals(new BigDecimal("500"), revenue.get(11));
        assertEquals(BigDecimal.ZERO, revenue.get(10));

        verify(repository).revenueByDay(
                firstMonday.atStartOfDay(),
                thisMonday.plusDays(6).atTime(java.time.LocalTime.MAX));
    }
}
