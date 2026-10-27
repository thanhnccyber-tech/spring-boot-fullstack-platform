package vn.utetra.service;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.utetra.entity.Order;
import vn.utetra.entity.DeliveryFailure;
import vn.utetra.entity.User;
import vn.utetra.repository.DeliveryFailureRepository;
import vn.utetra.repository.OrderRepository;
import vn.utetra.repository.ShipperOrderRepository;
import vn.utetra.repository.UserRepository;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Nghiệp vụ của Shipper: xem đơn chờ nhận, nhận đơn, giao thành công, giao thất bại, thống kê.
 * Việc đổi trạng thái (kèm WebSocket báo khách, thông báo, email, cộng điểm) được giao cho
 * {@link OrderService#updateStatus(Long, String)} để dùng chung một luồng với màn hình Manager.
 */
@Service
public class ShipperService {

    public static final int MAX_REASON_LENGTH = 500;

    @Autowired private ShipperOrderRepository shipperRepo;
    @Autowired private OrderRepository orderRepo;
    @Autowired private UserRepository userRepo;
    @Autowired private DeliveryFailureRepository failureRepo;
    @Autowired private OrderService orderService;

    /**
     * Số liệu thống kê của shipper trong một khoảng thời gian.
     * Dùng class (có getter) thay vì record để JSP đọc được bằng ${stats.delivered}.
     */
    @Getter
    @AllArgsConstructor
    public static class Stats {
        private final long delivered;
        private final BigDecimal revenue;
        private final BigDecimal codCollected;
        private final long failed;
    }

    /* ==================== XÁC ĐỊNH SHIPPER ==================== */

    /** Lấy shipper đang đăng nhập; không phải shipper (hoặc bị khóa) thì từ chối. */
    public User shipperOf(String email) {
        User u = userRepo.findByEmail(email)
                .orElseThrow(() -> new AccessDeniedException("Không tìm thấy tài khoản"));
        if (!Boolean.TRUE.equals(u.getEnabled()) || !u.isShipper()) {
            throw new AccessDeniedException("Chỉ shipper mới được dùng chức năng này");
        }
        return u;
    }

    private static Long branchIdOf(User u) {
        return u.getBranch() == null ? null : u.getBranch().getId();
    }

    private static boolean isMine(Order o, User me) {
        return o.getShipper() != null && o.getShipper().getId().equals(me.getId());
    }

    private static boolean sameBranch(Order o, User me) {
        Long mine = branchIdOf(me);
        return mine != null && o.getBranch() != null && mine.equals(o.getBranch().getId());
    }

    /** Đơn nằm trong "hàng chờ nhận" của chi nhánh shipper: giao tận nơi, đã xác nhận, chưa ai nhận. */
    private static boolean isOpenForPickup(Order o, User me) {
        return !o.isPickup()
                && (Order.STATUS_PREPARING.equals(o.getStatus())
                    || Order.STATUS_READY.equals(o.getStatus()))
                && sameBranch(o, me)
                && (o.getShipper() == null || isMine(o, me));
    }

    /* ==================== DANH SÁCH ==================== */

    /** Shipper chưa được gán chi nhánh thì không có đơn nào để nhận. */
    @Transactional(readOnly = true)
    public boolean hasBranch(String email) {
        return branchIdOf(shipperOf(email)) != null;
    }

    /** Đơn chờ nhận tại chi nhánh của shipper. */
    @Transactional(readOnly = true)
    public List<Order> available(String email) {
        User me = shipperOf(email);
        Long branchId = branchIdOf(me);
        if (branchId == null) return List.of();
        return shipperRepo.findAvailable(branchId, me.getId());
    }

    /** Đơn shipper đã nhận và đang giao. */
    @Transactional(readOnly = true)
    public List<Order> delivering(String email) {
        User me = shipperOf(email);
        return shipperRepo.findByShipperAndStatuses(me.getId(), List.of(Order.STATUS_DELIVERING));
    }

    /** Lịch sử: đơn đã giao xong hoặc giao thất bại. */
    @Transactional(readOnly = true)
    public List<Order> history(String email) {
        User me = shipperOf(email);
        return shipperRepo.findByShipperAndStatuses(me.getId(),
                List.of(Order.STATUS_COMPLETED, Order.STATUS_FAILED));
    }

    @Transactional(readOnly = true)
    public List<DeliveryFailure> failureHistory(String email) {
        User me = shipperOf(email);
        return failureRepo.findHistoryByShipperId(me.getId());
    }

    @Transactional(readOnly = true)
    public List<DeliveryFailure> failureHistoryForOrder(Long orderId, String email) {
        User me = shipperOf(email);
        Order order = orderRepo.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn hàng."));
        if (!sameBranch(order, me) && !failureRepo.existsByOrder_IdAndShipper_Id(orderId, me.getId())) {
            throw new AccessDeniedException("Đơn hàng không thuộc chi nhánh của bạn");
        }
        return failureRepo.findHistoryByOrderId(orderId);
    }

    /** Chi tiết một đơn. Chỉ xem được đơn của mình hoặc đơn đang chờ nhận ở chi nhánh mình (chống truy cập chéo). */
    @Transactional(readOnly = true)
    public Order detail(Long orderId, String email) {
        User me = shipperOf(email);
        Order o = orderRepo.findByIdWithDetails(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn hàng."));
        if (!isMine(o, me) && !isOpenForPickup(o, me)
                && !failureRepo.existsByOrder_IdAndShipper_Id(orderId, me.getId())) {
            throw new AccessDeniedException("Bạn không có quyền xem đơn hàng này");
        }
        return o;
    }

    /* ==================== THAO TÁC ==================== */

    /** Nhận đơn: PREPARING/READY -> DELIVERING, gán shipper, báo realtime cho khách. */
    @Transactional
    public void accept(Long orderId, String email) {
        User me = shipperOf(email);
        Order o = lock(orderId);
        if (!sameBranch(o, me)) {
            throw new AccessDeniedException("Đơn hàng không thuộc chi nhánh của bạn");
        }
        if (!isOpenForPickup(o, me)) {
            throw new IllegalArgumentException("Đơn này không còn chờ nhận hoặc đã có shipper khác nhận.");
        }
        o.setShipper(me);
        orderService.updateStatus(orderId, Order.STATUS_DELIVERING);
    }

    /** Giao thành công: DELIVERING -> COMPLETED. Đơn COD được ghi nhận đã thu tiền. */
    @Transactional
    public void complete(Long orderId, String email) {
        Order o = lockOwnedDelivering(orderId, shipperOf(email));
        if ("COD".equals(o.getPaymentMethod()) && !"PAID".equals(o.getPaymentStatus())) {
            o.setPaymentStatus("PAID");
            o.setPaidAt(LocalDateTime.now());
        }
        o.setFailedReason(null);
        orderService.updateStatus(orderId, Order.STATUS_COMPLETED);
    }

    /** Giao thất bại: ghi nhận lần giao, bỏ shipper và đưa đơn về hàng chờ giao lại. */
    @Transactional
    public void fail(Long orderId, String email, String reason) {
        String r = reason == null ? "" : reason.trim();
        if (r.isEmpty()) throw new IllegalArgumentException("Vui lòng nhập lý do giao thất bại.");
        if (r.length() > MAX_REASON_LENGTH)
            throw new IllegalArgumentException("Lý do tối đa " + MAX_REASON_LENGTH + " ký tự.");
        Order o = lockOwnedDelivering(orderId, shipperOf(email));
        DeliveryFailure failure = new DeliveryFailure();
        failure.setOrder(o);
        failure.setShipper(o.getShipper());
        failure.setReason(r);
        failureRepo.save(failure);
        o.setFailedReason(r);
        o.setShipper(null);
        orderService.updateStatus(orderId, Order.STATUS_READY);
    }

    private Order lock(Long orderId) {
        return shipperRepo.findByIdForUpdate(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn hàng."));
    }

    /** Khóa đơn và kiểm tra: phải là đơn của chính shipper và đang ở trạng thái "đang giao". */
    private Order lockOwnedDelivering(Long orderId, User me) {
        Order o = lock(orderId);
        if (!isMine(o, me)) {
            throw new AccessDeniedException("Đơn hàng này không được giao cho bạn");
        }
        if (!Order.STATUS_DELIVERING.equals(o.getStatus())) {
            throw new IllegalArgumentException("Đơn không ở trạng thái đang giao nên không thể thao tác.");
        }
        return o;
    }

    /* ==================== THỐNG KÊ ==================== */

    @Transactional(readOnly = true)
    public Stats stats(String email, LocalDateTime from, LocalDateTime to) {
        User me = shipperOf(email);
        Object[] row = shipperRepo.deliveredStats(me.getId(), from, to).get(0);
        BigDecimal cod = shipperRepo.codCollected(me.getId(), from, to);
        long failed = failureRepo.countByShipperIdAndFailedAtBetween(me.getId(), from, to);
        return new Stats(
                ((Number) row[0]).longValue(),
                toMoney(row[1]),
                cod == null ? BigDecimal.ZERO : cod,
                failed);
    }

    private static BigDecimal toMoney(Object v) {
        return v == null ? BigDecimal.ZERO : new BigDecimal(v.toString());
    }

    @Transactional(readOnly = true)
    public Stats statsToday(String email) {
        LocalDate d = LocalDate.now();
        return stats(email, d.atStartOfDay(), d.atTime(LocalTime.MAX));
    }

    /** Tuần này: từ thứ Hai đến hết hôm nay. */
    @Transactional(readOnly = true)
    public Stats statsThisWeek(String email) {
        LocalDate today = LocalDate.now();
        LocalDate monday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        return stats(email, monday.atStartOfDay(), today.atTime(LocalTime.MAX));
    }

    @Transactional(readOnly = true)
    public Stats statsThisMonth(String email) {
        LocalDate today = LocalDate.now();
        return stats(email, today.withDayOfMonth(1).atStartOfDay(), today.atTime(LocalTime.MAX));
    }

    /** Số đơn giao thành công mỗi ngày trong {@code days} ngày gần nhất (kể cả hôm nay), cũ đến mới. */
    @Transactional(readOnly = true)
    public Map<LocalDate, Long> deliveredPerDay(String email, int days) {
        User me = shipperOf(email);
        LocalDate today = LocalDate.now();
        LocalDate start = today.minusDays(Math.max(days, 1) - 1L);

        Map<LocalDate, Long> result = new LinkedHashMap<>();
        for (LocalDate d = start; !d.isAfter(today); d = d.plusDays(1)) result.put(d, 0L);

        List<LocalDateTime> times = shipperRepo.deliveredTimes(
                me.getId(), start.atStartOfDay(), today.atTime(LocalTime.MAX));
        for (LocalDateTime t : times) result.merge(t.toLocalDate(), 1L, Long::sum);
        return result;
    }
}