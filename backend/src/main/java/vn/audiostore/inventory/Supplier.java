package vn.audiostore.inventory;

import jakarta.persistence.*;

@Entity
@Table(name = "suppliers")
public class Supplier {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false, length = 180) private String name;
    @Column(length = 30) private String phone;
    @Column(length = 190) private String email;
    @Column(length = 500) private String address;
    @Column(nullable = false) private boolean active = true;
    protected Supplier() {}
    public Supplier(String name, String phone, String email, String address) { this.name=name; this.phone=phone; this.email=email; this.address=address; }
    public Long getId() { return id; } public String getName() { return name; } public String getPhone() { return phone; } public String getEmail() { return email; } public String getAddress() { return address; } public boolean isActive() { return active; }
}
