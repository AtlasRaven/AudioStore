package vn.audiostore.catalog;

import java.math.BigDecimal;
import java.text.Normalizer;
import java.util.Locale;
import vn.audiostore.inventory.Inventory;
import vn.audiostore.inventory.InventoryRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CatalogService {
    private final ProductRepository products; private final CategoryRepository categories; private final BrandRepository brands; private final InventoryRepository inventory;
    public CatalogService(ProductRepository products, CategoryRepository categories, BrandRepository brands, InventoryRepository inventory) { this.products=products; this.categories=categories; this.brands=brands; this.inventory=inventory; }
    @Transactional(readOnly = true) public Page<ProductPublicView> browse(String q, Long category, Long brand, BigDecimal min, BigDecimal max, Pageable pageable) {
        return products.browse(q, category, brand, min, max, pageable, false).map(this::publicView);
    }
    @Transactional(readOnly = true) public ProductPublicView get(long id) { return publicView(products.findById(id).filter(Product::isActive).orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sản phẩm."))); }
    private ProductPublicView publicView(Product p) { return ProductPublicView.from(p, inventory.findById(p.getId()).map(Inventory::getAvailableQuantity).orElse(0)); }
    @Transactional(readOnly = true) public Page<ProductStaffView> adminBrowse(String q, Pageable pageable) { return products.browse(q, null, null, null, null, pageable, true).map(ProductStaffView::from); }
    @Transactional(readOnly = true) public Page<ProductView> adminBrowseWithCosts(String q, Pageable pageable) { return products.browse(q, null, null, null, null, pageable, true).map(ProductView::from); }
    public java.util.List<Category> categories() { return categories.findByActiveTrueOrderByNameAsc(); }
    public java.util.List<Brand> brands() { return brands.findByActiveTrueOrderByNameAsc(); }
    @Transactional
    public ProductView create(ProductInput in) {
        validateAdmin(in);
        Category category = categories.findById(in.categoryId()).orElseThrow(() -> new IllegalArgumentException("Danh mục không tồn tại."));
        Brand brand = brands.findById(in.brandId()).orElseThrow(() -> new IllegalArgumentException("Thương hiệu không tồn tại."));
        Product p = new Product(in.sku().trim(), in.name().trim(), slug(in.name()), category, brand, in.description(), in.specifications(), in.salePrice(), in.costPrice(), in.imageUrl(), in.warrantyMonths());
        Product saved=products.save(p);
        inventory.save(new Inventory(saved.getId(),0,3));
        return ProductView.from(saved);
    }
    @Transactional
    public ProductView update(long id, ProductInput in, String role) {
        Product p = products.findById(id).orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sản phẩm."));
        if ("STAFF_BAN_HANG".equals(role)) {
            if (in.salePrice() == null || in.salePrice().signum() < 0) throw new IllegalArgumentException("Giá bán chưa hợp lệ.");
            p.updatePrice(in.salePrice());
        } else if ("MARKETING_CSKH".equals(role)) {
            p.updateContent(in.description(), in.imageUrl());
        } else {
            validateAdmin(in);
            Category c=categories.findById(in.categoryId()).orElseThrow(() -> new IllegalArgumentException("Danh mục không tồn tại."));
            Brand b=brands.findById(in.brandId()).orElseThrow(() -> new IllegalArgumentException("Thương hiệu không tồn tại."));
            p.updateAll(in.name().trim(), slug(in.name()), c, b, in.description(), in.specifications(), in.salePrice(), in.costPrice(), in.imageUrl(), in.warrantyMonths(), in.active());
        }
        return ProductView.from(p);
    }
    @Transactional public void deactivate(long id) { Product p=products.findById(id).orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sản phẩm.")); p.updateAll(p.getName(),p.getSlug(),p.getCategory(),p.getBrand(),p.getDescription(),p.getSpecifications(),p.getSalePrice(),p.getCostPrice(),p.getImageUrl(),p.getWarrantyMonths(),false); }
    @Transactional
    public Category saveCategory(Long id, String name, boolean active) { Category c=id == null ? new Category(name.trim(), slug(name)) : categories.findById(id).orElseThrow(() -> new IllegalArgumentException("Không tìm thấy danh mục.")); c.update(name.trim(), slug(name), active); return categories.save(c); }
    @Transactional
    public Brand saveBrand(Long id, String name, boolean active) { Brand b=id == null ? new Brand(name.trim(), slug(name)) : brands.findById(id).orElseThrow(() -> new IllegalArgumentException("Không tìm thấy thương hiệu.")); b.update(name.trim(), slug(name), active); return brands.save(b); }
    @Transactional public void deactivateCategory(long id) { Category c=categories.findById(id).orElseThrow(() -> new IllegalArgumentException("Không tìm thấy danh mục.")); c.update(c.getName(), c.getSlug(), false); }
    @Transactional public void deactivateBrand(long id) { Brand b=brands.findById(id).orElseThrow(() -> new IllegalArgumentException("Không tìm thấy thương hiệu.")); b.update(b.getName(), b.getSlug(), false); }
    private void validateAdmin(ProductInput in) {
        if (in.sku() == null || in.sku().isBlank() || in.name() == null || in.name().isBlank() || in.categoryId() == null || in.brandId() == null || in.salePrice() == null || in.costPrice() == null || in.salePrice().signum() < 0 || in.costPrice().signum() < 0 || in.warrantyMonths() < 0) throw new IllegalArgumentException("Thông tin sản phẩm chưa đầy đủ hoặc chưa hợp lệ.");
    }
    private String slug(String text) { return Normalizer.normalize(text.toLowerCase(Locale.ROOT), Normalizer.Form.NFD).replaceAll("\\p{M}", "").replace('đ','d').replaceAll("[^a-z0-9]+", "-").replaceAll("^-|-$", ""); }
    public record ProductInput(String sku, String name, Long categoryId, Long brandId, String description, String specifications, BigDecimal salePrice, BigDecimal costPrice, String imageUrl, int warrantyMonths, boolean active) {}
}
