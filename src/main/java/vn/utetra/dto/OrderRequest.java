package vn.utetra.dto;

import lombok.*;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class OrderRequest {
    private Long branchId;
    private String shippingAddress;
    private String paymentMethod;
    private String note;
    private List<CartItem> items;
}