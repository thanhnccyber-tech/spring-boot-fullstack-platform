package vn.utetra.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import vn.utetra.entity.Voucher;
import vn.utetra.repository.VoucherRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VoucherServiceTest {

    @Mock private VoucherRepository repo;
    @InjectMocks private VoucherService service;

    private Voucher percent;

    @BeforeEach
    void setUp() {
        percent = new Voucher();
        percent.setCode("WELCOME10");
        percent.setDiscountType(Voucher.PERCENT);
        percent.setDiscountValue(new BigDecimal("10"));
        percent.setMinOrderAmount(new BigDecimal("50000"));
        percent.setMaxDiscountAmount(new BigDecimal("30000"));
        percent.setActive(true);
        percent.setUsedCount(0);
    }

    @Test
    void percentDiscountIsCapped() {
        assertEquals(0, new BigDecimal("30000").compareTo(service.discountOf(percent, new BigDecimal("1000000"))));
        assertEquals(0, new BigDecimal("8000").compareTo(service.discountOf(percent, new BigDecimal("80000"))));
    }

    @Test
    void fixedDiscountNeverExceedsSubtotal() {
        Voucher amount = new Voucher();
        amount.setDiscountType(Voucher.AMOUNT);
        amount.setDiscountValue(new BigDecimal("15000"));
        assertEquals(0, new BigDecimal("10000").compareTo(service.discountOf(amount, new BigDecimal("10000"))));
    }

    @Test
    void validateRejectsBelowMinimum() {
        when(repo.findByCodeIgnoreCase("WELCOME10")).thenReturn(Optional.of(percent));
        assertThrows(IllegalArgumentException.class, () -> service.validate("welcome10", new BigDecimal("20000")));
        assertSame(percent, service.validate("welcome10", new BigDecimal("60000")));
    }

    @Test
    void validateRejectsExpiredInactiveAndExhausted() {
        when(repo.findByCodeIgnoreCase("WELCOME10")).thenReturn(Optional.of(percent));

        percent.setExpiresAt(LocalDateTime.now().minusDays(1));
        assertThrows(IllegalArgumentException.class, () -> service.validate("WELCOME10", new BigDecimal("60000")));

        percent.setExpiresAt(null);
        percent.setActive(false);
        assertThrows(IllegalArgumentException.class, () -> service.validate("WELCOME10", new BigDecimal("60000")));

        percent.setActive(true);
        percent.setUsageLimit(5);
        percent.setUsedCount(5);
        assertThrows(IllegalArgumentException.class, () -> service.validate("WELCOME10", new BigDecimal("60000")));
    }

    @Test
    void unknownCodeIsRejected() {
        when(repo.findByCodeIgnoreCase("NOPE")).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> service.validate("NOPE", new BigDecimal("60000")));
    }
}
