package vn.utetra.dto;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class ResetPasswordRequest {
    private String email;
    /** Token dùng 1 lần, cấp sau khi xác thực OTP thành công */
    private String token;
    private String newPassword;
    private String confirmPassword;
}