package vn.audiostore.orders;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order,Long> { List<Order> findByUserIdOrderByCreatedAtDesc(Long userId); Optional<Order> findByIdAndUserId(Long id,Long userId); List<Order> findAllByOrderByCreatedAtDesc(); List<Order> findByStatusAndCreatedAtBetweenOrderByCreatedAtAsc(String status,java.time.Instant from,java.time.Instant to); }
