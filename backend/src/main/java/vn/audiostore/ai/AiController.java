package vn.audiostore.ai;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import vn.audiostore.common.ApiResponse;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class AiController {
    private final AiFeatureService features;private final AiService ai;
    public AiController(AiFeatureService features,AiService ai){this.features=features;this.ai=ai;}
    @PostMapping("/ai/chat")
    public ApiResponse<?> chat(@Valid @RequestBody AiFeatureService.ChatRequest request,Authentication auth,jakarta.servlet.http.HttpServletRequest http){
        String email=loggedIn(auth)?auth.getName():null;String identity=email==null?"ip:"+http.getRemoteAddr():"user:"+email;return ApiResponse.ok("Tư vấn sản phẩm.",features.chat(request,identity,email));
    }
    @GetMapping("/ai/related/{productId}")
    public ApiResponse<?> related(@PathVariable long productId,Authentication auth,jakarta.servlet.http.HttpServletRequest http){
        String email=loggedIn(auth)?auth.getName():null;String identity=email==null?"ip:"+http.getRemoteAddr():"user:"+email;return ApiResponse.ok("Sản phẩm liên quan.",features.related(productId,identity,email));
    }
    @PostMapping("/admin/ai/descriptions") @PreAuthorize("hasAnyRole('ADMIN','MARKETING_CSKH')")
    public ApiResponse<?> description(@Valid @RequestBody DescriptionRequest request,Authentication auth){return ApiResponse.ok("Bản nháp mô tả sản phẩm.",features.description(new AiFeatureService.DescriptionRequest(request.name(),request.specifications()),"user:"+auth.getName(),auth.getName()));}
    @GetMapping("/ai/reviews/{productId}/summary")
    public ApiResponse<?> summary(@PathVariable long productId,Authentication auth,jakarta.servlet.http.HttpServletRequest http){
        String email=loggedIn(auth)?auth.getName():null;String identity=email==null?"ip:"+http.getRemoteAddr():"user:"+email;return ApiResponse.ok("Tóm tắt đánh giá.",features.reviewSummary(productId,identity,email));
    }
    @GetMapping("/admin/ai/logs") @PreAuthorize("hasAnyRole('ADMIN','MARKETING_CSKH')")
    public ApiResponse<Page<AiLog>> logs(@RequestParam(defaultValue="0") int page,@RequestParam(defaultValue="20") int size){return ApiResponse.ok("Lịch sử yêu cầu AI.",ai.logs(page,size));}
    private boolean loggedIn(Authentication authentication){return authentication!=null&&!"anonymousUser".equals(authentication.getName());}
    public record DescriptionRequest(@NotBlank @Size(max=180) String name,@NotBlank @Size(max=3000) String specifications){}
}
