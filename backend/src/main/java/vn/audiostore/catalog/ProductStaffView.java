package vn.audiostore.catalog;

import java.math.BigDecimal;

public record ProductStaffView(Long id,String sku,String name,String slug,Long categoryId,String categoryName,Long brandId,String brandName,
        String description,String specifications,BigDecimal salePrice,String imageUrl,int warrantyMonths,boolean active){
    public static ProductStaffView from(Product p){return new ProductStaffView(p.getId(),p.getSku(),p.getName(),p.getSlug(),p.getCategory().getId(),p.getCategory().getName(),p.getBrand().getId(),p.getBrand().getName(),p.getDescription(),p.getSpecifications(),p.getSalePrice(),p.getImageUrl(),p.getWarrantyMonths(),p.isActive());}
    public static ProductStaffView from(ProductView p){return new ProductStaffView(p.id(),p.sku(),p.name(),p.slug(),p.categoryId(),p.categoryName(),p.brandId(),p.brandName(),p.description(),p.specifications(),p.salePrice(),p.imageUrl(),p.warrantyMonths(),p.active());}
}
