package vn.utetra.dto;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class OtpVerifyRequest {
    private String email;
    private String code;
}