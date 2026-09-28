package vn.audiostore.inventory;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "stock_movements")
public class StockMovement {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "product_id", nullable = false) private Long productId;
    @Column(name = "actor_id", nullable = false) private Long actorId;
    @Column(nullable = false, length = 24) private String type;
    @Column(name = "quantity_delta", nullable = false) private int quantityDelta;
    @Column(nullable = false, length = 500) private String reason;
    @Column(name = "reference_id") private Long referenceId;
    @Column(name = "created_at", nullable = false) private Instant createdAt = Instant.now();
    protected StockMovement() {}
    public StockMovement(Long productId, Long actorId, String type, int quantityDelta, String reason, Long referenceId) {
        this.productId=productId; this.actorId=actorId; this.type=type; this.quantityDelta=quantityDelta; this.reason=reason; this.referenceId=referenceId;
    }
    public Long getId() { return id; } public Long getProductId() { return productId; } public Long getActorId() { return actorId; }
    public String getType() { return type; } public int getQuantityDelta() { return quantityDelta; } public String getReason() { return reason; }
    public Long getReferenceId() { return referenceId; } public Instant getCreatedAt() { return createdAt; }
}
