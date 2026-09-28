package vn.audiostore.auth;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import vn.audiostore.common.ApiResponse;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService service;
    public AuthController(AuthService service) { this.service = service; }
    @PostMapping("/register") public ApiResponse<AuthService.LoginResult> register(@Valid @RequestBody RegisterRequest request) {
        return ApiResponse.ok("Tạo tài khoản thành công.", service.register(request.email(), request.password(), request.fullName(), request.phone()));
    }
    @PostMapping("/login") public ApiResponse<AuthService.LoginResult> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.ok("Đăng nhập thành công.", service.login(request.email(), request.password()));
    }
    @GetMapping("/me") public ApiResponse<UserView> me(Authentication auth) { return ApiResponse.ok("Thông tin tài khoản.", UserView.from(service.getByEmail(auth.getName()))); }
    @PostMapping("/change-password") public ApiResponse<Void> changePassword(Authentication auth, @Valid @RequestBody PasswordRequest request) {
        service.changePassword(auth.getName(), request.oldPassword(), request.newPassword()); return ApiResponse.ok("Đã đổi mật khẩu.", null);
    }
    public record RegisterRequest(@Email @NotBlank String email, @NotBlank @Size(min=8, max=72) String password, @NotBlank @Size(max=120) String fullName, @Size(max=30) String phone) {}
    public record LoginRequest(@Email @NotBlank String email, @NotBlank String password) {}
    public record PasswordRequest(@NotBlank String oldPassword, @NotBlank @Size(min=8, max=72) String newPassword) {}
}
