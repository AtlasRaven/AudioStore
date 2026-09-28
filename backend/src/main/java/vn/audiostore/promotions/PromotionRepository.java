package vn.audiostore.promotions;

import java.time.Instant;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PromotionRepository extends JpaRepository<Promotion,Long>{
    List<Promotion> findByActiveTrueAndStartsAtLessThanEqualAndEndsAtGreaterThanEqual(Instant now1,Instant now2);
    List<Promotion> findByActiveTrueAndStartsAtLessThanEqualAndEndsAtGreaterThanEqualOrderByStartsAtAsc(Instant now1,Instant now2);
}
