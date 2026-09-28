package vn.audiostore.config;

import vn.audiostore.auth.Role;
import vn.audiostore.auth.User;
import vn.audiostore.auth.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class SeedAccounts {
    @Bean CommandLineRunner seedInitialAccounts(UserRepository users, PasswordEncoder encoder) {
        return args -> {
            add(users, encoder, "admin@audiostore.local", "Quản trị AudioStore", Role.ADMIN);
            add(users, encoder, "kho@audiostore.local", "Nhân viên kho", Role.STAFF_KHO);
            add(users, encoder, "banhang@audiostore.local", "Nhân viên bán hàng", Role.STAFF_BAN_HANG);
            add(users, encoder, "ketoan@audiostore.local", "Kế toán", Role.KE_TOAN);
            add(users, encoder, "marketing@audiostore.local", "Marketing và chăm sóc khách", Role.MARKETING_CSKH);
            add(users, encoder, "khach@audiostore.local", "Khách mua hàng", Role.CUSTOMER);
        };
    }
    private void add(UserRepository users, PasswordEncoder encoder, String email, String name, Role role) {
        if (!users.existsByEmailIgnoreCase(email)) users.save(new User(email, encoder.encode("AudioStore@2026"), name, null, role));
    }
}
