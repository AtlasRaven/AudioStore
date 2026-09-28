package vn.audiostore.inventory;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PurchaseItemRepository extends JpaRepository<PurchaseItem, Long> { List<PurchaseItem> findByPurchaseIdOrderByProductId(Long purchaseId); }
