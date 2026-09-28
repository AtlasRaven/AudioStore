package vn.audiostore.catalog;

import java.math.BigDecimal;

public record ProductView(Long id, String sku, String name, String slug, Long categoryId, String categoryName,
        Long brandId, String brandName, String description, String specifications, BigDecimal salePrice,
        BigDecimal costPrice, String imageUrl, int warrantyMonths, boolean active) {
    public static ProductView from(Product p) {
        return new ProductView(p.getId(), p.getSku(), p.getName(), p.getSlug(), p.getCategory().getId(), p.getCategory().getName(),
                p.getBrand().getId(), p.getBrand().getName(), p.getDescription(), p.getSpecifications(), p.getSalePrice(), p.getCostPrice(), p.getImageUrl(), p.getWarrantyMonths(), p.isActive());
    }
}
