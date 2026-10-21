package vn.utetra.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.utetra.dto.CartItem;
import vn.utetra.dto.OrderRequest;
import vn.utetra.entity.*;
import vn.utetra.repository.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class OrderService {

    private static final BigDecimal SIZE_M_SURCHARGE = new BigDecimal("5000");
    private static final BigDecimal SIZE_L_SURCHARGE = new BigDecimal("10000");

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
            od.setSize(ci.getSize());
            od.setSugarLevel(ci.getSugarLevel());
            od.setIceLevel(ci.getIceLevel());

            BigDecimal unit = p.getBasePrice();
            if ("M".equalsIgnoreCase(ci.getSize())) unit = unit.add(SIZE_M_SURCHARGE);
            else if ("L".equalsIgnoreCase(ci.getSize())) unit = unit.add(SIZE_L_SURCHARGE);

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

    /** Đơn hàng của 1 user (fetch sẵn user + branch cho JSP) */
    public List<Order> ordersOf(String email) {
        User u = userRepo.findByEmail(email).orElseThrow();
        return orderRepo.findByUserIdWithUser(u.getId());
    }

    /** Đơn hàng của 1 chi nhánh (fetch sẵn user + branch) */
    public List<Order> ordersForBranch(Long branchId) {
        return orderRepo.findByBranchIdWithUser(branchId);
    }

    /** Toàn bộ đơn (admin, fetch sẵn user + branch) */
    public List<Order> allWithUser() {
        return orderRepo.findAllWithUser();
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

        Map<String, Object> msg = new HashMap<>();
        msg.put("orderId", o.getId());
        msg.put("orderCode", o.getOrderCode());
        msg.put("status", status);
        msg.put("updatedAt", o.getUpdatedAt().toString());
        messaging.convertAndSend("/topic/order-status/" + o.getUser().getId(), msg);

        Notification n = new Notification();
        n.setUserId(o.getUser().getId());
        n.setTitle("Đơn hàng " + o.getOrderCode());
        n.setContent("Trạng thái mới: " + status);
        n.setType("ORDER");
        notiRepo.save(n);
    }
}