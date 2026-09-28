package vn.audiostore.inventory;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "purchases")
public class Purchase {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "supplier_id") private Supplier supplier;
    @Column(name = "created_by", nullable = false) private Long createdBy;
    @Column(nullable = false, length = 20) private String status = "DRAFT";
    @Column(nullable = false, precision = 15, scale = 2) private BigDecimal total;
    @Column(name = "created_at", nullable = false) private Instant createdAt = Instant.now();
    @Column(name = "received_at") private Instant receivedAt;
    protected Purchase() {}
    public Purchase(Supplier supplier, Long createdBy, BigDecimal total) { this.supplier=supplier; this.createdBy=createdBy; this.total=total; }
    public Long getId() { return id; } public Supplier getSupplier() { return supplier; } public Long getCreatedBy() { return createdBy; } public String getStatus() { return status; }
    public BigDecimal getTotal() { return total; } public Instant getCreatedAt() { return createdAt; } public Instant getReceivedAt() { return receivedAt; }
    public void receive() { status="RECEIVED"; receivedAt=Instant.now(); }
}
