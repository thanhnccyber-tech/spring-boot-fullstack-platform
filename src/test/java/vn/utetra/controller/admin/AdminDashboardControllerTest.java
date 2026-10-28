package vn.utetra.controller.admin;

import org.junit.jupiter.api.Test;
import org.springframework.ui.ConcurrentModel;
import vn.utetra.repository.OrderRepository;
import vn.utetra.repository.UserRepository;
import vn.utetra.repository.VoucherRepository;
import vn.utetra.service.BranchService;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AdminDashboardControllerTest {

    @Test
    void dashboardBuildsTwelveMonthSeries() throws Exception {
        OrderRepository repository = mock(OrderRepository.class);
        UserRepository userRepo = mock(UserRepository.class);
        VoucherRepository voucherRepo = mock(VoucherRepository.class);
        BranchService branchService = mock(BranchService.class);

        AdminDashboardController controller = new AdminDashboardController();
        setField(controller, "orderRepo", repository);
        setField(controller, "userRepo", userRepo);
        setField(controller, "voucherRepo", voucherRepo);
        setField(controller, "branchService", branchService);

        when(repository.statusCountBetween(any(), any(), any())).thenReturn(List.of());
        when(repository.liveTotals(any(), any(), any())).thenReturn(rows(new Object[]{new BigDecimal("5000"), 10L}));
        when(repository.collectedTotals(any(), any(), any())).thenReturn(rows(new Object[]{new BigDecimal("3500"), 8L}));
        when(repository.collectedByDay(any(), any(), any())).thenReturn(rows(
                new Object[]{LocalDate.now().toString(), new BigDecimal("100")}
        ));
        when(repository.liveByDay(any(), any(), any())).thenReturn(rows(
                new Object[]{LocalDate.now().toString(), new BigDecimal("120")}
        ));
        when(repository.collectedByMonth(any(), any(), any())).thenReturn(rows(
                new Object[]{LocalDate.now().getYear(), LocalDate.now().getMonthValue(), new BigDecimal("1000")}
        ));
        when(repository.liveByMonth(any(), any(), any())).thenReturn(rows(
                new Object[]{LocalDate.now().getYear(), LocalDate.now().getMonthValue(), new BigDecimal("1200")}
        ));
        when(repository.liveByHour(any(), any(), any())).thenReturn(List.of());
        when(repository.liveByPaymentMethod(any(), any(), any())).thenReturn(List.of());
        when(repository.topProductsBetween(any(), any(), any())).thenReturn(List.of());
        when(repository.topToppingsBetween(any(), any(), any())).thenReturn(List.of());
        when(repository.countPending(any())).thenReturn(0L);
        when(repository.countStaleOnlinePayments(any(), any())).thenReturn(0L);
        when(repository.countStaleUnconfirmed(any(), any())).thenReturn(0L);
        when(userRepo.countNewBetween(any(), any())).thenReturn(0L);
        when(voucherRepo.findExpiringOrNearlyUsedUp(any(), any(), anyInt())).thenReturn(List.of());
        when(branchService.all()).thenReturn(List.of());

        ConcurrentModel model = new ConcurrentModel();
        controller.dashboard(null, null, null, null, model);

        @SuppressWarnings("unchecked")
        List<String> labels = (List<String>) model.getAttribute("labels12");
        @SuppressWarnings("unchecked")
        List<Long> collected = (List<Long>) model.getAttribute("data12Collected");
        @SuppressWarnings("unchecked")
        List<Long> expected = (List<Long>) model.getAttribute("data12Expected");

        assertNotNull(labels);
        assertNotNull(collected);
        assertNotNull(expected);
        assertEquals(12, labels.size());
        assertEquals(12, collected.size());
        assertEquals(12, expected.size());
    }

    private static List<Object[]> rows(Object[]... values) {
        return List.of(values);
    }

    private static void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = AdminDashboardController.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
