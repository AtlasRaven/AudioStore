package vn.audiostore.orders;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemRepository extends JpaRepository<OrderItem,Long> { List<OrderItem> findByOrderIdOrderById(Long orderId); List<OrderItem> findByOrderIdIn(java.util.Collection<Long> orderIds); }
