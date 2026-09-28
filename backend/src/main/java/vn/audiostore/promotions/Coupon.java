package vn.audiostore.promotions;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity @Table(name="coupons")
public class Coupon {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @Column(nullable=false,unique=true,length=40) private String code;
    @Column(name="discount_type",nullable=false,length=12) private String discountType;
    @Column(name="discount_value",nullable=false,precision=15,scale=2) private BigDecimal discountValue;
    @Column(name="minimum_order",nullable=false,precision=15,scale=2) private BigDecimal minimumOrder;
    @Column(name="starts_at",nullable=false) private Instant startsAt;
    @Column(name="ends_at",nullable=false) private Instant endsAt;
    @Column(name="usage_limit") private Integer usageLimit;
    @Column(name="used_count",nullable=false) private int usedCount;
    @Column(nullable=false) private boolean active=true;
    protected Coupon(){}
    public Coupon(String code,String type,BigDecimal value,BigDecimal minimum,Instant startsAt,Instant endsAt,Integer limit){this.code=code.toUpperCase();this.discountType=type;this.discountValue=value;this.minimumOrder=minimum;this.startsAt=startsAt;this.endsAt=endsAt;this.usageLimit=limit;}
    public Long getId(){return id;} public String getCode(){return code;} public String getDiscountType(){return discountType;} public BigDecimal getDiscountValue(){return discountValue;} public BigDecimal getMinimumOrder(){return minimumOrder;} public Instant getStartsAt(){return startsAt;} public Instant getEndsAt(){return endsAt;} public Integer getUsageLimit(){return usageLimit;} public int getUsedCount(){return usedCount;} public boolean isActive(){return active;}
    public void update(String code,String type,BigDecimal value,BigDecimal minimum,Instant startsAt,Instant endsAt,Integer limit,boolean active){this.code=code.toUpperCase();this.discountType=type;this.discountValue=value;this.minimumOrder=minimum;this.startsAt=startsAt;this.endsAt=endsAt;this.usageLimit=limit;this.active=active;}
    public void use(){usedCount++;} public void release(){if(usedCount>0)usedCount--;}
}
