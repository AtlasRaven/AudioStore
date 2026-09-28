package vn.audiostore.catalog;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import vn.audiostore.common.ApiResponse;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
public class AdminCatalogController {
    private final CatalogService service;
    public AdminCatalogController(CatalogService service) { this.service=service; }
    @GetMapping("/products") @PreAuthorize("hasAnyRole('ADMIN','STAFF_KHO','STAFF_BAN_HANG','KE_TOAN','MARKETING_CSKH')")
    public ApiResponse<?> products(@RequestParam(required=false) String q, @RequestParam(defaultValue="0") int page, @RequestParam(defaultValue="20") int size,org.springframework.security.core.Authentication auth) {
        var pageable=PageRequest.of(Math.max(0,page),Math.min(100,Math.max(1,size)),org.springframework.data.domain.Sort.by("name"));
        return ApiResponse.ok("Danh sách sản phẩm.","ADMIN".equals(auth.getAuthorities().iterator().next().getAuthority().substring(5))?service.adminBrowseWithCosts(q,pageable):service.adminBrowse(q,pageable));
    }
    @PostMapping("/products") @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<ProductView> create(@Valid @RequestBody CatalogService.ProductInput request) { return ApiResponse.ok("Đã thêm sản phẩm.", service.create(request)); }
    @PutMapping("/products/{id}") @PreAuthorize("hasAnyRole('ADMIN','STAFF_BAN_HANG','MARKETING_CSKH')")
    public ApiResponse<?> update(@PathVariable long id, @RequestBody CatalogService.ProductInput request, Authentication auth) {
        ProductView updated=service.update(id, request, auth.getAuthorities().iterator().next().getAuthority().substring(5));
        return ApiResponse.ok("Đã cập nhật sản phẩm.","ADMIN".equals(auth.getAuthorities().iterator().next().getAuthority().substring(5))?updated:ProductStaffView.from(updated));
    }
    @DeleteMapping("/products/{id}") @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> deactivate(@PathVariable long id) { service.deactivate(id); return ApiResponse.ok("Đã ẩn sản phẩm.", null); }
    @PostMapping("/categories") @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Category> createCategory(@Valid @RequestBody CatalogName request) { return ApiResponse.ok("Đã thêm danh mục.", service.saveCategory(null,request.name(),true)); }
    @PutMapping("/categories/{id}") @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Category> updateCategory(@PathVariable long id,@Valid @RequestBody CatalogName request) { return ApiResponse.ok("Đã cập nhật danh mục.",service.saveCategory(id,request.name(),request.active())); }
    @DeleteMapping("/categories/{id}") @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> deleteCategory(@PathVariable long id) { service.deactivateCategory(id); return ApiResponse.ok("Đã ẩn danh mục.",null); }
    @PostMapping("/brands") @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Brand> createBrand(@Valid @RequestBody CatalogName request) { return ApiResponse.ok("Đã thêm thương hiệu.",service.saveBrand(null,request.name(),true)); }
    @PutMapping("/brands/{id}") @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Brand> updateBrand(@PathVariable long id,@Valid @RequestBody CatalogName request) { return ApiResponse.ok("Đã cập nhật thương hiệu.",service.saveBrand(id,request.name(),request.active())); }
    @DeleteMapping("/brands/{id}") @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> deleteBrand(@PathVariable long id) { service.deactivateBrand(id); return ApiResponse.ok("Đã ẩn thương hiệu.",null); }
    public record CatalogName(@NotBlank @Size(max=100) String name, boolean active) {}
}
