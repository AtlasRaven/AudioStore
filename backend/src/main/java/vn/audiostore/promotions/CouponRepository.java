package vn.audiostore.promotions;

import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CouponRepository extends JpaRepository<Coupon,Long>{
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from Coupon c where upper(c.code)=upper(:code)") Optional<Coupon> findLockedByCode(@Param("code") String code);
    Optional<Coupon> findByCodeIgnoreCase(String code);
}
