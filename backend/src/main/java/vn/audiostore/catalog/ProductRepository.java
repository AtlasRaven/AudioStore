package vn.audiostore.catalog;

import java.math.BigDecimal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {
    @EntityGraph(attributePaths={"category","brand"}) List<Product> findByActiveTrue();
    @EntityGraph(attributePaths={"category","brand"}) Optional<Product> findOneById(Long id);
    default Page<Product> browse(String q, Long category, Long brand, BigDecimal min, BigDecimal max, Pageable pageable, boolean includeInactive) {
        Specification<Product> spec = (root, query, cb) -> cb.conjunction();
        if (!includeInactive) spec = spec.and((r, qy, cb) -> cb.isTrue(r.get("active")));
        if (q != null && !q.isBlank()) spec = spec.and((r, qy, cb) -> cb.or(cb.like(cb.lower(r.get("name")), "%" + q.toLowerCase() + "%"), cb.like(cb.lower(r.get("sku")), "%" + q.toLowerCase() + "%"), cb.like(cb.lower(r.join("brand").get("name")), "%" + q.toLowerCase() + "%")));
        if (category != null) spec = spec.and((r, qy, cb) -> cb.equal(r.join("category").get("id"), category));
        if (brand != null) spec = spec.and((r, qy, cb) -> cb.equal(r.join("brand").get("id"), brand));
        if (min != null) spec = spec.and((r, qy, cb) -> cb.greaterThanOrEqualTo(r.get("salePrice"), min));
        if (max != null) spec = spec.and((r, qy, cb) -> cb.lessThanOrEqualTo(r.get("salePrice"), max));
        return findAll(spec, pageable);
    }
}
