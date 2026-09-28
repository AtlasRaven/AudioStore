package vn.audiostore.ai;

import vn.audiostore.catalog.*;
import vn.audiostore.reviews.*;
import java.math.BigDecimal;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class AiFeatureService {
    private final CatalogService catalog;private final ProductRepository products;private final AiService ai;private final AiRateLimiter limiter;
    private final ReviewService reviews;private final ReviewSummaryRepository summaries;
    public AiFeatureService(CatalogService catalog,ProductRepository products,AiService ai,AiRateLimiter limiter,ReviewService reviews,ReviewSummaryRepository summaries){this.catalog=catalog;this.products=products;this.ai=ai;this.limiter=limiter;this.reviews=reviews;this.summaries=summaries;}
    public ChatResult chat(ChatRequest request,String identity,String email){
        limiter.check(identity);List<Product> candidates=productCandidates(request);
        List<ProductPublicView> views=candidates.stream().map(p->catalog.get(p.getId())).toList();
        if(views.isEmpty())return new ChatResult("Chưa có mẫu phù hợp trong danh sách hàng hiện tại. Bạn có thể cho biết thêm mức ngân sách hoặc cách sử dụng.",views,false);
        String prompt="Khách đang cần tư vấn thiết bị âm thanh. Nhu cầu: "+clean(request.need())+". Ngân sách: "+(request.budget()==null?"chưa nêu":request.budget()+" đồng")+". Phòng nghe: "+clean(request.room())+". Nhạc thường nghe: "+clean(request.music())+
                "\nChỉ tư vấn dựa trên danh sách dữ liệu thật dưới đây. Nếu chưa đủ thông tin, hỏi một câu ngắn. Không tự thêm sản phẩm, mức giá hay thông số.\n"+productContext(views);
        AiService.Result result=ai.generate("CHAT",prompt,email);
        String reply=result.available()?result.text():"Tư vấn tự động đang tạm gián đoạn. Bạn có thể xem các mẫu phù hợp bên dưới hoặc liên hệ cửa hàng để được hỗ trợ.";
        return new ChatResult(reply,views,result.available());
    }
    public RelatedResult related(long productId,String identity,String email){
        limiter.check(identity);Product base=products.findOneById(productId).filter(Product::isActive).orElseThrow(()->new IllegalArgumentException("Không tìm thấy sản phẩm."));
        List<ProductPublicView> choices=products.findByActiveTrue().stream().filter(p->!p.getId().equals(productId))
                .sorted(Comparator.comparingInt((Product p)->scoreRelated(base,p)).reversed()).limit(6).map(p->catalog.get(p.getId())).toList();
        if(choices.isEmpty())return new RelatedResult("Hiện chưa có mẫu tương tự để gợi ý.",choices,false);
        String prompt="Giới thiệu ngắn vài lựa chọn thay thế cho sản phẩm "+base.getName()+". Chỉ nêu sản phẩm có trong danh sách, không thêm giá hoặc thông số ngoài dữ liệu.\n"+productContext(choices);
        AiService.Result result=ai.generate("RELATED",prompt,email);
        return new RelatedResult(result.available()?result.text():"Một số mẫu cùng nhóm bạn có thể xem:",choices,result.available());
    }
    public DescriptionResult description(DescriptionRequest request,String identity,String email){
        limiter.check(identity);
        String prompt="Viết bản nháp mô tả sản phẩm AudioStore bằng tiếng Việt tự nhiên, ngắn gọn và chính xác. Không tự bịa tính năng. Không dùng lời quảng cáo sáo rỗng.\nTên: "+clean(request.name())+"\nThông số đã cung cấp: "+clean(request.specifications());
        AiService.Result result=ai.generate("DESCRIPTION",prompt,email);
        return new DescriptionResult(result.text()==null?"":result.text(),result.available());
    }
    public SummaryResult reviewSummary(long productId,String identity,String email){
        limiter.check(identity);long count=reviews.visibleCount(productId);
        if(count==0)return new SummaryResult("Sản phẩm chưa có đánh giá được duyệt.",0,false);
        ReviewSummary cached=summaries.findById(productId).orElse(null);
        if(cached!=null&&cached.getReviewCount()==count)return new SummaryResult(cached.getSummary(),count,true);
        List<Review> entries=reviews.visibleEntities(productId);
        String context=entries.stream().limit(100).map(r->"Đánh giá "+r.getRating()+"/5: "+sanitizeReview(r.getContent())).filter(s->!s.endsWith(": ")).collect(Collectors.joining("\n"));
        AiService.Result result=ai.generate("REVIEW_SUMMARY","Tóm tắt các ý khách hàng nhắc đến nhiều nhất thành hai dòng: Ưu điểm và Điểm cần cân nhắc. Chỉ dựa trên đánh giá sau, không suy diễn và không nêu thông tin cá nhân.\n"+context,email);
        if(!result.available())return new SummaryResult("Chưa thể tạo tóm tắt lúc này. Vui lòng thử lại sau.",count,false);
        try{summaries.save(new ReviewSummary(productId,result.text(),count));}catch(RuntimeException ex){ReviewSummary concurrent=summaries.findById(productId).orElse(null);if(concurrent!=null&&concurrent.getReviewCount()==count)return new SummaryResult(concurrent.getSummary(),count,true);}
        return new SummaryResult(result.text(),count,true);
    }
    private List<Product> productCandidates(ChatRequest request){
        List<Product> available=products.findByActiveTrue();
        BigDecimal budget=request.budget();List<Product> within=budget==null?available:available.stream().filter(p->p.getSalePrice().compareTo(budget)<=0).toList();
        if(within.isEmpty())within=available;
        Set<String> terms=words(String.join(" ",clean(request.need()),clean(request.room()),clean(request.music())));
        List<Product> ranked=within.stream().sorted(Comparator.comparingInt((Product p)->score(p,terms)).reversed().thenComparing(Product::getSalePrice)).limit(8).toList();
        return ranked;
    }
    private int score(Product p,Set<String> terms){String source=(p.getName()+" "+p.getDescription()+" "+p.getSpecifications()+" "+p.getCategory().getName()+" "+p.getBrand().getName()).toLowerCase(Locale.ROOT);return (int)terms.stream().filter(source::contains).count();}
    private int scoreRelated(Product base,Product p){int score=0;if(base.getCategory().getId().equals(p.getCategory().getId()))score+=3;if(base.getBrand().getId().equals(p.getBrand().getId()))score+=2;return score;}
    private Set<String> words(String text){return Arrays.stream(text.toLowerCase(Locale.ROOT).split("[^\\p{L}\\p{N}]+" )).filter(w->w.length()>2).collect(Collectors.toSet());}
    private String productContext(List<ProductPublicView> list){return list.stream().map(p->"SKU "+p.sku()+" | "+p.name()+" | "+p.categoryName()+" | "+p.brandName()+" | "+p.salePrice()+" đồng | tồn khả dụng "+p.availableQuantity()+" | "+p.description()+" | "+p.specifications()).collect(Collectors.joining("\n"));}
    private String clean(String value){
        if(value==null)return "chưa cung cấp";
        return value.replaceAll("[\\r\\n]+"," ")
                .replaceAll("(?i)[\\w.+-]+@[\\w.-]+\\.[A-Z]{2,}","[ẩn email]")
                .replaceAll("(?i)(?:\\+?84|0)(?:[ .()\\-]*\\d){8,10}","[ẩn số điện thoại]")
                .replaceAll("(?i)(tôi tên|tên tôi|tôi là|địa chỉ|số nhà|số điện thoại|sđt|zalo|email)\\s*[:：]?[^.!?\\n]*","[đã lược thông tin cá nhân]")
                .replaceAll("https?://\\S+","[đã lược liên kết]").trim();
    }
    private String sanitizeReview(String content){
        String value=content.replaceAll("(?i)[\\w.+-]+@[\\w.-]+\\.[A-Z]{2,}","[ẩn email]");
        value=value.replaceAll("(?i)(?:\\+?84|0)(?:[ .()\\-]*\\d){8,10}","[ẩn số điện thoại]");
        value=value.replaceAll("(?i)(tôi tên|tên tôi|địa chỉ|số điện thoại|sđt|zalo|email)\\s*[:：]?[^.!?\\n]*","[đã lược thông tin cá nhân]");
        return value.length()>700?value.substring(0,700):value;
    }
    public record ChatRequest(@jakarta.validation.constraints.NotBlank @jakarta.validation.constraints.Size(max=600) String need,
            @jakarta.validation.constraints.DecimalMin("0") @jakarta.validation.constraints.DecimalMax("500000000") BigDecimal budget,
            @jakarta.validation.constraints.Size(max=160) String room,@jakarta.validation.constraints.Size(max=160) String music){}
    public record ChatResult(String reply,List<ProductPublicView> products,boolean aiAvailable){}
    public record RelatedResult(String advice,List<ProductPublicView> products,boolean aiAvailable){}
    public record DescriptionRequest(String name,String specifications){}
    public record DescriptionResult(String draft,boolean aiAvailable){}
    public record SummaryResult(String summary,long reviewCount,boolean aiAvailable){}
}
