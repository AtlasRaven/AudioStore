package vn.audiostore.reviews;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewRepository extends JpaRepository<Review,Long>{
    boolean existsByOrderIdAndProductId(Long orderId,Long productId);
    List<Review> findByProductIdAndVisibleTrueOrderByCreatedAtDesc(Long productId);
    long countByProductIdAndVisibleTrue(Long productId);
    Page<Review> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
