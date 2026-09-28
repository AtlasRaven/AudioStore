package vn.audiostore.catalog;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "products")
public class Product {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false, unique = true, length = 40) private String sku;
    @Column(nullable = false, length = 180) private String name;
    @Column(nullable = false, unique = true, length = 200) private String slug;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "category_id") private Category category;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "brand_id") private Brand brand;
    @Column(columnDefinition = "TEXT") private String description;
    @Column(columnDefinition = "TEXT") private String specifications;
    @Column(name = "sale_price", nullable = false, precision = 15, scale = 2) private BigDecimal salePrice;
    @Column(name = "cost_price", nullable = false, precision = 15, scale = 2) private BigDecimal costPrice;
    @Column(name = "image_url", length = 1000) private String imageUrl;
    @Column(name = "warranty_months", nullable = false) private int warrantyMonths;
    @Column(nullable = false) private boolean active = true;
    protected Product() {}
    public Product(String sku, String name, String slug, Category category, Brand brand, String description, String specifications, BigDecimal salePrice, BigDecimal costPrice, String imageUrl, int warrantyMonths) {
        this.sku=sku; this.name=name; this.slug=slug; this.category=category; this.brand=brand; this.description=description; this.specifications=specifications; this.salePrice=salePrice; this.costPrice=costPrice; this.imageUrl=imageUrl; this.warrantyMonths=warrantyMonths;
    }
    public Long getId() { return id; } public String getSku() { return sku; } public String getName() { return name; } public String getSlug() { return slug; }
    public Category getCategory() { return category; } public Brand getBrand() { return brand; } public String getDescription() { return description; }
    public String getSpecifications() { return specifications; } public BigDecimal getSalePrice() { return salePrice; } public BigDecimal getCostPrice() { return costPrice; }
    public String getImageUrl() { return imageUrl; } public int getWarrantyMonths() { return warrantyMonths; } public boolean isActive() { return active; }
    public void updateAll(String name, String slug, Category category, Brand brand, String description, String specs, BigDecimal salePrice, BigDecimal costPrice, String imageUrl, int warrantyMonths, boolean active) {
        this.name=name; this.slug=slug; this.category=category; this.brand=brand; this.description=description; this.specifications=specs; this.salePrice=salePrice; this.costPrice=costPrice; this.imageUrl=imageUrl; this.warrantyMonths=warrantyMonths; this.active=active;
    }
    public void updatePrice(BigDecimal price) { this.salePrice = price; }
    public void updateContent(String description, String imageUrl) { this.description=description; this.imageUrl=imageUrl; }
}
