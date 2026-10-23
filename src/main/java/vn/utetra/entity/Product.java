package vn.utetra.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "products", indexes = {
        @Index(name = "idx_product_active", columnList = "active"),
        @Index(name = "idx_product_category", columnList = "category_id")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "base_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal basePrice;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "is_featured", nullable = false)
    private Boolean featured = false;

    @Column(name = "is_new", nullable = false)
    private Boolean isNew = false;

    @Column(name = "discount_percent", nullable = false)
    private Integer discountPercent = 0;

    @Column(nullable = false)
    private Boolean active = true;

    @Column(name = "out_of_stock", nullable = false)
    private Boolean outOfStock = false;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @Column(name = "allow_size", nullable = false)
    private Boolean allowSize = true;

    @Column(name = "allow_sugar", nullable = false)
    private Boolean allowSugar = true;

    @Column(name = "allow_ice", nullable = false)
    private Boolean allowIce = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "default_size_id")
    private SizeOption defaultSize;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "default_sugar_id")
    private SugarLevel defaultSugar;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "default_ice_id")
    private IceLevel defaultIce;

    @ManyToMany
    @JoinTable(name = "product_toppings",
            joinColumns = @JoinColumn(name = "product_id"),
            inverseJoinColumns = @JoinColumn(name = "topping_id"))
    private Set<Topping> toppings = new LinkedHashSet<>();

    @ManyToMany
    @JoinTable(name = "product_sizes",
            joinColumns = @JoinColumn(name = "product_id"),
            inverseJoinColumns = @JoinColumn(name = "size_id"))
    private Set<SizeOption> sizes = new LinkedHashSet<>();

    @ManyToMany
    @JoinTable(name = "product_sugars",
            joinColumns = @JoinColumn(name = "product_id"),
            inverseJoinColumns = @JoinColumn(name = "sugar_id"))
    private Set<SugarLevel> sugars = new LinkedHashSet<>();

    @ManyToMany
    @JoinTable(name = "product_ices",
            joinColumns = @JoinColumn(name = "product_id"),
            inverseJoinColumns = @JoinColumn(name = "ice_id"))
    private Set<IceLevel> ices = new LinkedHashSet<>();
}