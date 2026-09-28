package vn.audiostore.auth;

public record UserView(Long id, String email, String fullName, String phone, Role role, boolean enabled) {
    public static UserView from(User user) { return new UserView(user.getId(), user.getEmail(), user.getFullName(), user.getPhone(), user.getRole(), user.isEnabled()); }
}
