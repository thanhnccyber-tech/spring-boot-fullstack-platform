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
    /** Số điểm hội viên muốn dùng để trừ tiền (server tự giới hạn theo điểm đang có và giá trị đơn) */
    private Integer usePoints;
    /** Toạ độ khách (nếu đã cho phép định vị) để tính phí ship theo khoảng cách */
    private Double lat;
    private Double lng;
    private List<CartItem> items;
}