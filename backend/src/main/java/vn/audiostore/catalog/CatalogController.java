package vn.audiostore.catalog;

import java.math.BigDecimal;
import vn.audiostore.common.ApiResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class CatalogController {
    private final CatalogService service;
    public CatalogController(CatalogService service) { this.service=service; }
    @GetMapping("/products") public ApiResponse<Page<ProductPublicView>> products(@RequestParam(required=false) String q,
            @RequestParam(required=false) Long category, @RequestParam(required=false) Long brand,
            @RequestParam(required=false) BigDecimal minPrice, @RequestParam(required=false) BigDecimal maxPrice,
            @RequestParam(defaultValue="0") int page, @RequestParam(defaultValue="20") int size,
            @RequestParam(defaultValue="name") String sort, @RequestParam(defaultValue="asc") String direction) {
        String field = java.util.Set.of("name", "salePrice", "id").contains(sort) ? sort : "name";
        Sort order = "desc".equalsIgnoreCase(direction) ? Sort.by(field).descending() : Sort.by(field).ascending();
        return ApiResponse.ok("Danh sách sản phẩm.", service.browse(q, category, brand, minPrice, maxPrice, PageRequest.of(Math.max(0,page), Math.min(100,Math.max(1,size)), order)));
    }
    @GetMapping("/products/{id}") public ApiResponse<ProductPublicView> product(@PathVariable long id) { return ApiResponse.ok("Thông tin sản phẩm.", service.get(id)); }
    @GetMapping("/categories") public ApiResponse<java.util.List<Category>> categories() { return ApiResponse.ok("Danh mục sản phẩm.", service.categories()); }
    @GetMapping("/brands") public ApiResponse<java.util.List<Brand>> brands() { return ApiResponse.ok("Thương hiệu.", service.brands()); }
}
