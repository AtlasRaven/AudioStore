package vn.audiostore.inventory;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "purchase_items")
public class PurchaseItem {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "purchase_id") private Purchase purchase;
    @Column(name = "product_id", nullable = false) private Long productId;
    @Column(nullable = false) private int quantity;
    @Column(name = "unit_cost", nullable = false, precision = 15, scale = 2) private BigDecimal unitCost;
    protected PurchaseItem() {}
    public PurchaseItem(Purchase purchase, Long productId, int quantity, BigDecimal unitCost) { this.purchase=purchase; this.productId=productId; this.quantity=quantity; this.unitCost=unitCost; }
    public Long getId() { return id; } public Long getProductId() { return productId; } public int getQuantity() { return quantity; } public BigDecimal getUnitCost() { return unitCost; }
}
