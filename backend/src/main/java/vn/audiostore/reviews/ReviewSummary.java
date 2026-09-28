package vn.audiostore.reviews;

import jakarta.persistence.*;
import java.time.Instant;

@Entity @Table(name="ai_review_summaries")
public class ReviewSummary {
    @Id @Column(name="product_id") private Long productId;
    @Column(nullable=false,columnDefinition="TEXT") private String summary;
    @Column(name="review_count",nullable=false) private long reviewCount;
    @Column(name="updated_at",nullable=false) private Instant updatedAt=Instant.now();
    protected ReviewSummary(){}
    public ReviewSummary(Long productId,String summary,long count){this.productId=productId;this.summary=summary;this.reviewCount=count;}
    public Long getProductId(){return productId;} public String getSummary(){return summary;} public long getReviewCount(){return reviewCount;} public Instant getUpdatedAt(){return updatedAt;}
}
