package vn.utetra.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "sugar_levels")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class SugarLevel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 30)
    private String name;

    @Column(nullable = false)
    private Integer percent = 50;

    @Column(nullable = false)
    private Boolean active = true;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 0;
}