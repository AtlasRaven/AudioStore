package vn.audiostore.inventory;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import vn.audiostore.common.ApiResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
public class InventoryController {
    private final InventoryService service;
    public InventoryController(InventoryService service) { this.service=service; }
    @GetMapping("/inventory") @PreAuthorize("hasAnyRole('ADMIN','STAFF_KHO','STAFF_BAN_HANG','KE_TOAN')")
    public ApiResponse<?> inventory(@RequestParam(required=false) String q,@RequestParam(defaultValue="false") boolean lowStock) { return ApiResponse.ok("Tồn kho sản phẩm.",service.listInventory(q,lowStock)); }
    @PostMapping("/inventory/{productId}/adjust") @PreAuthorize("hasAnyRole('ADMIN','STAFF_KHO')")
    public ApiResponse<?> adjust(@PathVariable long productId,@Valid @RequestBody AdjustRequest req,Authentication auth) { return ApiResponse.ok("Đã điều chỉnh tồn kho.",service.adjust(productId,req.delta(),req.minimumQuantity(),req.reason(),auth.getName())); }
    @PostMapping("/stock-issues") @PreAuthorize("hasAnyRole('ADMIN','STAFF_KHO')")
    public ApiResponse<?> issue(@Valid @RequestBody IssueRequest req,Authentication auth) { return ApiResponse.ok("Đã ghi nhận phiếu xuất.",service.issue(req.productId(),req.quantity(),req.reason(),auth.getName())); }
    @GetMapping("/stock-movements") @PreAuthorize("hasAnyRole('ADMIN','STAFF_KHO')")
    public ApiResponse<?> movements() { return ApiResponse.ok("Lịch sử biến động kho.",service.movements()); }
    @GetMapping("/suppliers") @PreAuthorize("hasAnyRole('ADMIN','STAFF_KHO','KE_TOAN')")
    public ApiResponse<?> suppliers() { return ApiResponse.ok("Nhà cung cấp.",service.suppliers()); }
    @PostMapping("/suppliers") @PreAuthorize("hasAnyRole('ADMIN','STAFF_KHO')")
    public ApiResponse<?> createSupplier(@Valid @RequestBody SupplierRequest req) { return ApiResponse.ok("Đã thêm nhà cung cấp.",service.createSupplier(req.name(),req.phone(),req.email(),req.address())); }
    @GetMapping("/purchases") @PreAuthorize("hasAnyRole('ADMIN','STAFF_KHO','KE_TOAN')")
    public ApiResponse<?> purchases() { return ApiResponse.ok("Phiếu nhập hàng.",service.purchases()); }
    @PostMapping("/purchases") @PreAuthorize("hasAnyRole('ADMIN','STAFF_KHO')")
    public ApiResponse<?> createPurchase(@Valid @RequestBody PurchaseRequest req,Authentication auth) {
        java.util.List<InventoryService.PurchaseLine> lines = req.items().stream()
                .map(item -> new InventoryService.PurchaseLine(item.productId(), item.quantity(), item.unitCost()))
                .toList();
        return ApiResponse.ok("Đã tạo phiếu nhập nháp.",service.createPurchase(req.supplierId(),lines,auth.getName()));
    }
    @PostMapping("/purchases/{id}/receive") @PreAuthorize("hasAnyRole('ADMIN','STAFF_KHO')")
    public ApiResponse<?> receive(@PathVariable long id,Authentication auth) { return ApiResponse.ok("Đã xác nhận nhập hàng.",service.receive(id,auth.getName())); }
    public record AdjustRequest(int delta,@Min(0) int minimumQuantity,@NotBlank @Size(max=500) String reason) {}
    public record IssueRequest(@NotNull Long productId,@Min(1) int quantity,@NotBlank @Size(max=500) String reason) {}
    public record SupplierRequest(@NotBlank @Size(max=180) String name,@Size(max=30) String phone,@Email @Size(max=190) String email,@Size(max=500) String address) {}
    public record PurchaseRequest(@NotNull Long supplierId,@NotNull @Size(min=1) java.util.List<@Valid PurchaseLineRequest> items) {}
    public record PurchaseLineRequest(@NotNull Long productId,@Min(1) int quantity,@NotNull @jakarta.validation.constraints.DecimalMin("0.00") java.math.BigDecimal unitCost) {}
}
