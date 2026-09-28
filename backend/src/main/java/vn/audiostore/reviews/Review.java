package vn.audiostore.reviews;

import jakarta.persistence.*;
import java.time.Instant;

@Entity @Table(name="reviews",uniqueConstraints=@UniqueConstraint(name="uk_review_order_product",columnNames={"order_id","product_id"}))
public class Review {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @Column(name="product_id",nullable=false) private Long productId;
    @Column(name="user_id",nullable=false) private Long userId;
    @Column(name="order_id",nullable=false) private Long orderId;
    @Column(nullable=false) private int rating;
    @Column(nullable=false,length=2000) private String content;
    @Column(nullable=false) private boolean visible;
    @Column(name="created_at",nullable=false) private Instant createdAt=Instant.now();
    protected Review(){}
    public Review(Long productId,Long userId,Long orderId,int rating,String content){this.productId=productId;this.userId=userId;this.orderId=orderId;this.rating=rating;this.content=content;}
    public Long getId(){return id;} public Long getProductId(){return productId;} public Long getUserId(){return userId;} public Long getOrderId(){return orderId;} public int getRating(){return rating;} public String getContent(){return content;} public boolean isVisible(){return visible;} public Instant getCreatedAt(){return createdAt;}
    public void setVisible(boolean value){visible=value;}
}
