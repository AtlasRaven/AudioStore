package vn.audiostore.orders;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name="order_items")
public class OrderItem {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @Column(name="order_id",nullable=false) private Long orderId;
    @Column(name="product_id",nullable=false) private Long productId;
    @Column(name="product_name",nullable=false,length=180) private String productName;
    @Column(nullable=false) private int quantity;
    @Column(name="unit_price",nullable=false,precision=15,scale=2) private BigDecimal unitPrice;
    @Column(name="cost_price",nullable=false,precision=15,scale=2) private BigDecimal costPrice;
    protected OrderItem() {}
    public OrderItem(Long orderId,Long productId,String productName,int quantity,BigDecimal unitPrice,BigDecimal costPrice){this.orderId=orderId;this.productId=productId;this.productName=productName;this.quantity=quantity;this.unitPrice=unitPrice;this.costPrice=costPrice;}
    public Long getId(){return id;} public Long getOrderId(){return orderId;} public Long getProductId(){return productId;} public String getProductName(){return productName;} public int getQuantity(){return quantity;} public BigDecimal getUnitPrice(){return unitPrice;} public BigDecimal getCostPrice(){return costPrice;}
}
