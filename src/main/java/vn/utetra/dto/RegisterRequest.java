package vn.utetra.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class RegisterRequest {

    @Email(message = "Email không hợp lệ")
    @Size(max = 100, message = "Email tối đa 100 ký tự")
    @NotBlank(message = "Email không được để trống")
    private String email;

    @NotBlank(message = "Mật khẩu không được để trống")
    @Pattern(regexp = "^(?=.*\\p{L})(?=.*\\d).{8,72}$",
             message = "Mật khẩu phải từ 8 đến 72 ký tự, gồm ít nhất 1 chữ cái và 1 chữ số")
    private String password;

    @NotBlank(message = "Họ tên không được để trống")
    @Size(max = 100, message = "Họ tên tối đa 100 ký tự")
    private String fullName;

    @Pattern(regexp = "^(0[0-9]{9,10})?$", message = "Số điện thoại không hợp lệ")
    private String phone;
}