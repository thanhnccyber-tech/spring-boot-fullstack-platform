package vn.utetra.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.access.AccessDeniedException;
import vn.utetra.entity.DeliveryFailure;
import vn.utetra.entity.*;
import vn.utetra.repository.OrderRepository;
import vn.utetra.repository.DeliveryFailureRepository;
import vn.utetra.repository.ShipperOrderRepository;
import vn.utetra.repository.UserRepository;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ShipperServiceTest {

    @Mock private ShipperOrderRepository shipperRepo;
    @Mock private OrderRepository orderRepo;
    @Mock private DeliveryFailureRepository failureRepo;
    @Mock private UserRepository userRepo;
    @Mock private OrderService orderService;
    @InjectMocks private ShipperService service;

    private User shipper;
    private User otherShipper;
    private Branch branch;
    private Order order;

    private static User shipperUser(long id, String email, Branch b) {
        Role r = new Role();
        r.setName(RoleName.SHIPPER);
        User u = new User();
        u.setId(id);
        u.setEmail(email);
        u.setEnabled(true);
        u.setRoles(Set.of(r));
        u.setBranch(b);
        return u;
    }

    @BeforeEach
    void setUp() {
        branch = new Branch();
        branch.setId(7L);

        shipper = shipperUser(10L, "ship@utetra.vn", branch);
        otherShipper = shipperUser(11L, "ship2@utetra.vn", branch);

        order = new Order();
        order.setId(100L);
        order.setBranch(branch);
        order.setStatus(Order.STATUS_PREPARING);
        order.setDeliveryMethod(Order.METHOD_DELIVERY);
        order.setPaymentMethod("COD");
        order.setPaymentStatus("PENDING");
        order.setTotalAmount(new BigDecimal("75000"));

        when(userRepo.findByEmail("ship@utetra.vn")).thenReturn(Optional.of(shipper));
        when(userRepo.findByEmail("ship2@utetra.vn")).thenReturn(Optional.of(otherShipper));
        when(shipperRepo.findByIdForUpdate(100L)).thenReturn(Optional.of(order));
    }

    /* ---------- xác định shipper ---------- */

    @Test
    void onlyEnabledShipperMayUseShipperFeatures() {
        User customer = new User();
        customer.setId(2L);
        customer.setEmail("kh@b.vn");
        customer.setEnabled(true);
        when(userRepo.findByEmail("kh@b.vn")).thenReturn(Optional.of(customer));
        assertThrows(AccessDeniedException.class, () -> service.available("kh@b.vn"));

        shipper.setEnabled(false);
        assertThrows(AccessDeniedException.class, () -> service.available("ship@utetra.vn"));
    }

    /* ---------- nhận đơn ---------- */

    @Test
    void acceptAssignsShipperAndMovesToDelivering() {
        service.accept(100L, "ship@utetra.vn");

        assertSame(shipper, order.getShipper());
        verify(orderService).updateStatus(100L, Order.STATUS_DELIVERING);
    }

    @Test
    void acceptRejectedWhenAnotherShipperAlreadyTookIt() {
        order.setShipper(otherShipper);

        assertThrows(IllegalArgumentException.class, () -> service.accept(100L, "ship@utetra.vn"));
        verify(orderService, never()).updateStatus(anyLong(), anyString());
        assertSame(otherShipper, order.getShipper());
    }

    @Test
    void acceptRejectedForOrderPlacedByTheShipperHimself() {
        // Shipper cũng là khách hàng: không được tự nhận đơn mình đặt
        order.setUser(shipper);

        assertThrows(IllegalArgumentException.class, () -> service.accept(100L, "ship@utetra.vn"));
        verify(orderService, never()).updateStatus(anyLong(), anyString());
        assertNull(order.getShipper());
    }

    @Test
    void acceptStillAllowedForOrderPlacedByAnotherCustomer() {
        order.setUser(otherShipper);

        service.accept(100L, "ship@utetra.vn");

        assertSame(shipper, order.getShipper());
    }

    @Test
    void acceptAllowedWhenAdminAssignedThisShipper() {
        order.setShipper(shipper);

        service.accept(100L, "ship@utetra.vn");

        verify(orderService).updateStatus(100L, Order.STATUS_DELIVERING);
    }

    @Test
    void acceptAllowedForOrderWaitingForRedelivery() {
        order.setStatus(Order.STATUS_READY);

        service.accept(100L, "ship@utetra.vn");

        assertSame(shipper, order.getShipper());
        verify(orderService).updateStatus(100L, Order.STATUS_DELIVERING);
    }

    @Test
    void acceptRejectedForOtherBranch() {
        Branch other = new Branch();
        other.setId(99L);
        order.setBranch(other);

        assertThrows(AccessDeniedException.class, () -> service.accept(100L, "ship@utetra.vn"));
        verify(orderService, never()).updateStatus(anyLong(), anyString());
    }

    @Test
    void acceptRejectedWhenShipperHasNoBranch() {
        shipper.setBranch(null);

        assertThrows(AccessDeniedException.class, () -> service.accept(100L, "ship@utetra.vn"));
    }

    @Test
    void acceptRejectedForPickupOrNotPreparingOrders() {
        order.setDeliveryMethod(Order.METHOD_PICKUP);
        assertThrows(IllegalArgumentException.class, () -> service.accept(100L, "ship@utetra.vn"));

        order.setDeliveryMethod(Order.METHOD_DELIVERY);
        order.setStatus(Order.STATUS_PENDING);
        assertThrows(IllegalArgumentException.class, () -> service.accept(100L, "ship@utetra.vn"));

        verify(orderService, never()).updateStatus(anyLong(), anyString());
    }

    /* ---------- giao thành công ---------- */

    private void deliveringOwnedByShipper() {
        order.setStatus(Order.STATUS_DELIVERING);
        order.setShipper(shipper);
    }

    @Test
    void completeMarksCodAsPaid() {
        deliveringOwnedByShipper();

        service.complete(100L, "ship@utetra.vn");

        assertEquals("PAID", order.getPaymentStatus());
        assertNotNull(order.getPaidAt());
        verify(orderService).updateStatus(100L, Order.STATUS_COMPLETED);
    }

    @Test
    void completeKeepsPaymentStatusForNonCodOrders() {
        deliveringOwnedByShipper();
        order.setPaymentMethod("VNPAY");
        order.setPaymentStatus("PAID");

        service.complete(100L, "ship@utetra.vn");

        assertEquals("PAID", order.getPaymentStatus());
        assertNull(order.getPaidAt());
        verify(orderService).updateStatus(100L, Order.STATUS_COMPLETED);
    }

    @Test
    void cannotCompleteSomeoneElsesOrder() {
        deliveringOwnedByShipper();

        assertThrows(AccessDeniedException.class, () -> service.complete(100L, "ship2@utetra.vn"));
        verify(orderService, never()).updateStatus(anyLong(), anyString());
    }

    @Test
    void cannotCompleteOrderThatIsNotDelivering() {
        order.setStatus(Order.STATUS_PREPARING);
        order.setShipper(shipper);

        assertThrows(IllegalArgumentException.class, () -> service.complete(100L, "ship@utetra.vn"));
        verify(orderService, never()).updateStatus(anyLong(), anyString());
    }

    /* ---------- giao thất bại ---------- */

    @Test
    void failRequiresReason() {
        deliveringOwnedByShipper();

        assertThrows(IllegalArgumentException.class, () -> service.fail(100L, "ship@utetra.vn", "   "));
        assertThrows(IllegalArgumentException.class, () -> service.fail(100L, "ship@utetra.vn", null));
        assertThrows(IllegalArgumentException.class,
                () -> service.fail(100L, "ship@utetra.vn", "x".repeat(ShipperService.MAX_REASON_LENGTH + 1)));
        verify(orderService, never()).updateStatus(anyLong(), anyString());
    }

    @Test
    void failStoresAttemptAndReturnsOrderToRedeliveryQueue() {
        deliveringOwnedByShipper();

        service.fail(100L, "ship@utetra.vn", "  Khách không nghe máy  ");

        assertEquals("Khách không nghe máy", order.getFailedReason());
        assertEquals("PENDING", order.getPaymentStatus());
        assertNull(order.getShipper());
        verify(failureRepo).save(argThat(failure ->
                failure.getOrder() == order
                        && failure.getShipper() == shipper
                        && "Khách không nghe máy".equals(failure.getReason())));
        verify(orderService).updateStatus(100L, Order.STATUS_READY);
    }

    @Test
    void cannotFailSomeoneElsesOrder() {
        deliveringOwnedByShipper();

        assertThrows(AccessDeniedException.class,
                () -> service.fail(100L, "ship2@utetra.vn", "Sai địa chỉ"));
        verify(orderService, never()).updateStatus(anyLong(), anyString());
    }

    /* ---------- xem chi tiết ---------- */

    @Test
    void detailVisibleForOwnOrPickupPoolOnly() {
        when(orderRepo.findByIdWithDetails(100L)).thenReturn(Optional.of(order));

        // đơn đang chờ nhận ở chi nhánh mình: xem được
        assertSame(order, service.detail(100L, "ship@utetra.vn"));

        // đơn đã giao cho shipper khác: không được xem
        order.setStatus(Order.STATUS_DELIVERING);
        order.setShipper(otherShipper);
        assertThrows(AccessDeniedException.class, () -> service.detail(100L, "ship@utetra.vn"));

        // đơn của mình: xem được
        order.setShipper(shipper);
        assertSame(order, service.detail(100L, "ship@utetra.vn"));
    }

    @Test
    void detailRemainsVisibleToShipperWhoPreviouslyAttemptedDelivery() {
        when(orderRepo.findByIdWithDetails(100L)).thenReturn(Optional.of(order));
        order.setStatus(Order.STATUS_COMPLETED);
        order.setShipper(otherShipper);
        when(failureRepo.existsByOrder_IdAndShipper_Id(100L, shipper.getId())).thenReturn(true);

        assertSame(order, service.detail(100L, "ship@utetra.vn"));
    }
}