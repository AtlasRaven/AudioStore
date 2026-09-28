package vn.audiostore.orders;

import vn.audiostore.auth.User;
import vn.audiostore.auth.UserRepository;
import vn.audiostore.catalog.Product;
import vn.audiostore.catalog.ProductRepository;
import vn.audiostore.inventory.Inventory;
import vn.audiostore.inventory.InventoryRepository;
import vn.audiostore.inventory.StockMovement;
import vn.audiostore.inventory.StockMovementRepository;
import vn.audiostore.promotions.PromotionService;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderService {
    private final OrderRepository orders; private final OrderItemRepository items; private final PaymentRepository payments;
    private final ShipmentRepository shipments; private final AddressRepository addresses; private final ProductRepository products;
    private final InventoryRepository inventory; private final StockMovementRepository movements; private final UserRepository users;
    private final PromotionService promotionService;
    private final String bankName,bankAccount,bankAccountName;
    public OrderService(OrderRepository orders,OrderItemRepository items,PaymentRepository payments,ShipmentRepository shipments,
            AddressRepository addresses,ProductRepository products,InventoryRepository inventory,StockMovementRepository movements,UserRepository users,PromotionService promotionService,
            @Value("${app.bank.name:Ngân hàng mẫu}") String bankName,@Value("${app.bank.account:}") String bankAccount,@Value("${app.bank.account-name:AudioStore}") String bankAccountName) {
        this.orders=orders;this.items=items;this.payments=payments;this.shipments=shipments;this.addresses=addresses;this.products=products;this.inventory=inventory;this.movements=movements;this.users=users;
        this.promotionService=promotionService;
        this.bankName=bankName;this.bankAccount=bankAccount;this.bankAccountName=bankAccountName;
    }
    @Transactional
    public OrderView place(String email,CreateOrder request) {
        User user=user(email);
        Address address=addresses.findByIdAndUserId(request.addressId(),user.getId()).orElseThrow(() -> new IllegalArgumentException("Địa chỉ giao hàng không tồn tại."));
        if (request.items()==null || request.items().isEmpty()) throw new IllegalArgumentException("Giỏ hàng đang trống.");
        if (!Set.of("COD","BANK_TRANSFER").contains(request.paymentMethod())) throw new IllegalArgumentException("Hình thức thanh toán chưa được hỗ trợ.");
        Map<Long,Integer> quantities=new TreeMap<>();
        for (OrderRequestLine line:request.items()) {
            if (line.productId()==null || line.quantity()<1 || line.quantity()>99) throw new IllegalArgumentException("Số lượng sản phẩm chưa hợp lệ.");
            quantities.merge(line.productId(),line.quantity(),Integer::sum);
        }
        if(quantities.values().stream().anyMatch(q->q>99))throw new IllegalArgumentException("Mỗi sản phẩm chỉ nhận tối đa 99 chiếc trong một đơn.");
        Map<Long,Inventory> locked=new LinkedHashMap<>();
        for (Long productId:quantities.keySet()) {
            Inventory stock=inventory.findLocked(productId).orElseThrow(() -> new IllegalArgumentException("Sản phẩm chưa có thông tin tồn kho."));
            if (stock.getAvailableQuantity()<quantities.get(productId)) throw new IllegalArgumentException("Một sản phẩm trong giỏ không đủ số lượng tồn.");
            locked.put(productId,stock);
        }
        List<Product> catalog=products.findAllById(quantities.keySet());
        Map<Long,Product> byId=catalog.stream().collect(Collectors.toMap(Product::getId,p->p));
        if (byId.size()!=quantities.size() || catalog.stream().anyMatch(p->!p.isActive())) throw new IllegalArgumentException("Một sản phẩm trong giỏ hiện không còn bán.");
        PromotionService.Quote quote=promotionService.quote(quantities,request.couponCode(),true);
        String code="AS"+java.time.format.DateTimeFormatter.ofPattern("yyMMddHHmmss").withZone(java.time.ZoneOffset.UTC).format(java.time.Instant.now())+UUID.randomUUID().toString().substring(0,4).toUpperCase(Locale.ROOT);
        String status="BANK_TRANSFER".equals(request.paymentMethod())?"CHO_THANH_TOAN":"CHO_XAC_NHAN";
        Order order=orders.save(new Order(code,user.getId(),address,quote.couponId(),status,request.paymentMethod(),quote.subtotal(),quote.couponDiscount(),quote.total()));
        for (var entry:quantities.entrySet()) {
            Product p=byId.get(entry.getKey()); int quantity=entry.getValue();
            items.save(new OrderItem(order.getId(),p.getId(),p.getName(),quantity,quote.prices().get(p.getId()),p.getCostPrice()));
            locked.get(p.getId()).reserve(quantity);
            movements.save(new StockMovement(p.getId(),user.getId(),"ORDER_RESERVE",0,"Giữ hàng cho đơn "+order.getCode(),order.getId()));
        }
        payments.save(new Payment(order.getId(),"BANK_TRANSFER".equals(request.paymentMethod())?"PENDING":"UNPAID",request.paymentMethod(),quote.total()));
        return view(order);
    }
    public List<OrderView> mine(String email) { return orders.findByUserIdOrderByCreatedAtDesc(user(email).getId()).stream().map(this::view).toList(); }
    public List<OrderView> list(String status) { return orders.findAllByOrderByCreatedAtDesc().stream().filter(o->status==null||status.isBlank()||status.equals(o.getStatus())).map(this::view).toList(); }
    @Transactional
    public OrderView transition(long id,String next,String email,String role) {
        Order order=orders.findById(id).orElseThrow(()->new IllegalArgumentException("Không tìm thấy đơn hàng."));
        String current=order.getStatus();
        boolean allowed;
        if ("STAFF_KHO".equals(role)) allowed="DANG_GIAO".equals(next)&&"DA_XAC_NHAN".equals(current);
        else if ("STAFF_BAN_HANG".equals(role)) allowed=validSalesTransition(current,next);
        else allowed=validAdminTransition(current,next);
        if (!allowed) throw new IllegalArgumentException("Không thể chuyển đơn từ trạng thái "+current+" sang "+next+".");
        if("STAFF_BAN_HANG".equals(role)&&Set.of("DA_XAC_NHAN","DANG_GIAO").contains(next)) {
            Long actorId=user(email).getId();
            if(order.getSalesOwnerId()!=null&&!order.getSalesOwnerId().equals(actorId))throw new IllegalArgumentException("Đơn hàng đã được nhân viên khác phụ trách.");
            order.assignSalesOwner(actorId);
        }
        if ("DA_HUY".equals(next)) releaseOrder(order,email);
        if ("DANG_GIAO".equals(next)) shipOrder(order,email);
        if ("HOAN_THANH".equals(next)) payments.findByOrderId(id).ifPresent(payment -> payment.confirm(user(email).getId()));
        order.changeStatus(next);
        return view(order);
    }
    @Transactional
    public OrderView confirmPayment(long id,String email) {
        Order order=orders.findById(id).orElseThrow(()->new IllegalArgumentException("Không tìm thấy đơn hàng."));
        if (!"CHO_THANH_TOAN".equals(order.getStatus())) throw new IllegalArgumentException("Đơn này không chờ xác nhận chuyển khoản.");
        Payment payment=payments.findByOrderId(id).orElseThrow(()->new IllegalArgumentException("Không tìm thấy thanh toán."));
        payment.confirm(user(email).getId()); order.changeStatus("CHO_XAC_NHAN");
        return view(order);
    }
    @Transactional
    public OrderView cancelMine(long id,String email) {
        Order order=orders.findByIdAndUserId(id,user(email).getId()).orElseThrow(()->new IllegalArgumentException("Không tìm thấy đơn hàng."));
        if (!Set.of("CHO_THANH_TOAN","CHO_XAC_NHAN").contains(order.getStatus())) throw new IllegalArgumentException("Đơn hàng không còn trong thời hạn hủy.");
        releaseOrder(order,email); order.changeStatus("DA_HUY"); return view(order);
    }
    @Transactional
    public AddressView addAddress(String email,AddressRequest request) {
        User u=user(email); boolean makeDefault=request.defaultAddress()||addresses.findByUserIdOrderByDefaultAddressDescIdDesc(u.getId()).isEmpty();
        if(makeDefault) addresses.findByUserIdOrderByDefaultAddressDescIdDesc(u.getId()).forEach(a -> { if(a.isDefaultAddress()) clearDefault(a); });
        Address a=addresses.save(new Address(u.getId(),request.recipient().trim(),request.phone().trim(),request.province().trim(),request.district().trim(),request.detail().trim(),makeDefault)); return AddressView.from(a);
    }
    public List<AddressView> addresses(String email) { return addresses.findByUserIdOrderByDefaultAddressDescIdDesc(user(email).getId()).stream().map(AddressView::from).toList(); }
    private void clearDefault(Address address) { address.setDefaultAddress(false); }
    private void releaseOrder(Order order,String email) {
        for(OrderItem item:items.findByOrderIdOrderById(order.getId())) { Inventory stock=inventory.findLocked(item.getProductId()).orElseThrow(); stock.release(item.getQuantity()); movements.save(new StockMovement(item.getProductId(),user(email).getId(),"ORDER_RELEASE",0,"Hủy giữ hàng cho đơn "+order.getCode(),order.getId())); }
        promotionService.releaseCoupon(order.getCouponId());
    }
    private void shipOrder(Order order,String email) {
        if(shipments.existsByOrderId(order.getId())) throw new IllegalArgumentException("Đơn này đã có phiếu xuất.");
        for(OrderItem item:items.findByOrderIdOrderById(order.getId())) {
            Inventory stock=inventory.findLocked(item.getProductId()).orElseThrow();
            if(stock.getReservedQuantity()<item.getQuantity()||stock.getQuantity()<item.getQuantity()) throw new IllegalArgumentException("Tồn kho giữ cho đơn không hợp lệ.");
            stock.ship(item.getQuantity()); movements.save(new StockMovement(item.getProductId(),user(email).getId(),"ORDER_SHIP",-item.getQuantity(),"Xuất theo đơn "+order.getCode(),order.getId()));
        }
        shipments.save(new Shipment(order.getId(),"DANG_GIAO","Tạo tự động khi chuyển đơn sang đang giao."));
    }
    private boolean validSalesTransition(String current,String next) {
        if("CHO_XAC_NHAN".equals(current)&&Set.of("DA_XAC_NHAN","DA_HUY").contains(next)) return true;
        if("DA_XAC_NHAN".equals(current)&&"DANG_GIAO".equals(next)) return true;
        return "DANG_GIAO".equals(current)&&"HOAN_THANH".equals(next);
    }
    private boolean validAdminTransition(String current,String next) {
        if("DA_HUY".equals(next)) return Set.of("CHO_THANH_TOAN","CHO_XAC_NHAN","DA_XAC_NHAN").contains(current);
        return validSalesTransition(current,next);
    }
    private User user(String email) { return users.findByEmailIgnoreCase(email).orElseThrow(()->new IllegalArgumentException("Không tìm thấy tài khoản.")); }
    private OrderView view(Order order) {
        List<OrderLineView> orderLines=items.findByOrderIdOrderById(order.getId()).stream().map(i->new OrderLineView(i.getProductId(),i.getProductName(),i.getQuantity(),i.getUnitPrice())).toList();
        Payment payment=payments.findByOrderId(order.getId()).orElse(null);
        TransferInfo transfer="BANK_TRANSFER".equals(order.getPaymentMethod())?new TransferInfo(bankName,bankAccount,bankAccountName,order.getCode()):null;
        return new OrderView(order.getId(),order.getCode(),order.getUserId(),order.getRecipient(),order.getPhone(),order.getShippingAddress(),order.getStatus(),order.getPaymentMethod(),order.getSubtotal(),order.getDiscount(),order.getTotal(),order.getCreatedAt(),orderLines,payment==null?null:payment.getStatus(),transfer);
    }
    public record OrderRequestLine(Long productId,int quantity) {}
    public record CreateOrder(Long addressId,String paymentMethod,String couponCode,List<OrderRequestLine> items) {}
    public record OrderLineView(Long productId,String productName,int quantity,BigDecimal unitPrice) {}
    public record TransferInfo(String bankName,String account,String accountName,String transferContent) {}
    public record OrderView(Long id,String code,Long userId,String recipient,String phone,String shippingAddress,String status,String paymentMethod,BigDecimal subtotal,BigDecimal discount,BigDecimal total,java.time.Instant createdAt,List<OrderLineView> items,String paymentStatus,TransferInfo transferInfo) {}
    public record AddressRequest(@jakarta.validation.constraints.NotBlank @jakarta.validation.constraints.Size(max=120) String recipient,
            @jakarta.validation.constraints.NotBlank @jakarta.validation.constraints.Size(max=30) String phone,
            @jakarta.validation.constraints.NotBlank @jakarta.validation.constraints.Size(max=100) String province,
            @jakarta.validation.constraints.NotBlank @jakarta.validation.constraints.Size(max=100) String district,
            @jakarta.validation.constraints.NotBlank @jakarta.validation.constraints.Size(max=500) String detail,boolean defaultAddress) {}
    public record AddressView(Long id,String recipient,String phone,String province,String district,String detail,boolean defaultAddress) { static AddressView from(Address a){return new AddressView(a.getId(),a.getRecipient(),a.getPhone(),a.getProvince(),a.getDistrict(),a.getDetail(),a.isDefaultAddress());} }
}
