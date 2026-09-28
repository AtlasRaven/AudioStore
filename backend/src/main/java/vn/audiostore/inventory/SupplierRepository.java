package vn.audiostore.inventory;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SupplierRepository extends JpaRepository<Supplier, Long> { List<Supplier> findByActiveTrueOrderByNameAsc(); }
