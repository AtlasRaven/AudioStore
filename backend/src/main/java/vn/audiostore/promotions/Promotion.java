package vn.audiostore.promotions;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity @Table(name="promotions")
public class Promotion {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @Column(name="event_id") private Long eventId;
    @Column(nullable=false,length=160) private String name;
    @Column(name="target_type",nullable=false,length=20) private String targetType;
    @Column(name="target_id",nullable=false) private Long targetId;
    @Column(name="discount_percent",nullable=false,precision=5,scale=2) private BigDecimal discountPercent;
    @Column(name="starts_at",nullable=false) private Instant startsAt;
    @Column(name="ends_at",nullable=false) private Instant endsAt;
    @Column(name="flash_sale",nullable=false) private boolean flashSale;
    @Column(nullable=false) private boolean active=true;
    protected Promotion(){}
    public Promotion(Long eventId,String name,String targetType,Long targetId,BigDecimal percent,Instant startsAt,Instant endsAt,boolean flashSale){this.eventId=eventId;this.name=name;this.targetType=targetType;this.targetId=targetId;this.discountPercent=percent;this.startsAt=startsAt;this.endsAt=endsAt;this.flashSale=flashSale;}
    public Long getId(){return id;} public Long getEventId(){return eventId;} public String getName(){return name;} public String getTargetType(){return targetType;} public Long getTargetId(){return targetId;} public BigDecimal getDiscountPercent(){return discountPercent;} public Instant getStartsAt(){return startsAt;} public Instant getEndsAt(){return endsAt;} public boolean isFlashSale(){return flashSale;} public boolean isActive(){return active;}
    public void update(Long eventId,String name,String targetType,Long targetId,BigDecimal percent,Instant startsAt,Instant endsAt,boolean flashSale,boolean active){this.eventId=eventId;this.name=name;this.targetType=targetType;this.targetId=targetId;this.discountPercent=percent;this.startsAt=startsAt;this.endsAt=endsAt;this.flashSale=flashSale;this.active=active;}
}
