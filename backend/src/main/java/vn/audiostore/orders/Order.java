package vn.audiostore.orders;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name="orders")
public class Order {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @Column(nullable=false,unique=true,length=40) private String code;
    @Column(name="user_id",nullable=false) private Long userId;
    @Column(name="address_id",nullable=false) private Long addressId;
    @Column(name="coupon_id") private Long couponId;
    @Column(name="sales_owner_id") private Long salesOwnerId;
    @Column(name="recipient",nullable=false,length=120) private String recipient;
    @Column(name="phone",nullable=false,length=30) private String phone;
    @Column(name="shipping_address",nullable=false,length=800) private String shippingAddress;
    @Column(nullable=false,length=30) private String status;
    @Column(name="payment_method",nullable=false,length=20) private String paymentMethod;
    @Column(nullable=false,precision=15,scale=2) private BigDecimal subtotal;
    @Column(nullable=false,precision=15,scale=2) private BigDecimal discount;
    @Column(nullable=false,precision=15,scale=2) private BigDecimal total;
    @Column(name="created_at",nullable=false) private Instant createdAt=Instant.now();
    @Column(name="updated_at",nullable=false) private Instant updatedAt=Instant.now();
    protected Order() {}
    public Order(String code,Long userId,Address address,Long couponId,String status,String method,BigDecimal subtotal,BigDecimal discount,BigDecimal total) { this.code=code;this.userId=userId;this.addressId=address.getId();this.couponId=couponId;this.recipient=address.getRecipient();this.phone=address.getPhone();this.shippingAddress=address.fullAddress();this.status=status;this.paymentMethod=method;this.subtotal=subtotal;this.discount=discount;this.total=total; }
    public Long getId(){return id;} public String getCode(){return code;} public Long getUserId(){return userId;} public Long getAddressId(){return addressId;} public Long getCouponId(){return couponId;} public Long getSalesOwnerId(){return salesOwnerId;} public String getRecipient(){return recipient;} public String getPhone(){return phone;} public String getShippingAddress(){return shippingAddress;} public String getStatus(){return status;} public String getPaymentMethod(){return paymentMethod;} public BigDecimal getSubtotal(){return subtotal;} public BigDecimal getDiscount(){return discount;} public BigDecimal getTotal(){return total;} public Instant getCreatedAt(){return createdAt;} public Instant getUpdatedAt(){return updatedAt;}
    public void changeStatus(String status){this.status=status;this.updatedAt=Instant.now();}
    public void assignSalesOwner(Long userId){this.salesOwnerId=userId;}
}
