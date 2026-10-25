package vn.utetra.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import vn.utetra.entity.Order;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Thanh toán VNPay (API 2.1.0). CHỈ bật khi khai báo vnpay.tmn-code và vnpay.hash-secret
 * (đăng ký tài khoản sandbox miễn phí tại sandbox.vnpayment.vn rồi đặt vào file .env).
 * Chữ ký HMAC-SHA512 được tính và kiểm tra ở server, trình duyệt không thể giả kết quả thanh toán.
 */
@Service
public class VnPayService {

    @Value("${vnpay.tmn-code:}")
    private String tmnCode;

    @Value("${vnpay.hash-secret:}")
    private String hashSecret;

    @Value("${vnpay.url:https://sandbox.vnpayment.vn/paymentv2/vpcpay.html}")
    private String payUrl;

    /** Địa chỉ công khai của website, VNPay sẽ chuyển khách về {baseUrl}/payment/vnpay-return */
    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    private static final ZoneId VN = ZoneId.of("Asia/Ho_Chi_Minh");
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    public boolean enabled() {
        return tmnCode != null && !tmnCode.isBlank() && hashSecret != null && !hashSecret.isBlank();
    }

    /** Mã giao dịch gửi VNPay: mỗi lần thanh toán một mã khác nhau (VNPay không cho trùng) nhưng luôn truy ngược được mã đơn. */
    public static String txnRef(String orderCode) {
        return orderCode + "R" + (System.currentTimeMillis() / 1000);
    }

    public static String orderCodeOf(String txnRef) {
        if (txnRef == null) return null;
        int i = txnRef.indexOf('R');
        return i > 0 ? txnRef.substring(0, i) : txnRef;
    }

    public String buildPaymentUrl(Order order, String ip) {
        if (!enabled()) throw new IllegalStateException("VNPay chưa được cấu hình.");
        LocalDateTime now = LocalDateTime.now(VN);
        Map<String, String> p = new TreeMap<>();
        p.put("vnp_Version", "2.1.0");
        p.put("vnp_Command", "pay");
        p.put("vnp_TmnCode", tmnCode);
        p.put("vnp_Amount", String.valueOf(order.getTotalAmount().longValue() * 100L));
        p.put("vnp_CurrCode", "VND");
        p.put("vnp_TxnRef", txnRef(order.getOrderCode()));
        p.put("vnp_OrderInfo", "Thanh toan don hang " + order.getOrderCode());
        p.put("vnp_OrderType", "other");
        p.put("vnp_Locale", "vn");
        p.put("vnp_ReturnUrl", baseUrl.replaceAll("/+$", "") + "/payment/vnpay-return");
        p.put("vnp_IpAddr", ip == null || ip.isBlank() ? "127.0.0.1" : ip);
        p.put("vnp_CreateDate", now.format(FMT));
        p.put("vnp_ExpireDate", now.plusMinutes(15).format(FMT));
        String data = join(p);
        return payUrl + "?" + data + "&vnp_SecureHash=" + hmac(data);
    }

    /** true nếu chữ ký trong tham số VNPay gửi về đúng. */
    public boolean verify(Map<String, String> params) {
        if (!enabled() || params == null) return false;
        String given = params.get("vnp_SecureHash");
        if (given == null || given.isBlank()) return false;
        Map<String, String> p = new TreeMap<>();
        for (Map.Entry<String, String> e : params.entrySet()) {
            String k = e.getKey();
            if (k.startsWith("vnp_") && !k.equals("vnp_SecureHash") && !k.equals("vnp_SecureHashType")
                    && e.getValue() != null && !e.getValue().isEmpty()) {
                p.put(k, e.getValue());
            }
        }
        String expected = hmac(join(p));
        return MessageDigest.isEqual(expected.getBytes(StandardCharsets.UTF_8),
                given.toLowerCase(Locale.ROOT).getBytes(StandardCharsets.UTF_8));
    }

    private static String join(Map<String, String> sorted) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> e : sorted.entrySet()) {
            if (e.getValue() == null || e.getValue().isEmpty()) continue;
            if (sb.length() > 0) sb.append('&');
            sb.append(e.getKey()).append('=')
              .append(URLEncoder.encode(e.getValue(), StandardCharsets.US_ASCII));
        }
        return sb.toString();
    }

    private String hmac(String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA512");
            mac.init(new SecretKeySpec(hashSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA512"));
            byte[] raw = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(raw.length * 2);
            for (byte b : raw) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            throw new IllegalStateException("Không tạo được chữ ký thanh toán.", e);
        }
    }
}
