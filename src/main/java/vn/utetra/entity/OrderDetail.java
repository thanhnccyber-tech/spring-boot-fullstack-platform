package vn.utetra.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "order_details")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class OrderDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "order_id")
    private Order order;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    private Integer quantity;

    private String size;        // S, M, L
    private String sugarLevel;  // 0, 30, 50, 100
    private String iceLevel;

    @Column(name = "unit_price", precision = 12, scale = 2)
    private BigDecimal unitPrice;

    @Column(precision = 12, scale = 2)
    private BigDecimal subtotal;

    /**
     * Dùng Set thay vì List để tránh MultipleBagFetchException khi
     * fetch đồng thời Order.details (bag) + OrderDetail.toppings (bag).
     * LinkedHashSet để giữ thứ tự insert cho hiển thị ổn định.
     */
    @ManyToMany
    @JoinTable(name = "order_detail_toppings",
            joinColumns = @JoinColumn(name = "order_detail_id"),
            inverseJoinColumns = @JoinColumn(name = "topping_id"))
    private Set<Topping> toppings = new LinkedHashSet<>();
}