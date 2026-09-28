package vn.audiostore.reports;

import java.time.LocalDate;
import vn.audiostore.common.ApiResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/reports")
public class ReportController {
    private final ReportService service;
    public ReportController(ReportService service){this.service=service;}
    @GetMapping("/revenue") @PreAuthorize("hasAnyRole('ADMIN','KE_TOAN','STAFF_BAN_HANG')")
    public ApiResponse<?> revenue(@RequestParam LocalDate from,@RequestParam LocalDate to,@RequestParam(defaultValue="month") String groupBy,Authentication auth){
        return ApiResponse.ok("Báo cáo doanh thu.",service.revenue(from,to,groupBy,auth.getName(),role(auth)));
    }
    @GetMapping("/by-products") @PreAuthorize("hasAnyRole('ADMIN','KE_TOAN','STAFF_BAN_HANG')")
    public ApiResponse<?> byProducts(@RequestParam LocalDate from,@RequestParam LocalDate to,Authentication auth){return ApiResponse.ok("Doanh thu theo sản phẩm.",service.byProducts(from,to,auth.getName(),role(auth)));}
    @GetMapping("/by-categories") @PreAuthorize("hasAnyRole('ADMIN','KE_TOAN','STAFF_BAN_HANG')")
    public ApiResponse<?> byCategories(@RequestParam LocalDate from,@RequestParam LocalDate to,Authentication auth){return ApiResponse.ok("Doanh thu theo danh mục.",service.byCategories(from,to,auth.getName(),role(auth)));}
    @GetMapping("/top-products") @PreAuthorize("hasAnyRole('ADMIN','KE_TOAN','STAFF_BAN_HANG')")
    public ApiResponse<?> topProducts(@RequestParam LocalDate from,@RequestParam LocalDate to,@RequestParam(defaultValue="10") int limit,Authentication auth){
        return ApiResponse.ok("Sản phẩm bán chạy.",service.byProducts(from,to,auth.getName(),role(auth)).stream().limit(Math.max(1,Math.min(100,limit))).toList());
    }
    @GetMapping("/export") @PreAuthorize("hasAnyRole('ADMIN','KE_TOAN')")
    public ResponseEntity<byte[]> export(@RequestParam LocalDate from,@RequestParam LocalDate to,Authentication auth){
        byte[] data=service.export(from,to,auth.getName(),role(auth));
        return ResponseEntity.ok().contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .header(HttpHeaders.CONTENT_DISPOSITION,"attachment; filename=AudioStore-bao-cao-"+from+"-"+to+".xlsx").body(data);
    }
    private String role(Authentication auth){return auth.getAuthorities().iterator().next().getAuthority().substring(5);}
}
