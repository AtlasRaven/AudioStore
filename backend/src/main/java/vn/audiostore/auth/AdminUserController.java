package vn.audiostore.auth;

import jakarta.validation.Valid;
import vn.audiostore.common.ApiResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/users")
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {
    private final AuthService service;
    public AdminUserController(AuthService service) { this.service = service; }
    @GetMapping public ApiResponse<Page<UserView>> list(@RequestParam(defaultValue="0") int page, @RequestParam(defaultValue="20") int size) {
        return ApiResponse.ok("Tài khoản.", service.listUsers(PageRequest.of(Math.max(0, page), Math.min(100, Math.max(1, size)))));
    }
    @PatchMapping("/{id}") public ApiResponse<UserView> update(@PathVariable long id, @Valid @RequestBody AccessRequest request) {
        return ApiResponse.ok("Đã cập nhật tài khoản.", service.updateAccess(id, request.role(), request.enabled()));
    }
    public record AccessRequest(Role role, boolean enabled) {}
}
