package vn.utetra.dto;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class ForgotPasswordRequest {
    private String email;
}