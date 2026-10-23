package vn.utetra.dto;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class ResetPasswordRequest {
    private String email;
    private String newPassword;
    private String confirmPassword;
}