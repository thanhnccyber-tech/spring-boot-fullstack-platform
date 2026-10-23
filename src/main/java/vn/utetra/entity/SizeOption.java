package vn.utetra.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "size_options")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class SizeOption {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 20)
    private String name;

    @Column(name = "extra_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal extraPrice = BigDecimal.ZERO;

    @Column(nullable = false)
    private Boolean active = true;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 0;
}