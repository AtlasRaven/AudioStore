package vn.audiostore.orders;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name="payments")
public class Payment {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @Column(name="order_id",nullable=false,unique=true) private Long orderId;
    @Column(nullable=false,length=20) private String status;
    @Column(nullable=false,length=20) private String method;
    @Column(nullable=false,precision=15,scale=2) private BigDecimal amount;
    @Column(name="confirmed_at") private Instant confirmedAt;
    @Column(name="confirmed_by") private Long confirmedBy;
    protected Payment() {}
    public Payment(Long orderId,String status,String method,BigDecimal amount){this.orderId=orderId;this.status=status;this.method=method;this.amount=amount;}
    public Long getId(){return id;} public Long getOrderId(){return orderId;} public String getStatus(){return status;} public String getMethod(){return method;} public BigDecimal getAmount(){return amount;} public Instant getConfirmedAt(){return confirmedAt;} public Long getConfirmedBy(){return confirmedBy;}
    public void confirm(Long actor){status="CONFIRMED";confirmedAt=Instant.now();confirmedBy=actor;}
}
