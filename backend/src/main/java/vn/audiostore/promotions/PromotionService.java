package vn.audiostore.promotions;

import vn.audiostore.catalog.Product;
import vn.audiostore.catalog.ProductRepository;
import vn.audiostore.catalog.CategoryRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PromotionService {
    private final PromotionRepository promotions; private final CouponRepository coupons; private final ProductRepository products; private final EventRepository events; private final CategoryRepository categories;
    public PromotionService(PromotionRepository promotions,CouponRepository coupons,ProductRepository products,EventRepository events,CategoryRepository categories){this.promotions=promotions;this.coupons=coupons;this.products=products;this.events=events;this.categories=categories;}
    @Transactional(readOnly=true)
    public List<Promotion> activePromotions(){return validPromotions();}
    public List<Promotion> allPromotions(){return promotions.findAll(org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC,"startsAt"));}
    public List<Coupon> allCoupons(){return coupons.findAll(org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC,"startsAt"));}
    public List<Event> allEvents(){return events.findAll(org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC,"startsAt"));}
    @Transactional
    public Quote quote(Map<Long,Integer> quantities,String couponCode,boolean reserveCoupon) {
        Map<Long,Product> found=products.findAllById(quantities.keySet()).stream().collect(Collectors.toMap(Product::getId,p->p));
        if(found.size()!=quantities.size())throw new IllegalArgumentException("Một sản phẩm trong giỏ không còn tồn tại.");
        Instant now=Instant.now(); List<Promotion> active=validPromotions();
        Map<Long,BigDecimal> prices=new LinkedHashMap<>(); BigDecimal subtotal=BigDecimal.ZERO;
        for(var e:quantities.entrySet()){
            Product p=found.get(e.getKey()); BigDecimal percent=active.stream().filter(x->matches(x,p)).map(Promotion::getDiscountPercent).max(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
            BigDecimal price=p.getSalePrice().multiply(BigDecimal.ONE.subtract(percent.movePointLeft(2))).setScale(2,RoundingMode.HALF_UP);
            prices.put(p.getId(),price); subtotal=subtotal.add(price.multiply(BigDecimal.valueOf(e.getValue())));
        }
        BigDecimal couponDiscount=BigDecimal.ZERO; Long couponId=null; Coupon coupon=null;
        if(couponCode!=null&&!couponCode.isBlank()){
            coupon=reserveCoupon?coupons.findLockedByCode(couponCode.trim()).orElseThrow(()->new IllegalArgumentException("Mã giảm giá không tồn tại.")):coupons.findByCodeIgnoreCase(couponCode.trim()).orElseThrow(()->new IllegalArgumentException("Mã giảm giá không tồn tại."));
            validateCoupon(coupon,subtotal,now);
            couponId=coupon.getId();
            couponDiscount="PERCENT".equals(coupon.getDiscountType())?subtotal.multiply(coupon.getDiscountValue()).movePointLeft(2).setScale(2,RoundingMode.HALF_UP):coupon.getDiscountValue().min(subtotal);
            if(reserveCoupon)coupon.use();
        }
        return new Quote(prices,subtotal,couponDiscount,subtotal.subtract(couponDiscount),couponId);
    }
    @Transactional public void releaseCoupon(Long id){if(id!=null){Coupon c=coupons.findById(id).orElseThrow(()->new IllegalArgumentException("Không tìm thấy mã giảm giá."));coupons.findLockedByCode(c.getCode()).ifPresent(Coupon::release);}}
    @Transactional(readOnly=true)
    public CouponQuote validate(String code,Map<Long,Integer> quantities){Quote q=quote(quantities,code,false);return new CouponQuote(q.subtotal(),q.couponDiscount(),q.total());}
    private boolean matches(Promotion p,Product product){return ("PRODUCT".equals(p.getTargetType())&&p.getTargetId().equals(product.getId()))||("CATEGORY".equals(p.getTargetType())&&p.getTargetId().equals(product.getCategory().getId()));}
    private List<Promotion> validPromotions(){
        Instant now=Instant.now();
        return promotions.findByActiveTrueAndStartsAtLessThanEqualAndEndsAtGreaterThanEqualOrderByStartsAtAsc(now,now).stream()
                .filter(p->p.getEventId()==null||events.findById(p.getEventId()).map(Event::isActive).orElse(false)).toList();
    }
    private void validateCoupon(Coupon c,BigDecimal subtotal,Instant now){
        if(!c.isActive()||now.isBefore(c.getStartsAt())||now.isAfter(c.getEndsAt()))throw new IllegalArgumentException("Mã giảm giá chưa có hiệu lực hoặc đã hết hạn.");
        if(c.getUsageLimit()!=null&&c.getUsedCount()>=c.getUsageLimit())throw new IllegalArgumentException("Mã giảm giá đã hết lượt sử dụng.");
        if(subtotal.compareTo(c.getMinimumOrder())<0)throw new IllegalArgumentException("Đơn hàng chưa đạt giá trị tối thiểu để dùng mã.");
    }
    @Transactional
    public Promotion savePromotion(Long id,PromotionInput in){
        validatePromotion(in); if(in.eventId()!=null&&!events.existsById(in.eventId()))throw new IllegalArgumentException("Sự kiện không tồn tại.");
        if("PRODUCT".equals(in.targetType())&&!products.existsById(in.targetId()))throw new IllegalArgumentException("Sản phẩm không tồn tại.");
        if("CATEGORY".equals(in.targetType())&&!categories.existsById(in.targetId()))throw new IllegalArgumentException("Danh mục không tồn tại.");
        Promotion p=id==null?new Promotion(in.eventId(),in.name().trim(),in.targetType(),in.targetId(),in.discountPercent(),in.startsAt(),in.endsAt(),in.flashSale()):promotions.findById(id).orElseThrow(()->new IllegalArgumentException("Không tìm thấy chương trình."));
        p.update(in.eventId(),in.name().trim(),in.targetType(),in.targetId(),in.discountPercent(),in.startsAt(),in.endsAt(),in.flashSale(),in.active());return promotions.save(p);
    }
    @Transactional public void deactivatePromotion(long id){Promotion p=promotions.findById(id).orElseThrow(()->new IllegalArgumentException("Không tìm thấy chương trình."));p.update(p.getEventId(),p.getName(),p.getTargetType(),p.getTargetId(),p.getDiscountPercent(),p.getStartsAt(),p.getEndsAt(),p.isFlashSale(),false);}
    @Transactional
    public Coupon saveCoupon(Long id,CouponInput in){validateCouponInput(in);Coupon c=id==null?new Coupon(in.code().trim(),in.discountType(),in.discountValue(),in.minimumOrder(),in.startsAt(),in.endsAt(),in.usageLimit()):coupons.findById(id).orElseThrow(()->new IllegalArgumentException("Không tìm thấy mã giảm giá."));c.update(in.code().trim(),in.discountType(),in.discountValue(),in.minimumOrder(),in.startsAt(),in.endsAt(),in.usageLimit(),in.active());return coupons.save(c);}
    @Transactional public void deactivateCoupon(long id){Coupon c=coupons.findById(id).orElseThrow(()->new IllegalArgumentException("Không tìm thấy mã giảm giá."));c.update(c.getCode(),c.getDiscountType(),c.getDiscountValue(),c.getMinimumOrder(),c.getStartsAt(),c.getEndsAt(),c.getUsageLimit(),false);}
    @Transactional public Event saveEvent(Long id,EventInput in){if(!in.endsAt().isAfter(in.startsAt()))throw new IllegalArgumentException("Thời gian kết thúc phải sau thời gian bắt đầu.");Event e=id==null?new Event(in.name().trim(),in.startsAt(),in.endsAt()):events.findById(id).orElseThrow(()->new IllegalArgumentException("Không tìm thấy sự kiện."));e.update(in.name().trim(),in.startsAt(),in.endsAt(),in.active());return events.save(e);}
    private void validatePromotion(PromotionInput in){if(in.name()==null||in.name().isBlank()||in.targetId()==null||!Set.of("PRODUCT","CATEGORY").contains(in.targetType())||in.discountPercent()==null||in.discountPercent().signum()<=0||in.discountPercent().compareTo(BigDecimal.valueOf(90))>0||!in.endsAt().isAfter(in.startsAt()))throw new IllegalArgumentException("Thông tin chương trình chưa hợp lệ.");}
    private void validateCouponInput(CouponInput in){if(in.code()==null||in.code().isBlank()||!Set.of("PERCENT","FIXED").contains(in.discountType())||in.discountValue()==null||in.discountValue().signum()<=0||in.minimumOrder()==null||in.minimumOrder().signum()<0||!in.endsAt().isAfter(in.startsAt())||(in.usageLimit()!=null&&in.usageLimit()<1)||("PERCENT".equals(in.discountType())&&in.discountValue().compareTo(BigDecimal.valueOf(100))>0))throw new IllegalArgumentException("Thông tin mã giảm giá chưa hợp lệ.");}
    public record Quote(Map<Long,BigDecimal> prices,BigDecimal subtotal,BigDecimal couponDiscount,BigDecimal total,Long couponId){}
    public record CouponQuote(BigDecimal subtotal,BigDecimal discount,BigDecimal total){}
    public record PromotionInput(Long eventId,String name,String targetType,Long targetId,BigDecimal discountPercent,Instant startsAt,Instant endsAt,boolean flashSale,boolean active){}
    public record CouponInput(String code,String discountType,BigDecimal discountValue,BigDecimal minimumOrder,Instant startsAt,Instant endsAt,Integer usageLimit,boolean active){}
    public record EventInput(String name,Instant startsAt,Instant endsAt,boolean active){}
}
