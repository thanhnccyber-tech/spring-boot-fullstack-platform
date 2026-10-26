package vn.utetra.service;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import vn.utetra.entity.Order;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class MomoService {

    private static final List<String> CALLBACK_FIELDS = List.of(
            "accessKey", "amount", "extraData", "message", "orderId", "orderInfo",
            "orderType", "partnerCode", "payType", "requestId", "responseTime",
            "resultCode", "transId");

    @Value("${momo.partner-code:}")
    private String partnerCode;

    @Value("${momo.access-key:}")
    private String accessKey;

    @Value("${momo.secret-key:}")
    private String secretKey;

    @Value("${momo.endpoint:https://test-payment.momo.vn/v2/gateway/api/create}")
    private String endpoint;

    @Value("${momo.ipn-url:}")
    private String ipnUrl;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    private final RestClient restClient = RestClient.create();

    public boolean enabled() {
        return present(partnerCode) && present(accessKey) && present(secretKey)
                && present(endpoint) && present(ipnUrl) && present(baseUrl);
    }

    public String createPaymentUrl(Order order) {
        if (!enabled()) throw new IllegalStateException("MoMo chưa được cấu hình.");

        String orderId = order.getOrderCode();
        String requestId = orderId + "M" + System.currentTimeMillis();
        String orderInfo = "Thanh toan don hang " + orderId;
        String redirectUrl = baseUrl.replaceAll("/+$", "") + "/payment/momo-return";
        String amount = String.valueOf(order.getTotalAmount().longValue());
        String extraData = "";
        String requestType = "captureWallet";

        String rawSignature = "accessKey=" + accessKey
                + "&amount=" + amount
                + "&extraData=" + extraData
                + "&ipnUrl=" + ipnUrl
                + "&orderId=" + orderId
                + "&orderInfo=" + orderInfo
                + "&partnerCode=" + partnerCode
                + "&redirectUrl=" + redirectUrl
                + "&requestId=" + requestId
                + "&requestType=" + requestType;

        Map<String, Object> request = Map.ofEntries(
                Map.entry("partnerCode", partnerCode),
                Map.entry("requestId", requestId),
                Map.entry("orderId", orderId),
                Map.entry("amount", Long.parseLong(amount)),
                Map.entry("orderInfo", orderInfo),
                Map.entry("redirectUrl", redirectUrl),
                Map.entry("ipnUrl", ipnUrl),
                Map.entry("requestType", requestType),
                Map.entry("extraData", extraData),
                Map.entry("lang", "vi"),
                Map.entry("signature", sign(rawSignature)));

        JsonNode response = restClient.post()
                .uri(endpoint)
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(JsonNode.class);
        if (response == null || !"0".equals(response.path("resultCode").asText())
                || !response.path("payUrl").isTextual()
                || response.path("payUrl").asText().isBlank()) {
            throw new IllegalStateException("MoMo không tạo được liên kết thanh toán.");
        }
        return response.path("payUrl").asText();
    }

    public boolean verifyCallback(Map<String, ?> params) {
        if (!enabled() || params == null) return false;
        if (!accessKey.equals(value(params.get("accessKey")))
                || !partnerCode.equals(value(params.get("partnerCode")))) return false;

        String given = value(params.get("signature"));
        if (given.isBlank()) return false;

        StringBuilder raw = new StringBuilder();
        for (String field : CALLBACK_FIELDS) {
            if (!params.containsKey(field)) return false;
            if (!raw.isEmpty()) raw.append('&');
            raw.append(field).append('=').append(value(params.get(field)));
        }
        String expected = sign(raw.toString());
        return MessageDigest.isEqual(expected.getBytes(StandardCharsets.UTF_8),
                given.toLowerCase(Locale.ROOT).getBytes(StandardCharsets.UTF_8));
    }

    private String sign(String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] bytes = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder result = new StringBuilder(bytes.length * 2);
            for (byte b : bytes) result.append(String.format("%02x", b));
            return result.toString();
        } catch (Exception e) {
            throw new IllegalStateException("Không tạo được chữ ký MoMo.", e);
        }
    }

    private static boolean present(String value) {
        return value != null && !value.isBlank();
    }

    private static String value(Object value) {
        return value == null ? "" : value.toString();
    }
}
