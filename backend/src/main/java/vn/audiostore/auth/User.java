package vn.audiostore.auth;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "users")
public class User {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false, unique = true, length = 190) private String email;
    @Column(name = "password_hash", nullable = false) private String passwordHash;
    @Column(name = "full_name", nullable = false, length = 120) private String fullName;
    @Column(length = 30) private String phone;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 30) private Role role;
    @Column(nullable = false) private boolean enabled = true;
    @Column(name = "created_at", nullable = false) private Instant createdAt = Instant.now();

    protected User() {}
    public User(String email, String passwordHash, String fullName, String phone, Role role) {
        this.email = email.toLowerCase().trim(); this.passwordHash = passwordHash; this.fullName = fullName.trim(); this.phone = phone; this.role = role;
    }
    public Long getId() { return id; }
    public String getEmail() { return email; }
    public String getPasswordHash() { return passwordHash; }
    public String getFullName() { return fullName; }
    public String getPhone() { return phone; }
    public Role getRole() { return role; }
    public boolean isEnabled() { return enabled; }
    public Instant getCreatedAt() { return createdAt; }
    public void changePassword(String hash) { passwordHash = hash; }
    public void updateAccess(Role nextRole, boolean active) { role = nextRole; enabled = active; }
}
