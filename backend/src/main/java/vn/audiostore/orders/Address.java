package vn.audiostore.orders;

import jakarta.persistence.*;

@Entity
@Table(name = "addresses")
public class Address {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "user_id", nullable = false) private Long userId;
    @Column(nullable = false, length = 120) private String recipient;
    @Column(nullable = false, length = 30) private String phone;
    @Column(nullable = false, length = 100) private String province;
    @Column(nullable = false, length = 100) private String district;
    @Column(nullable = false, length = 500) private String detail;
    @Column(name = "default_address", nullable = false) private boolean defaultAddress;
    protected Address() {}
    public Address(Long userId,String recipient,String phone,String province,String district,String detail,boolean defaultAddress) { this.userId=userId;this.recipient=recipient;this.phone=phone;this.province=province;this.district=district;this.detail=detail;this.defaultAddress=defaultAddress; }
    public Long getId(){return id;} public Long getUserId(){return userId;} public String getRecipient(){return recipient;} public String getPhone(){return phone;} public String getProvince(){return province;} public String getDistrict(){return district;} public String getDetail(){return detail;} public boolean isDefaultAddress(){return defaultAddress;}
    public void setDefaultAddress(boolean value){defaultAddress=value;}
    public String fullAddress(){return detail+", "+district+", "+province;}
}
