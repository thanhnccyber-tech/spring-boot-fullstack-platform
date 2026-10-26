package vn.utetra.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Entity
@Table(name = "delivery_failures", indexes = {
        @Index(name = "idx_delivery_failure_shipper_date", columnList = "shipper_id, failed_at"),
        @Index(name = "idx_delivery_failure_order_date", columnList = "order_id, failed_at")
})
@Getter
@Setter
@NoArgsConstructor
public class DeliveryFailure {

    private static final DateTimeFormatter DISPLAY_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "shipper_id", nullable = false)
    private User shipper;

    @Column(nullable = false, length = 500)
    private String reason;

    @Column(name = "failed_at", nullable = false)
    private LocalDateTime failedAt = LocalDateTime.now();

    public String getFailedAtFormatted() {
        return failedAt == null ? "" : failedAt.format(DISPLAY_FORMAT);
    }
}
