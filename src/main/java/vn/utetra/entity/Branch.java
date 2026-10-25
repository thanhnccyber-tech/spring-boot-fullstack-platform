package vn.utetra.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "branches")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Branch {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String name;

    private String address;
    private String phone;

    @Column(nullable = false)
    private Boolean active = true;

    /** Toạ độ cửa hàng (để gợi ý cửa hàng gần nhất và tính phí ship theo khoảng cách). */
    private Double latitude;
    private Double longitude;

    public boolean hasCoords() { return latitude != null && longitude != null; }
}