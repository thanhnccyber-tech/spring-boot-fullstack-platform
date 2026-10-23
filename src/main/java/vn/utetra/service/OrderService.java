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

    @Transactional
    public Order placeOrder(String email, OrderRequest req) {
        User user = userRepo.findByEmail(email).orElseThrow();
        Branch branch = branchRepo.findById(req.getBranchId()).orElseThrow();

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
            Product p = productRepo.findById(ci.getProductId()).orElseThrow();
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