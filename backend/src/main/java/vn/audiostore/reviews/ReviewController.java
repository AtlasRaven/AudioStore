package vn.audiostore.reviews;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import vn.audiostore.common.ApiResponse;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class ReviewController {
    private final ReviewService service;
    public ReviewController(ReviewService service){this.service=service;}
    @GetMapping("/products/{productId}/reviews") public ApiResponse<?> visible(@PathVariable long productId){return ApiResponse.ok("Đánh giá sản phẩm.",service.visible(productId));}
    @PostMapping("/products/{productId}/reviews") @PreAuthorize("hasRole('CUSTOMER')")
    public ApiResponse<?> create(@PathVariable long productId,@Valid @RequestBody CreateReview request,Authentication auth){return ApiResponse.ok("Đánh giá đã gửi, đang chờ duyệt.",service.create(productId,request.orderId(),request.rating(),request.content(),auth.getName()));}
    @GetMapping("/admin/reviews") @PreAuthorize("hasAnyRole('ADMIN','MARKETING_CSKH')")
    public ApiResponse<Page<ReviewService.ReviewView>> all(@RequestParam(defaultValue="0") int page,@RequestParam(defaultValue="20") int size){return ApiResponse.ok("Đánh giá khách hàng.",service.all(page,size));}
    @PatchMapping("/admin/reviews/{id}") @PreAuthorize("hasAnyRole('ADMIN','MARKETING_CSKH')")
    public ApiResponse<?> moderate(@PathVariable long id,@Valid @RequestBody ReviewVisibility request){return ApiResponse.ok("Đã cập nhật trạng thái đánh giá.",service.moderate(id,request.visible()));}
    public record CreateReview(@NotNull Long orderId,@Min(1) @Max(5) int rating,@NotBlank @Size(max=2000) String content){}
    public record ReviewVisibility(boolean visible){}
}
