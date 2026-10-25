package vn.utetra.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import vn.utetra.entity.Order;

import java.math.BigDecimal;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class VnPayServiceTest {

    private VnPayService svc;

    @BeforeEach
    void setUp() {
        svc = new VnPayService();
        ReflectionTestUtils.setField(svc, "tmnCode", "TESTCODE");
        ReflectionTestUtils.setField(svc, "hashSecret", "SECRETKEY123");
        ReflectionTestUtils.setField(svc, "payUrl", "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html");
        ReflectionTestUtils.setField(svc, "baseUrl", "http://localhost:8080");
    }

    private static Map<String, String> parse(String url) {
        Map<String, String> m = new LinkedHashMap<>();
        String q = url.substring(url.indexOf('?') + 1);
        for (String kv : q.split("&")) {
            int i = kv.indexOf('=');
            m.put(kv.substring(0, i), URLDecoder.decode(kv.substring(i + 1), StandardCharsets.UTF_8));
        }
        return m;
    }

    private Order order() {
        Order o = new Order();
        o.setOrderCode("UTE1700000000000");
        o.setTotalAmount(new BigDecimal("113000"));
        return o;
    }

    @Test
    void disabledWithoutCredentials() {
        VnPayService off = new VnPayService();
        assertFalse(off.enabled());
        assertTrue(svc.enabled());
    }

    @Test
    void paymentUrlCarriesAmountInHundredthsAndValidSignature() {
        Map<String, String> p = parse(svc.buildPaymentUrl(order(), "1.2.3.4"));
        assertEquals("11300000", p.get("vnp_Amount"));
        assertEquals("TESTCODE", p.get("vnp_TmnCode"));
        assertTrue(p.get("vnp_TxnRef").startsWith("UTE1700000000000R"));
        assertTrue(svc.verify(p), "chữ ký do chính server tạo phải được chấp nhận");
    }

    @Test
    void tamperedParametersAreRejected() {
        Map<String, String> p = parse(svc.buildPaymentUrl(order(), "1.2.3.4"));
        p.put("vnp_Amount", "100");
        assertFalse(svc.verify(p));

        Map<String, String> q = parse(svc.buildPaymentUrl(order(), "1.2.3.4"));
        q.put("vnp_SecureHash", "00" + q.get("vnp_SecureHash").substring(2));
        assertFalse(svc.verify(q));

        Map<String, String> r = parse(svc.buildPaymentUrl(order(), "1.2.3.4"));
        r.remove("vnp_SecureHash");
        assertFalse(svc.verify(r));
    }

    @Test
    void orderCodeIsRecoveredFromTxnRef() {
        String ref = VnPayService.txnRef("UTE123");
        assertEquals("UTE123", VnPayService.orderCodeOf(ref));
        assertNull(VnPayService.orderCodeOf(null));
    }
}
