package vn.audiostore.orders;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name="shipments")
public class Shipment {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @Column(name="order_id",nullable=false,unique=true) private Long orderId;
    @Column(nullable=false,length=20) private String status;
    @Column(name="shipped_at",nullable=false) private Instant shippedAt;
    @Column(length=500) private String note;
    protected Shipment() {}
    public Shipment(Long orderId,String status,String note){this.orderId=orderId;this.status=status;this.note=note;this.shippedAt=Instant.now();}
}
