package vn.audiostore.inventory;

import vn.audiostore.auth.User;
import vn.audiostore.auth.UserRepository;
import vn.audiostore.catalog.Product;
import vn.audiostore.catalog.ProductRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InventoryService {
    private final InventoryRepository inventories; private final StockMovementRepository movements;
    private final SupplierRepository suppliers; private final PurchaseRepository purchases;
    private final PurchaseItemRepository purchaseItems; private final ProductRepository products; private final UserRepository users;
    public InventoryService(InventoryRepository inventories, StockMovementRepository movements, SupplierRepository suppliers,
            PurchaseRepository purchases, PurchaseItemRepository purchaseItems, ProductRepository products, UserRepository users) {
        this.inventories=inventories; this.movements=movements; this.suppliers=suppliers; this.purchases=purchases; this.purchaseItems=purchaseItems; this.products=products; this.users=users;
    }
    @Transactional
    public InventoryView adjust(long productId, int delta, int minimum, String reason, String email) {
        requireProduct(productId);
        if (delta == 0 || minimum < 0 || reason == null || reason.isBlank()) throw new IllegalArgumentException("Số lượng và lý do điều chỉnh chưa hợp lệ.");
        Inventory stock = lockedOrCreate(productId);
        if (stock.getQuantity() + delta < stock.getReservedQuantity()) throw new IllegalArgumentException("Tồn khả dụng không đủ để điều chỉnh.");
        stock.add(delta); stock.setMinimumQuantity(minimum);
        movements.save(new StockMovement(productId, actor(email).getId(), "ADJUST", delta, reason.trim(), null));
        return view(stock);
    }
    @Transactional
    public InventoryView issue(long productId, int quantity, String reason, String email) {
        requireProduct(productId);
        if (quantity <= 0 || reason == null || reason.isBlank()) throw new IllegalArgumentException("Số lượng và lý do xuất chưa hợp lệ.");
        Inventory stock=lockedOrCreate(productId);
        if (stock.getAvailableQuantity() < quantity) throw new IllegalArgumentException("Tồn khả dụng không đủ để xuất.");
        stock.add(-quantity);
        movements.save(new StockMovement(productId,actor(email).getId(),"OUT",-quantity,reason.trim(),null));
        return view(stock);
    }
    @Transactional
    public PurchaseView createPurchase(long supplierId, List<PurchaseLine> lines, String email) {
        Supplier supplier=suppliers.findById(supplierId).filter(Supplier::isActive).orElseThrow(() -> new IllegalArgumentException("Không tìm thấy nhà cung cấp."));
        if (lines == null || lines.isEmpty()) throw new IllegalArgumentException("Phiếu nhập cần có ít nhất một sản phẩm.");
        BigDecimal total=BigDecimal.ZERO;
        for (PurchaseLine line : lines) {
            if (line.productId()==null || line.quantity()<=0 || line.unitCost()==null || line.unitCost().signum()<0) throw new IllegalArgumentException("Dòng hàng trong phiếu nhập chưa hợp lệ.");
            requireProduct(line.productId()); total=total.add(line.unitCost().multiply(BigDecimal.valueOf(line.quantity())));
        }
        Purchase purchase=purchases.save(new Purchase(supplier,actor(email).getId(),total));
        for (PurchaseLine line : lines) purchaseItems.save(new PurchaseItem(purchase,line.productId(),line.quantity(),line.unitCost()));
        return purchaseView(purchase);
    }
    @Transactional
    public PurchaseView receive(long id, String email) {
        Purchase purchase=purchases.findById(id).orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phiếu nhập."));
        if (!"DRAFT".equals(purchase.getStatus())) throw new IllegalArgumentException("Phiếu nhập đã được xác nhận.");
        List<PurchaseItem> lines=purchaseItems.findByPurchaseIdOrderByProductId(id);
        for (PurchaseItem line : lines) {
            Inventory stock=lockedOrCreate(line.getProductId()); stock.add(line.getQuantity());
            movements.save(new StockMovement(line.getProductId(),actor(email).getId(),"PURCHASE_IN",line.getQuantity(),"Nhập theo phiếu #"+id,id));
        }
        purchase.receive();
        return purchaseView(purchase);
    }
    public List<InventoryView> listInventory(String query, boolean lowStock) {
        Map<Long,Product> productMap=products.findAll().stream().collect(Collectors.toMap(Product::getId,p -> p));
        return inventories.findAll().stream().filter(i -> productMap.containsKey(i.getProductId()))
                .filter(i -> query == null || query.isBlank() || productMap.get(i.getProductId()).getName().toLowerCase().contains(query.toLowerCase()) || productMap.get(i.getProductId()).getSku().toLowerCase().contains(query.toLowerCase()))
                .filter(i -> !lowStock || i.getQuantity() <= i.getMinimumQuantity()).map(this::view).toList();
    }
    public List<StockMovement> movements() { return movements.findAll(org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC,"createdAt")); }
    public List<Supplier> suppliers() { return suppliers.findByActiveTrueOrderByNameAsc(); }
    @Transactional public Supplier createSupplier(String name,String phone,String email,String address) {
        if (name==null || name.isBlank()) throw new IllegalArgumentException("Tên nhà cung cấp không được để trống.");
        return suppliers.save(new Supplier(name.trim(),phone,email,address));
    }
    @Transactional(readOnly = true) public List<PurchaseView> purchases() { return purchases.findAll(org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC,"createdAt")).stream().map(this::purchaseView).toList(); }
    private Inventory lockedOrCreate(long productId) {
        return inventories.findLocked(productId).orElseGet(() -> inventories.saveAndFlush(new Inventory(productId,0,0)));
    }
    private Product requireProduct(long id) { return products.findById(id).orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sản phẩm.")); }
    private User actor(String email) { return users.findByEmailIgnoreCase(email).orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài khoản.")); }
    private InventoryView view(Inventory i) { Product p=requireProduct(i.getProductId()); return new InventoryView(i.getProductId(),p.getSku(),p.getName(),i.getQuantity(),i.getReservedQuantity(),i.getAvailableQuantity(),i.getMinimumQuantity(),i.getAvailableQuantity()<=i.getMinimumQuantity()); }
    private PurchaseView purchaseView(Purchase p) {
        List<PurchaseLineView> lines=purchaseItems.findByPurchaseIdOrderByProductId(p.getId()).stream().map(x -> new PurchaseLineView(x.getProductId(),requireProduct(x.getProductId()).getName(),x.getQuantity(),x.getUnitCost())).toList();
        return new PurchaseView(p.getId(),p.getSupplier().getId(),p.getSupplier().getName(),p.getCreatedBy(),p.getStatus(),p.getTotal(),p.getCreatedAt(),p.getReceivedAt(),lines);
    }
    public record PurchaseLine(Long productId,int quantity,BigDecimal unitCost) {}
    public record PurchaseLineView(Long productId,String productName,int quantity,BigDecimal unitCost) {}
    public record PurchaseView(Long id,Long supplierId,String supplierName,Long createdBy,String status,BigDecimal total,java.time.Instant createdAt,java.time.Instant receivedAt,List<PurchaseLineView> items) {}
    public record InventoryView(Long productId,String sku,String productName,int quantity,int reservedQuantity,int availableQuantity,int minimumQuantity,boolean lowStock) {}
}
