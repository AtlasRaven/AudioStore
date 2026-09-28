package vn.audiostore.orders;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import vn.audiostore.common.ApiResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class OrderController {
    private final OrderService service;
    public OrderController(OrderService service){this.service=service;}
    @GetMapping("/addresses") @PreAuthorize("hasRole('CUSTOMER')")
    public ApiResponse<?> addresses(Authentication auth){return ApiResponse.ok("Địa chỉ giao hàng.",service.addresses(auth.getName()));}
    @PostMapping("/addresses") @PreAuthorize("hasRole('CUSTOMER')")
    public ApiResponse<?> addAddress(Authentication auth,@Valid @RequestBody OrderService.AddressRequest request){return ApiResponse.ok("Đã lưu địa chỉ giao hàng.",service.addAddress(auth.getName(),request));}
    @PostMapping("/orders") @PreAuthorize("hasRole('CUSTOMER')")
    public ApiResponse<?> place(Authentication auth,@Valid @RequestBody CreateOrderRequest request){
        var data=service.place(auth.getName(),new OrderService.CreateOrder(request.addressId(),request.paymentMethod(),request.couponCode(),request.items().stream().map(x->new OrderService.OrderRequestLine(x.productId(),x.quantity())).toList()));
        return ApiResponse.ok("Đã tiếp nhận đơn hàng.",data);
    }
    @GetMapping("/orders/mine") @PreAuthorize("hasRole('CUSTOMER')")
    public ApiResponse<?> mine(Authentication auth){return ApiResponse.ok("Đơn hàng của bạn.",service.mine(auth.getName()));}
    @PostMapping("/orders/{id}/cancel") @PreAuthorize("hasRole('CUSTOMER')")
    public ApiResponse<?> cancel(@PathVariable long id,Authentication auth){return ApiResponse.ok("Đã hủy đơn hàng.",service.cancelMine(id,auth.getName()));}
    @GetMapping("/admin/orders") @PreAuthorize("hasAnyRole('ADMIN','STAFF_KHO','STAFF_BAN_HANG','KE_TOAN','MARKETING_CSKH')")
    public ApiResponse<?> list(@RequestParam(required=false) String status){return ApiResponse.ok("Danh sách đơn hàng.",service.list(status));}
    @PatchMapping("/admin/orders/{id}/status") @PreAuthorize("hasAnyRole('ADMIN','STAFF_KHO','STAFF_BAN_HANG')")
    public ApiResponse<?> transition(@PathVariable long id,@Valid @RequestBody StatusRequest request,Authentication auth){
        String role=auth.getAuthorities().iterator().next().getAuthority().substring(5);
        return ApiResponse.ok("Đã cập nhật đơn hàng.",service.transition(id,request.status(),auth.getName(),role));
    }
    @PostMapping("/admin/orders/{id}/confirm-payment") @PreAuthorize("hasAnyRole('ADMIN','KE_TOAN')")
    public ApiResponse<?> confirmPayment(@PathVariable long id,Authentication auth){return ApiResponse.ok("Đã xác nhận thanh toán.",service.confirmPayment(id,auth.getName()));}
    public record StatusRequest(@NotBlank String status){}
    public record CreateOrderRequest(@NotNull Long addressId,@NotBlank String paymentMethod,String couponCode,@NotNull @Size(min=1) java.util.List<@Valid ItemRequest> items){}
    public record ItemRequest(@NotNull Long productId,@jakarta.validation.constraints.Min(1) int quantity){}
}
