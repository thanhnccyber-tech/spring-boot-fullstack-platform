package vn.utetra.dto;

import lombok.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class CartItem implements Serializable {
    private Long productId;
    private String productName;
    private String imageUrl;
    private BigDecimal basePrice;
    private Integer quantity;
    private String size;
    private String sugarLevel;
    private String iceLevel;
    private List<Long> toppingIds;
    private List<String> toppingNames;
    private BigDecimal toppingPrice;
    private BigDecimal unitPrice;

    public BigDecimal getSubtotal() {
        if (unitPrice == null) return BigDecimal.ZERO;
        return unitPrice.multiply(BigDecimal.valueOf(quantity == null ? 0 : quantity));
    }
}