package vn.audiostore.auth;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {
    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final JwtService jwt;
    public AuthService(UserRepository users, PasswordEncoder encoder, JwtService jwt) { this.users = users; this.encoder = encoder; this.jwt = jwt; }

    @Transactional
    public LoginResult register(String email, String password, String fullName, String phone) {
        if (users.existsByEmailIgnoreCase(email)) throw new IllegalArgumentException("Email này đã được sử dụng.");
        User user = users.save(new User(email, encoder.encode(password), fullName, phone, Role.CUSTOMER));
        return new LoginResult(jwt.issue(user), UserView.from(user));
    }
    public LoginResult login(String email, String password) {
        User user = users.findByEmailIgnoreCase(email).orElseThrow(() -> new IllegalArgumentException("Email hoặc mật khẩu chưa đúng."));
        if (!user.isEnabled() || !encoder.matches(password, user.getPasswordHash())) throw new IllegalArgumentException("Email hoặc mật khẩu chưa đúng.");
        return new LoginResult(jwt.issue(user), UserView.from(user));
    }
    public User getByEmail(String email) { return users.findByEmailIgnoreCase(email).orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài khoản.")); }
    @Transactional
    public void changePassword(String email, String oldPassword, String newPassword) {
        User user = getByEmail(email);
        if (!encoder.matches(oldPassword, user.getPasswordHash())) throw new IllegalArgumentException("Mật khẩu hiện tại chưa đúng.");
        user.changePassword(encoder.encode(newPassword));
    }
    public Page<UserView> listUsers(Pageable pageable) { return users.findAll(pageable).map(UserView::from); }
    @Transactional
    public UserView updateAccess(long id, Role role, boolean enabled) {
        User user = users.findById(id).orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài khoản."));
        user.updateAccess(role, enabled); return UserView.from(user);
    }
    public record LoginResult(String token, UserView user) {}
}
