package vn.utetra.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class MomoServiceTest {

    private MomoService service;

    @BeforeEach
    void setUp() {
        service = new MomoService();
        ReflectionTestUtils.setField(service, "partnerCode", "MOMO_TEST");
        ReflectionTestUtils.setField(service, "accessKey", "access-test");
        ReflectionTestUtils.setField(service, "secretKey", "secret-test");
        ReflectionTestUtils.setField(service, "endpoint", "https://test-payment.momo.vn/v2/gateway/api/create");
        ReflectionTestUtils.setField(service, "ipnUrl", "https://example.test/payment/momo-ipn");
        ReflectionTestUtils.setField(service, "baseUrl", "https://example.test");
    }

    @Test
    void requiresAllCallbackConfiguration() {
        assertTrue(service.enabled());
        ReflectionTestUtils.setField(service, "ipnUrl", "");
        assertFalse(service.enabled());
        ReflectionTestUtils.setField(service, "ipnUrl", "https://example.test/payment/momo-ipn");
        ReflectionTestUtils.setField(service, "endpoint", "");
        assertFalse(service.enabled());
    }

    @Test
    void verifiesCallbackSignatureAndRejectsTampering() throws Exception {
        Map<String, String> callback = validCallback();
        callback.put("signature", sign(callback, "secret-test"));

        assertTrue(service.verifyCallback(callback));

        callback.put("amount", "1");
        assertFalse(service.verifyCallback(callback));
    }

    @Test
    void rejectsCallbackWithMissingSignedField() throws Exception {
        Map<String, String> callback = validCallback();
        callback.remove("extraData");
        callback.put("signature", sign(callback, "secret-test"));

        assertFalse(service.verifyCallback(callback));
    }

    private static Map<String, String> validCallback() {
        Map<String, String> callback = new LinkedHashMap<>();
        callback.put("accessKey", "access-test");
        callback.put("amount", "85000");
        callback.put("extraData", "");
        callback.put("message", "Successful.");
        callback.put("orderId", "UTE123");
        callback.put("orderInfo", "Thanh toan don hang UTE123");
        callback.put("orderType", "momo_wallet");
        callback.put("partnerCode", "MOMO_TEST");
        callback.put("payType", "qr");
        callback.put("requestId", "UTE123M456");
        callback.put("responseTime", "1780000000000");
        callback.put("resultCode", "0");
        callback.put("transId", "99887766");
        return callback;
    }

    private static String sign(Map<String, String> params, String secret) throws Exception {
        String raw = String.join("&", params.entrySet().stream()
                .filter(entry -> !"signature".equals(entry.getKey()))
                .map(entry -> entry.getKey() + "=" + entry.getValue())
                .toList());
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        StringBuilder signature = new StringBuilder();
        for (byte value : mac.doFinal(raw.getBytes(StandardCharsets.UTF_8))) {
            signature.append(String.format("%02x", value));
        }
        return signature.toString();
    }
}
