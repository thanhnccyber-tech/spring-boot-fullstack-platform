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

    @Autowired private OrderRepository orderRepository;
    @Autowired private OrderDetailRepository orderDetailRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private ProductRepository productRepository;
    @Autowired private BranchRepository branchRepository;
    @Autowired private ToppingRepository toppingRepository;
    @Autowired private NotificationRepository notificationRepository;
    @Autowired private SimpMessagingTemplate messagingTemplate;

    @Transactional
    public Order placeOrder(String email, OrderRequest req) {
        User user = userRepository.findByEmail(email).orElseThrow();
        Branch branch = branchRepository.findById(req.getBranchId()).orElseThrow();

        Order order = new Order();
        order.setOrderCode("UTE" + System.currentTimeMillis());
        order.setUser(user);
        order.setBranch(branch);
        order.setShippingAddress(req.getShippingAddress());
        order.setPaymentMethod(req.getPaymentMethod());
        order.setNote(req.getNote());
        order.setStatus("PENDING");
        order.setTotalAmount(BigDecimal.ZERO);

        Order saved = orderRepository.save(order);

        BigDecimal total = BigDecimal.ZERO;
        List<OrderDetail> details = new ArrayList<>();

        for (CartItem ci : req.getItems()) {
            Product p = productRepository.findById(ci.getProductId()).orElseThrow();
            OrderDetail od = new OrderDetail();
            od.setOrder(saved);
            od.setProduct(p);
            od.setQuantity(ci.getQuantity());
            od.setSize(ci.getSize());
            od.setSugarLevel(ci.getSugarLevel());
            od.setIceLevel(ci.getIceLevel());

            BigDecimal unit = p.getBasePrice();

            // Phụ thu theo Size
            if ("M".equalsIgnoreCase(ci.getSize())) {
                unit = unit.add(SIZE_M_SURCHARGE);
            } else if ("L".equalsIgnoreCase(ci.getSize())) {
                unit = unit.add(SIZE_L_SURCHARGE);
            }

            // Topping
            if (ci.getToppingIds() != null && !ci.getToppingIds().isEmpty()) {
                List<Topping> toppings = toppingRepository.findAllById(ci.getToppingIds());
                od.setToppings(toppings);
                BigDecimal tSum = toppings.stream()
                        .map(Topping::getPrice)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                unit = unit.add(tSum);
            }
            od.setUnitPrice(unit);
            od.setSubtotal(unit.multiply(BigDecimal.valueOf(ci.getQuantity())));
            details.add(od);
            total = total.add(od.getSubtotal());
        }
        orderDetailRepository.saveAll(details);
        saved.setTotalAmount(total);
        orderRepository.save(saved);

        // Gửi thông báo realtime đến admin/staff
        Map<String, Object> noti = new HashMap<>();
        noti.put("orderId", saved.getId());
        noti.put("orderCode", saved.getOrderCode());
        noti.put("customer", user.getFullName());
        noti.put("total", saved.getTotalAmount());
        noti.put("createdAt", saved.getCreatedAt().toString());
        messagingTemplate.convertAndSend("/topic/orders", noti);

        return saved;
    }

    public List<Order> ordersOf(String email) {
        User u = userRepository.findByEmail(email).orElseThrow();
        return orderRepository.findByUserIdOrderByCreatedAtDesc(u.getId());
    }

    public List<Order> all() {
        return orderRepository.findAll();
    }

    public Order get(Long id) { return orderRepository.findById(id).orElseThrow(); }

    @Transactional
    public void updateStatus(Long id, String status) {
        Order o = orderRepository.findById(id).orElseThrow();
        o.setStatus(status);
        o.setUpdatedAt(LocalDateTime.now());
        orderRepository.save(o);

        Map<String, Object> msg = new HashMap<>();
        msg.put("orderId", o.getId());
        msg.put("orderCode", o.getOrderCode());
        msg.put("status", status);
        msg.put("updatedAt", o.getUpdatedAt().toString());

        messagingTemplate.convertAndSend(
                "/topic/order-status/" + o.getUser().getId(), msg);

        Notification n = new Notification();
        n.setUserId(o.getUser().getId());
        n.setTitle("Đơn hàng " + o.getOrderCode());
        n.setContent("Trạng thái mới: " + status);
        n.setType("ORDER");
        notificationRepository.save(n);
    }
}