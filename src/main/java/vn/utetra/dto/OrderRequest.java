package vn.utetra.dto;

import lombok.*;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class OrderRequest {
    private Long branchId;
    private String shippingAddress;
    private String paymentMethod;
    private String note;

    /** DELIVERY | PICKUP */
    private String deliveryMethod;
    private String phone;
    /** "ASAP" hoặc giá trị datetime-local (yyyy-MM-ddTHH:mm) */
    private String deliveryTime;
    private String voucherCode;
    private boolean vat;
    private String vatCompany;
    private String vatTaxCode;
    private String vatEmail;
    private boolean agreeTerms;
    private List<CartItem> items;
}