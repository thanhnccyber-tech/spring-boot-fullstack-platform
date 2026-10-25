package vn.utetra.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.regex.Pattern;
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
    @Autowired private VoucherService voucherService;

    /** Phí giao hàng cố định (đ). Đặt trong application.properties: shop.shipping-fee */
    @Value("${shop.shipping-fee:25000}")
    private long shippingFee;

    /** Miễn phí giao hàng khi tiền hàng từ mức này trở lên; 0 = không áp dụng. */
    @Value("${shop.free-ship-from:0}")
    private long freeShipFrom;

    /** 10.000đ chi tiêu = 1 điểm (đúng quy tắc hội viên mô tả ở trang Hội viên). */
    public static final long POINT_UNIT = 10000L;

    private static final Pattern PHONE = Pattern.compile("^(0|\\+84)[0-9]{9,10}$");
    private static final Pattern EMAIL = Pattern.compile("^[^@\\s]{1,64}@[^@\\s]{1,255}$");
    private static final DateTimeFormatter FMT_SLOT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    /** Phí ship cho một đơn: đến lấy = 0; giao hàng = phí cố định (hoặc 0 nếu đủ mức miễn phí). */
    public BigDecimal shippingFeeFor(String method, BigDecimal subtotal) {
        if (Order.METHOD_PICKUP.equals(method)) return BigDecimal.ZERO;
        if (freeShipFrom > 0 && subtotal != null && subtotal.compareTo(BigDecimal.valueOf(freeShipFrom)) >= 0)
            return BigDecimal.ZERO;
        return BigDecimal.valueOf(shippingFee);
    }

    public long getShippingFee() { return shippingFee; }
    public long getFreeShipFrom() { return freeShipFrom; }

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

        String method = req.getDeliveryMethod() == null ? Order.METHOD_DELIVERY : req.getDeliveryMethod();
        if (!Order.METHOD_DELIVERY.equals(method) && !Order.METHOD_PICKUP.equals(method))
            throw new IllegalArgumentException("Phương thức nhận hàng không hợp lệ.");
        req.setDeliveryMethod(method);

        String phone = req.getPhone() == null ? "" : req.getPhone().trim().replace(" ", "");
        if (!PHONE.matcher(phone).matches())
            throw new IllegalArgumentException("Số điện thoại không hợp lệ (ví dụ 0912345678).");
        req.setPhone(phone);

        String addr = req.getShippingAddress() == null ? "" : req.getShippingAddress().trim();
        if (Order.METHOD_DELIVERY.equals(method) && addr.isEmpty())
            throw new IllegalArgumentException("Vui lòng nhập địa chỉ nhận hàng.");
        if (addr.length() > 500)
            throw new IllegalArgumentException("Địa chỉ nhận hàng tối đa 500 ký tự.");
        req.setShippingAddress(addr);

        if (req.getPaymentMethod() == null || !PAYMENT_METHODS.contains(req.getPaymentMethod()))
            throw new IllegalArgumentException("Phương thức thanh toán không hợp lệ.");
        // Tiền mặt chỉ khi đến lấy tại quầy; COD chỉ khi giao tận nơi
        if (Order.METHOD_PICKUP.equals(method) && "COD".equals(req.getPaymentMethod()))
            throw new IllegalArgumentException("Đơn đến lấy tại cửa hàng không dùng COD, vui lòng chọn cách thanh toán khác.");
        if (Order.METHOD_DELIVERY.equals(method) && "CASH".equals(req.getPaymentMethod()))
            throw new IllegalArgumentException("Tiền mặt tại quầy chỉ áp dụng khi đến lấy tại cửa hàng.");

        if (!req.isAgreeTerms())
            throw new IllegalArgumentException("Bạn cần đồng ý với điều khoản và chính sách để đặt hàng.");

        if (req.isVat()) {
            String comp = req.getVatCompany() == null ? "" : req.getVatCompany().trim();
            String tax = req.getVatTaxCode() == null ? "" : req.getVatTaxCode().trim();
            String mail = req.getVatEmail() == null ? "" : req.getVatEmail().trim();
            if (comp.isEmpty() || comp.length() > 200)
                throw new IllegalArgumentException("Hoá đơn VAT: vui lòng nhập tên công ty (tối đa 200 ký tự).");
            if (!tax.matches("[0-9-]{10,14}"))
                throw new IllegalArgumentException("Hoá đơn VAT: mã số thuế không hợp lệ (10-14 chữ số).");
            if (!EMAIL.matcher(mail).matches())
                throw new IllegalArgumentException("Hoá đơn VAT: email nhận hoá đơn không hợp lệ.");
            req.setVatCompany(comp); req.setVatTaxCode(tax); req.setVatEmail(mail);
        }

        String note = req.getNote() == null ? "" : req.getNote().trim();
        if (note.length() > 1000)
            throw new IllegalArgumentException("Ghi chú tối đa 1000 ký tự.");
        req.setNote(note.isEmpty() ? null : note);
    }

    /** "ASAP" hoặc giờ hẹn hợp lệ (từ 20 phút đến 3 ngày tới). Trả về chuỗi để lưu. */
    private String parseDeliveryTime(String raw) {
        if (raw == null || raw.isBlank() || "ASAP".equalsIgnoreCase(raw.trim())) return "ASAP";
        try {
            LocalDateTime t = LocalDateTime.parse(raw.trim());
            if (t.isBefore(LocalDateTime.now().plusMinutes(20)))
                throw new IllegalArgumentException("Giờ nhận hàng phải sau thời điểm hiện tại ít nhất 20 phút.");
            if (t.isAfter(LocalDateTime.now().plusDays(3)))
                throw new IllegalArgumentException("Chỉ nhận hẹn giờ trong vòng 3 ngày tới.");
            return t.format(FMT_SLOT);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Giờ nhận hàng không hợp lệ.");
        }
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
        order.setDeliveryMethod(req.getDeliveryMethod());
        order.setPhone(req.getPhone());
        order.setDeliveryTime(parseDeliveryTime(req.getDeliveryTime()));
        if (req.isVat()) {
            order.setVatInfo("Công ty: " + req.getVatCompany() + " | MST: " + req.getVatTaxCode()
                    + " | Email: " + req.getVatEmail());
        }
        if (Order.METHOD_PICKUP.equals(req.getDeliveryMethod()) && req.getShippingAddress().isEmpty()) {
            order.setShippingAddress("Nhận tại cửa hàng: " + branch.getName() + " - " + branch.getAddress());
        }
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

        // Mã giảm giá + phí giao hàng: tính lại hoàn toàn ở server, không tin số tiền từ trình duyệt
        BigDecimal discount = BigDecimal.ZERO;
        if (req.getVoucherCode() != null && !req.getVoucherCode().isBlank()) {
            Voucher v = voucherService.validate(req.getVoucherCode(), total);
            discount = voucherService.discountOf(v, total);
            voucherService.consume(v);
            saved.setVoucherCode(v.getCode());
        }
        BigDecimal fee = shippingFeeFor(req.getDeliveryMethod(), total);
        saved.setShippingFee(fee);
        saved.setDiscountAmount(discount);
        saved.setTotalAmount(total.subtract(discount).add(fee));
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
        awardPointsIfCompleted(o);
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

    /** Cộng điểm hội viên 1 lần duy nhất khi đơn hoàn thành: mỗi 10.000đ tiền hàng (sau giảm giá) = 1 điểm, làm tròn xuống. */
    private void awardPointsIfCompleted(Order o) {
        if (!Order.STATUS_COMPLETED.equals(o.getStatus())) return;
        if (o.getPointsEarned() != null || o.getUser() == null) return;
        BigDecimal paidGoods = o.getTotalAmount() == null ? BigDecimal.ZERO : o.getTotalAmount();
        if (o.getShippingFee() != null) paidGoods = paidGoods.subtract(o.getShippingFee());
        int pts = (int) Math.max(0, paidGoods.longValue() / POINT_UNIT);
        o.setPointsEarned(pts);
        if (pts > 0) {
            User u = o.getUser();
            u.setLoyaltyPoints(u.getPoints() + pts);
            userRepo.save(u);
            saveNotification(o, "Bạn được cộng " + pts + " điểm hội viên cho đơn này.");
        }
    }
}
