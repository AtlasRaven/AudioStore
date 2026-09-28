package vn.audiostore.catalog;

import jakarta.persistence.*;

@Entity
@Table(name = "brands")
public class Brand {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false, unique = true, length = 100) private String name;
    @Column(nullable = false, unique = true, length = 120) private String slug;
    @Column(nullable = false) private boolean active = true;
    protected Brand() {}
    public Brand(String name, String slug) { this.name = name; this.slug = slug; }
    public Long getId() { return id; } public String getName() { return name; } public String getSlug() { return slug; } public boolean isActive() { return active; }
    public void update(String name, String slug, boolean active) { this.name = name; this.slug = slug; this.active = active; }
}
