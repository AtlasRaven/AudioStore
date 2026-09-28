package vn.audiostore.promotions;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.stream.Collectors;
import vn.audiostore.common.ApiResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class PromotionController {
    private final PromotionService service;
    public PromotionController(PromotionService service){this.service=service;}
    @GetMapping("/promotions/active") public ApiResponse<?> active(){return ApiResponse.ok("Khuyến mãi đang áp dụng.",service.activePromotions());}
    @PostMapping("/coupons/validate") @PreAuthorize("hasRole('CUSTOMER')")
    public ApiResponse<?> validate(@Valid @RequestBody CouponValidation request){
        Map<Long,Integer> quantities=request.items().stream().collect(Collectors.toMap(Item::productId,Item::quantity,Integer::sum));
        return ApiResponse.ok("Mã giảm giá áp dụng được.",service.validate(request.code(),quantities));
    }
    @GetMapping("/admin/promotions") @PreAuthorize("hasAnyRole('ADMIN','MARKETING_CSKH','STAFF_BAN_HANG','KE_TOAN')")
    public ApiResponse<?> promotions(){return ApiResponse.ok("Chương trình khuyến mãi.",service.allPromotions());}
    @PostMapping("/admin/promotions") @PreAuthorize("hasAnyRole('ADMIN','MARKETING_CSKH')")
    public ApiResponse<?> createPromotion(@Valid @RequestBody PromotionRequest request){return ApiResponse.ok("Đã tạo chương trình.",service.savePromotion(null,request.input()));}
    @PutMapping("/admin/promotions/{id}") @PreAuthorize("hasAnyRole('ADMIN','MARKETING_CSKH')")
    public ApiResponse<?> updatePromotion(@PathVariable long id,@Valid @RequestBody PromotionRequest request){return ApiResponse.ok("Đã cập nhật chương trình.",service.savePromotion(id,request.input()));}
    @DeleteMapping("/admin/promotions/{id}") @PreAuthorize("hasAnyRole('ADMIN','MARKETING_CSKH')")
    public ApiResponse<Void> deactivatePromotion(@PathVariable long id){service.deactivatePromotion(id);return ApiResponse.ok("Đã dừng chương trình.",null);}
    @GetMapping("/admin/coupons") @PreAuthorize("hasAnyRole('ADMIN','MARKETING_CSKH','STAFF_BAN_HANG','KE_TOAN')")
    public ApiResponse<?> coupons(){return ApiResponse.ok("Mã giảm giá.",service.allCoupons());}
    @PostMapping("/admin/coupons") @PreAuthorize("hasAnyRole('ADMIN','MARKETING_CSKH')")
    public ApiResponse<?> createCoupon(@Valid @RequestBody CouponRequest request){return ApiResponse.ok("Đã tạo mã giảm giá.",service.saveCoupon(null,request.input()));}
    @PutMapping("/admin/coupons/{id}") @PreAuthorize("hasAnyRole('ADMIN','MARKETING_CSKH')")
    public ApiResponse<?> updateCoupon(@PathVariable long id,@Valid @RequestBody CouponRequest request){return ApiResponse.ok("Đã cập nhật mã giảm giá.",service.saveCoupon(id,request.input()));}
    @DeleteMapping("/admin/coupons/{id}") @PreAuthorize("hasAnyRole('ADMIN','MARKETING_CSKH')")
    public ApiResponse<Void> deactivateCoupon(@PathVariable long id){service.deactivateCoupon(id);return ApiResponse.ok("Đã dừng mã giảm giá.",null);}
    @GetMapping("/admin/events") @PreAuthorize("hasAnyRole('ADMIN','MARKETING_CSKH','STAFF_BAN_HANG','KE_TOAN')")
    public ApiResponse<?> events(){return ApiResponse.ok("Sự kiện.",service.allEvents());}
    @PostMapping("/admin/events") @PreAuthorize("hasAnyRole('ADMIN','MARKETING_CSKH')")
    public ApiResponse<?> createEvent(@Valid @RequestBody EventRequest request){return ApiResponse.ok("Đã tạo sự kiện.",service.saveEvent(null,request.input()));}
    @PutMapping("/admin/events/{id}") @PreAuthorize("hasAnyRole('ADMIN','MARKETING_CSKH')")
    public ApiResponse<?> updateEvent(@PathVariable long id,@Valid @RequestBody EventRequest request){return ApiResponse.ok("Đã cập nhật sự kiện.",service.saveEvent(id,request.input()));}
    public record Item(@NotNull Long productId,@Min(1) @Max(99) int quantity){}
    public record CouponValidation(@NotBlank String code,@NotEmpty java.util.List<@Valid Item> items){}
    public record PromotionRequest(Long eventId,@NotBlank @Size(max=160) String name,@NotBlank String targetType,@NotNull Long targetId,@NotNull @DecimalMin("0.01") @DecimalMax("90.00") BigDecimal discountPercent,@NotNull Instant startsAt,@NotNull Instant endsAt,boolean flashSale,boolean active){PromotionService.PromotionInput input(){return new PromotionService.PromotionInput(eventId,name,targetType,targetId,discountPercent,startsAt,endsAt,flashSale,active);}}
    public record CouponRequest(@NotBlank @Size(max=40) String code,@NotBlank String discountType,@NotNull @DecimalMin("0.01") BigDecimal discountValue,@NotNull @DecimalMin("0.00") BigDecimal minimumOrder,@NotNull Instant startsAt,@NotNull Instant endsAt,@Min(1) Integer usageLimit,boolean active){PromotionService.CouponInput input(){return new PromotionService.CouponInput(code,discountType,discountValue,minimumOrder,startsAt,endsAt,usageLimit,active);}}
    public record EventRequest(@NotBlank @Size(max=160) String name,@NotNull Instant startsAt,@NotNull Instant endsAt,boolean active){PromotionService.EventInput input(){return new PromotionService.EventInput(name,startsAt,endsAt,active);}}
}
