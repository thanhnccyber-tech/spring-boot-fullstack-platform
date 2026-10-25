package vn.utetra.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.util.ReflectionTestUtils;
import vn.utetra.dto.CartItem;
import vn.utetra.dto.OrderRequest;
import vn.utetra.entity.*;
import vn.utetra.repository.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class OrderServiceTest {

    @Mock private OrderRepository orderRepo;
    @Mock private OrderDetailRepository orderDetailRepo;
    @Mock private UserRepository userRepo;
    @Mock private ProductRepository productRepo;
    @Mock private BranchRepository branchRepo;
    @Mock private ToppingRepository toppingRepo;
    @Mock private NotificationRepository notiRepo;
    @Mock private SimpMessagingTemplate messaging;
    @Mock private VoucherService voucherService;
    @Mock private MailService mailService;
    @Mock private VnPayService vnPayService;
    @Mock private CartService cartService;
    @InjectMocks private OrderService service;

    private User user;
    private Branch branch;
    private Product product;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(service, "shippingFee", 25000L);
        ReflectionTestUtils.setField(service, "freeShipFrom", 0L);
        ReflectionTestUtils.setField(service, "shipBaseKm", 3.0);
        ReflectionTestUtils.setField(service, "shipPerKm", 5000L);
        ReflectionTestUtils.setField(service, "shipMaxFee", 60000L);
        ReflectionTestUtils.setField(service, "pointValue", 200L);

        user = new User();
        user.setId(1L);
        user.setEmail("a@b.vn");
        user.setFullName("Khach A");
        user.setLoyaltyPoints(100);

        branch = new Branch();
        branch.setId(7L);
        branch.setName("Q1");
        branch.setActive(true);
        branch.setLatitude(10.7743);
        branch.setLongitude(106.7040);

        product = new Product();
        product.setId(5L);
        product.setName("Tra sua");
        product.setBasePrice(new BigDecimal("50000"));
        product.setActive(true);
        product.setOutOfStock(false);

        when(userRepo.findByEmail("a@b.vn")).thenReturn(Optional.of(user));
        when(branchRepo.findById(7L)).thenReturn(Optional.of(branch));
        when(productRepo.findById(5L)).thenReturn(Optional.of(product));
        when(orderRepo.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    private OrderRequest request() {
        CartItem ci = new CartItem();
        ci.setProductId(5L);
        ci.setQuantity(2);
        OrderRequest r = new OrderRequest();
        r.setItems(List.of(ci));
        r.setBranchId(7L);
        r.setDeliveryMethod(Order.METHOD_DELIVERY);
        r.setShippingAddress("1 Nguyen Hue");
        r.setPhone("0912345678");
        r.setPaymentMethod("COD");
        r.setAgreeTerms(true);
        return r;
    }

    /* ---------- phí ship ---------- */

    @Test
    void pickupIsFreeAndDeliveryIsFlatWithoutDistance() {
        BigDecimal sub = new BigDecimal("100000");
        assertEquals(0, BigDecimal.ZERO.compareTo(service.shippingFeeFor(Order.METHOD_PICKUP, sub)));
        assertEquals(0, new BigDecimal("25000").compareTo(service.shippingFeeFor(Order.METHOD_DELIVERY, sub)));
    }

    @Test
    void shippingFeeGrowsWithDistanceAndIsCapped() {
        BigDecimal sub = new BigDecimal("100000");
        assertEquals(0, new BigDecimal("25000").compareTo(service.shippingFeeFor(Order.METHOD_DELIVERY, sub, 2.5)));
        // 7.2km: vượt 3km -> 4.2 -> làm tròn lên 5 km x 5000 = 25000, cộng phí gốc = 50000
        assertEquals(0, new BigDecimal("50000").compareTo(service.shippingFeeFor(Order.METHOD_DELIVERY, sub, 7.2)));
        assertEquals(0, new BigDecimal("60000").compareTo(service.shippingFeeFor(Order.METHOD_DELIVERY, sub, 25.0)));
    }

    @Test
    void freeShipThresholdWins() {
        ReflectionTestUtils.setField(service, "freeShipFrom", 200000L);
        assertEquals(0, BigDecimal.ZERO.compareTo(
                service.shippingFeeFor(Order.METHOD_DELIVERY, new BigDecimal("250000"), 10.0)));
    }

    /* ---------- đặt hàng ---------- */

    @Test
    void placeOrderAppliesVoucherPointsAndShipping() {
        OrderRequest r = request();
        r.setVoucherCode("GIAM10K");
        r.setUsePoints(10);

        Voucher v = new Voucher();
        v.setId(3L);
        v.setCode("GIAM10K");
        when(voucherService.validate(eq("GIAM10K"), any())).thenReturn(v);
        when(voucherService.discountOf(eq(v), any())).thenReturn(new BigDecimal("10000"));
        when(userRepo.spendPoints(1L, 10)).thenReturn(1);

        Order o = service.placeOrder("a@b.vn", r);

        // 2 x 50.000 = 100.000 - 10.000 (voucher) - 2.000 (10 điểm x 200) + 25.000 ship
        assertEquals(0, new BigDecimal("113000").compareTo(o.getTotalAmount()));
        assertEquals(10, o.getPointsUsed());
        assertEquals("GIAM10K", o.getVoucherCode());
        verify(voucherService).consume(v);
        verify(userRepo).spendPoints(1L, 10);
    }

    @Test
    void pointsAreCappedByGoodsValueAndOwnedPoints() {
        user.setLoyaltyPoints(3);
        OrderRequest r = request();
        r.setUsePoints(999);
        when(userRepo.spendPoints(1L, 3)).thenReturn(1);

        Order o = service.placeOrder("a@b.vn", r);

        assertEquals(3, o.getPointsUsed());
        assertEquals(0, new BigDecimal("124400").compareTo(o.getTotalAmount())); // 100.000 - 600 + 25.000
    }

    @Test
    void placeOrderFailsWhenPointsWereSpentConcurrently() {
        OrderRequest r = request();
        r.setUsePoints(10);
        when(userRepo.spendPoints(1L, 10)).thenReturn(0);
        assertThrows(IllegalArgumentException.class, () -> service.placeOrder("a@b.vn", r));
    }

    @Test
    void placeOrderRequiresTermsAndValidPhone() {
        OrderRequest r = request();
        r.setAgreeTerms(false);
        assertThrows(IllegalArgumentException.class, () -> service.placeOrder("a@b.vn", r));

        OrderRequest r2 = request();
        r2.setPhone("123");
        assertThrows(IllegalArgumentException.class, () -> service.placeOrder("a@b.vn", r2));
    }

    @Test
    void cashOnlyForPickupAndCodOnlyForDelivery() {
        OrderRequest r = request();
        r.setPaymentMethod("CASH");
        assertThrows(IllegalArgumentException.class, () -> service.placeOrder("a@b.vn", r));

        OrderRequest r2 = request();
        r2.setDeliveryMethod(Order.METHOD_PICKUP);
        r2.setPaymentMethod("COD");
        assertThrows(IllegalArgumentException.class, () -> service.placeOrder("a@b.vn", r2));
    }

    @Test
    void vnpayRejectedWhenNotConfigured() {
        when(vnPayService.enabled()).thenReturn(false);
        OrderRequest r = request();
        r.setPaymentMethod("VNPAY");
        assertThrows(IllegalArgumentException.class, () -> service.placeOrder("a@b.vn", r));
    }

    /* ---------- hủy đơn ---------- */

    private Order pendingOrder() {
        Order o = new Order();
        o.setId(10L);
        o.setOrderCode("UTE1");
        o.setUser(user);
        o.setStatus(Order.STATUS_PENDING);
        o.setVoucherCode("GIAM10K");
        o.setPointsUsed(10);
        o.setTotalAmount(new BigDecimal("113000"));
        when(orderRepo.findById(10L)).thenReturn(Optional.of(o));
        return o;
    }

    @Test
    void cancelRefundsVoucherAndPoints() {
        Order o = pendingOrder();
        service.cancelByCustomer(10L, "a@b.vn");
        assertEquals(Order.STATUS_CANCELLED, o.getStatus());
        verify(voucherService).release("GIAM10K");
        verify(userRepo).addPoints(1L, 10);
    }

    @Test
    void cannotCancelOnceStoreConfirmedOrPaid() {
        Order o = pendingOrder();
        o.setStatus(Order.STATUS_PREPARING);
        assertThrows(IllegalArgumentException.class, () -> service.cancelByCustomer(10L, "a@b.vn"));

        o.setStatus(Order.STATUS_PENDING);
        o.setPaymentStatus("PAID");
        assertThrows(IllegalArgumentException.class, () -> service.cancelByCustomer(10L, "a@b.vn"));
        verify(userRepo, never()).addPoints(anyLong(), anyInt());
    }

    @Test
    void cannotCancelSomeoneElsesOrder() {
        pendingOrder();
        assertThrows(AccessDeniedException.class, () -> service.cancelByCustomer(10L, "other@b.vn"));
    }

    @Test
    void cancelledOrderCannotBeReopenedByStaff() {
        Order o = pendingOrder();
        o.setStatus(Order.STATUS_CANCELLED);
        assertThrows(IllegalArgumentException.class, () -> service.updateStatus(10L, Order.STATUS_PREPARING));
    }

    /* ---------- điểm thưởng ---------- */

    @Test
    void pointsAwardedOnlyOnceOnCompletion() {
        Order o = pendingOrder();
        o.setShippingFee(new BigDecimal("25000"));
        o.setTotalAmount(new BigDecimal("125000")); // hàng 100.000 => 10 điểm
        user.setLoyaltyPoints(0);

        service.updateStatus(10L, Order.STATUS_COMPLETED);
        assertEquals(10, o.getPointsEarned());
        assertEquals(10, user.getPoints());

        service.updateStatus(10L, Order.STATUS_COMPLETED);
        assertEquals(10, user.getPoints(), "không được cộng điểm lần 2");
    }

    /* ---------- VNPay ---------- */

    @Test
    void vnpayResultIsIdempotentAndChecksAmount() {
        Order o = new Order();
        o.setId(11L);
        o.setOrderCode("UTE999");
        o.setUser(user);
        o.setPaymentMethod("VNPAY");
        o.setTotalAmount(new BigDecimal("113000"));
        when(orderRepo.findByOrderCode("UTE999")).thenReturn(Optional.of(o));

        assertEquals("04", service.recordVnPay("UTE999R1", 100L, true, "T1").rspCode());
        assertNotEquals("PAID", o.getPaymentStatus());

        OrderService.PaymentResult ok = service.recordVnPay("UTE999R1", 11300000L, true, "T1");
        assertEquals("00", ok.rspCode());
        assertEquals("PAID", o.getPaymentStatus());

        assertEquals("02", service.recordVnPay("UTE999R2", 11300000L, true, "T2").rspCode());
        assertEquals("01", service.recordVnPay("NOPE", 1L, true, "T3").rspCode());
    }

    @Test
    void failedPaymentLeavesOrderUnpaid() {
        Order o = new Order();
        o.setId(12L);
        o.setOrderCode("UTE888");
        o.setUser(user);
        o.setPaymentMethod("VNPAY");
        o.setTotalAmount(new BigDecimal("50000"));
        when(orderRepo.findByOrderCode("UTE888")).thenReturn(Optional.of(o));

        OrderService.PaymentResult r = service.recordVnPay("UTE888R1", 5000000L, false, null);
        assertFalse(r.success());
        assertNotEquals("PAID", o.getPaymentStatus());
    }
}
