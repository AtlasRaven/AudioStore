package vn.audiostore.catalog;

import java.math.BigDecimal;

public record ProductPublicView(Long id, String sku, String name, String slug, Long categoryId, String categoryName,
        Long brandId, String brandName, String description, String specifications, BigDecimal salePrice,
        String imageUrl, int warrantyMonths, int availableQuantity) {
    public static ProductPublicView from(Product p, int availableQuantity) {
        return new ProductPublicView(p.getId(), p.getSku(), p.getName(), p.getSlug(), p.getCategory().getId(), p.getCategory().getName(),
                p.getBrand().getId(), p.getBrand().getName(), p.getDescription(), p.getSpecifications(), p.getSalePrice(), p.getImageUrl(), p.getWarrantyMonths(), availableQuantity);
    }
}
