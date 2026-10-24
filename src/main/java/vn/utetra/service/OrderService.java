package vn.utetra.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.utetra.dto.CartItem;
import vn.utetra.dto.OrderRequest;
import vn.utetra.entity.*;
import vn.utetra.repository.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

@Service
public class OrderService {

    @Autowired private OrderRepository orderRepo;
    @Autowired private OrderDetailRepository orderDetailRepo;
    @Autowired private UserRepository userRepo;
    @Autowired private ProductRepository productRepo;
    @Autowired private BranchRepository branchRepo;
    @Autowired private ToppingRepository toppingRepo;
    @Autowired private NotificationRepository notiRepo;
    @Autowired private SimpMessagingTemplate messaging;

    private static final Set<String> PAYMENT_METHODS = Set.of("CASH", "BANK", "COD");
    private static final Set<String> STATUSES = Set.of(
            Order.STATUS_PENDING, Order.STATUS_PREPARING, Order.STATUS_DELIVERING,
            Order.STATUS_COMPLETED, Order.STATUS_CANCELLED, Order.STATUS_FAILED);
    private static final int MAX_ITEMS = 30;
    private static final int MAX_QTY = 50;

    /** Kiểm tra dữ liệu đặt hàng ở phía server (không tin dữ liệu từ form). */
    private void validateOrderRequest(OrderRequest req) {
        if (req == null || req.getItems() == null || req.getItems().isEmpty())
            throw new IllegalArgumentException("Giỏ hàng trống.");
        if (req.getItems().size() > MAX_ITEMS)
            throw new IllegalArgumentException("Giỏ hàng tối đa " + MAX_ITEMS + " món.");
        if (req.getBranchId() == null)
            throw new IllegalArgumentException("Vui lòng chọn chi nhánh.");

        String addr = req.getShippingAddress() == null ? "" : req.getShippingAddress().trim();
        if (addr.isEmpty() || addr.length() > 500)
            throw new IllegalArgumentException("Địa chỉ nhận hàng không hợp lệ (tối đa 500 ký tự).");
        req.setShippingAddress(addr);

        if (req.getPaymentMethod() == null || !PAYMENT_METHODS.contains(req.getPaymentMethod()))
            throw new IllegalArgumentException("Phương thức thanh toán không hợp lệ.");

        String note = req.getNote() == null ? "" : req.getNote().trim();
        if (note.length() > 1000)
            throw new IllegalArgumentException("Ghi chú tối đa 1000 ký tự.");
        req.setNote(note.isEmpty() ? null : note);
    }

    @Transactional
    public Order placeOrder(String email, OrderRequest req) {
        validateOrderRequest(req);
        User user = userRepo.findByEmail(email).orElseThrow();
        Branch branch = branchRepo.findById(req.getBranchId())
                .filter(b -> Boolean.TRUE.equals(b.getActive()))
                .orElseThrow(() -> new IllegalArgumentException("Chi nhánh không tồn tại hoặc đã ngừng hoạt động."));

        Order order = new Order();
        order.setOrderCode("UTE" + System.currentTimeMillis());
        order.setUser(user);
        order.setBranch(branch);
        order.setShippingAddress(req.getShippingAddress());
        order.setPaymentMethod(req.getPaymentMethod());
        order.setNote(req.getNote());
        order.setStatus(Order.STATUS_PENDING);
        order.setTotalAmount(BigDecimal.ZERO);

        Order saved = orderRepo.save(order);

        BigDecimal total = BigDecimal.ZERO;
        List<OrderDetail> details = new ArrayList<>();

        for (CartItem ci : req.getItems()) {
            Product p = productRepo.findById(ci.getProductId())
                    .orElseThrow(() -> new IllegalArgumentException("Có sản phẩm trong giỏ không còn tồn tại."));
            if (!Boolean.TRUE.equals(p.getActive()) || Boolean.TRUE.equals(p.getOutOfStock()))
                throw new IllegalArgumentException("Sản phẩm \"" + p.getName() + "\" hiện không còn bán.");
            if (ci.getQuantity() == null || ci.getQuantity() < 1 || ci.getQuantity() > MAX_QTY)
                throw new IllegalArgumentException("Số lượng mỗi món phải từ 1 đến " + MAX_QTY + ".");
            OrderDetail od = new OrderDetail();
            od.setOrder(saved);
            od.setProduct(p);
            od.setQuantity(ci.getQuantity());
            od.setSize(ci.getSizeName());
            od.setSugarLevel(ci.getSugarName());
            od.setIceLevel(ci.getIceName());

            BigDecimal unit = p.getBasePrice();
            if (ci.getSizeExtraPrice() != null) unit = unit.add(ci.getSizeExtraPrice());

            if (ci.getToppingIds() != null && !ci.getToppingIds().isEmpty()) {
                Set<Topping> tops = new LinkedHashSet<>(toppingRepo.findAllById(ci.getToppingIds()));
                od.setToppings(tops);
                unit = unit.add(tops.stream().map(Topping::getPrice)
                        .reduce(BigDecimal.ZERO, BigDecimal::add));
            }
            od.setUnitPrice(unit);
            od.setSubtotal(unit.multiply(BigDecimal.valueOf(ci.getQuantity())));
            details.add(od);
            total = total.add(od.getSubtotal());
        }
        orderDetailRepo.saveAll(details);
        saved.setTotalAmount(total);
        orderRepo.save(saved);

        Map<String, Object> noti = new HashMap<>();
        noti.put("orderId", saved.getId());
        noti.put("orderCode", saved.getOrderCode());
        noti.put("customer", user.getFullName());
        noti.put("total", saved.getTotalAmount());
        noti.put("createdAt", saved.getCreatedAt().toString());
        messaging.convertAndSend("/topic/orders", noti);

        return saved;
    }

    public List<Order> ordersOf(String email) {
        User u = userRepo.findByEmail(email).orElseThrow();
        return orderRepo.findByUserIdWithUser(u.getId());
    }

    public List<Order> ordersForBranch(Long branchId) {
        return orderRepo.findByBranchIdWithUser(branchId);
    }

    public List<Order> allWithUser() {
        return orderRepo.findAllWithUser();
    }

    public Page<Order> searchForAdmin(String kw, String status, Long branchId,
                                      LocalDate fromDate, LocalDate toDate,
                                      int page, int size) {
        String kwP = (kw == null || kw.isBlank()) ? null : kw.trim();
        String statusP = (status == null || status.isBlank()) ? null : status;

        LocalDateTime from = fromDate == null ? null : fromDate.atStartOfDay();
        LocalDateTime to = toDate == null ? null : toDate.atTime(LocalTime.MAX);

        Sort sort = Sort.by("createdAt").descending();
        return orderRepo.searchForAdmin(kwP, statusP, branchId, from, to,
                PageRequest.of(Math.max(page, 0), size, sort));
    }

    public Map<String, Long> countByStatus(Long branchId) {
        Map<String, Long> map = new LinkedHashMap<>();
        List<Object[]> rows = (branchId == null)
                ? orderRepo.countByStatus()
                : orderRepo.countByStatusForBranch(branchId);
        long total = 0;
        for (Object[] row : rows) {
            long c = ((Number) row[1]).longValue();
            map.put((String) row[0], c);
            total += c;
        }
        map.put("ALL", total);
        return map;
    }

    public Order get(Long id) {
        return orderRepo.findByIdWithDetails(id).orElseThrow();
    }

    @Transactional
    public void updateStatus(Long id, String status) {
        if (status == null || !STATUSES.contains(status))
            throw new IllegalArgumentException("Trạng thái đơn hàng không hợp lệ.");
        Order o = orderRepo.findById(id).orElseThrow();
        o.setStatus(status);
        o.setUpdatedAt(LocalDateTime.now());
        orderRepo.save(o);

        pushStatusToCustomer(o);
        saveNotification(o, "Trạng thái mới: " + status);
    }

    @Transactional
    public void assignShipper(Long orderId, Long shipperId) {
        Order o = orderRepo.findById(orderId).orElseThrow();

        if (shipperId == null) {
            o.setShipper(null);
            o.setUpdatedAt(LocalDateTime.now());
            orderRepo.save(o);
            return;
        }

        User shipper = userRepo.findById(shipperId).orElseThrow();
        if (!shipper.isShipper()) {
            throw new RuntimeException("Người dùng không phải shipper");
        }

        o.setShipper(shipper);
        o.setUpdatedAt(LocalDateTime.now());
        orderRepo.save(o);

        pushStatusToCustomer(o);
        saveNotification(o, "Đơn hàng đã được phân công cho shipper: " + shipper.getFullName());
    }

    private void pushStatusToCustomer(Order o) {
        Map<String, Object> msg = new HashMap<>();
        msg.put("orderId", o.getId());
        msg.put("orderCode", o.getOrderCode());
        msg.put("status", o.getStatus());
        msg.put("updatedAt", o.getUpdatedAt().toString());
        messaging.convertAndSend("/topic/order-status/" + o.getUser().getId(), msg);
    }

    private void saveNotification(Order o, String content) {
        Notification n = new Notification();
        n.setUserId(o.getUser().getId());
        n.setTitle("Đơn hàng " + o.getOrderCode());
        n.setContent(content);
        n.setType("ORDER");
        notiRepo.save(n);
    }
}