package vn.audiostore.inventory;

import jakarta.persistence.*;

@Entity
@Table(name = "inventory")
public class Inventory {
    @Id @Column(name = "product_id") private Long productId;
    @Column(nullable = false) private int quantity;
    @Column(name = "reserved_quantity", nullable = false) private int reservedQuantity;
    @Column(name = "minimum_quantity", nullable = false) private int minimumQuantity;
    @Version private long version;
    protected Inventory() {}
    public Inventory(Long productId, int quantity, int minimumQuantity) { this.productId=productId; this.quantity=quantity; this.minimumQuantity=minimumQuantity; }
    public Long getProductId() { return productId; } public int getQuantity() { return quantity; } public int getReservedQuantity() { return reservedQuantity; } public int getAvailableQuantity() { return quantity-reservedQuantity; } public int getMinimumQuantity() { return minimumQuantity; }
    public void add(int amount) { quantity += amount; }
    public void setQuantity(int value) { quantity = value; }
    public void setMinimumQuantity(int value) { minimumQuantity = value; }
    public void reserve(int amount) { reservedQuantity += amount; }
    public void release(int amount) { reservedQuantity -= amount; }
    public void ship(int amount) { reservedQuantity -= amount; quantity -= amount; }
}
