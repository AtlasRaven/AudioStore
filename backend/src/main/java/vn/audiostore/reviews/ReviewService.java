package vn.audiostore.reviews;

import vn.audiostore.auth.User;
import vn.audiostore.auth.UserRepository;
import vn.audiostore.catalog.ProductRepository;
import vn.audiostore.orders.Order;
import vn.audiostore.orders.OrderItemRepository;
import vn.audiostore.orders.OrderRepository;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReviewService {
    private final ReviewRepository reviews;private final ReviewSummaryRepository summaries;private final OrderRepository orders;private final OrderItemRepository items;private final ProductRepository products;private final UserRepository users;
    public ReviewService(ReviewRepository reviews,ReviewSummaryRepository summaries,OrderRepository orders,OrderItemRepository items,ProductRepository products,UserRepository users){this.reviews=reviews;this.summaries=summaries;this.orders=orders;this.items=items;this.products=products;this.users=users;}
    @Transactional
    public ReviewView create(long productId,long orderId,int rating,String content,String email){
        if(rating<1||rating>5||content==null||content.isBlank())throw new IllegalArgumentException("Đánh giá cần có số sao và nhận xét.");
        if(!products.existsById(productId))throw new IllegalArgumentException("Không tìm thấy sản phẩm.");
        User user=users.findByEmailIgnoreCase(email).orElseThrow(()->new IllegalArgumentException("Không tìm thấy tài khoản."));
        Order order=orders.findByIdAndUserId(orderId,user.getId()).orElseThrow(()->new IllegalArgumentException("Đơn hàng không thuộc tài khoản này."));
        if(!"HOAN_THANH".equals(order.getStatus()))throw new IllegalArgumentException("Chỉ có thể đánh giá đơn đã hoàn thành.");
        if(items.findByOrderIdOrderById(orderId).stream().noneMatch(i->i.getProductId().equals(productId)))throw new IllegalArgumentException("Sản phẩm không nằm trong đơn hàng này.");
        if(reviews.existsByOrderIdAndProductId(orderId,productId))throw new IllegalArgumentException("Sản phẩm này đã được đánh giá trong đơn hàng.");
        return view(reviews.save(new Review(productId,user.getId(),orderId,rating,content.trim())));
    }
    @Transactional(readOnly=true) public List<ReviewView> visible(long productId){return reviews.findByProductIdAndVisibleTrueOrderByCreatedAtDesc(productId).stream().map(this::view).toList();}
    @Transactional(readOnly=true) public Page<ReviewView> all(int page,int size){return reviews.findAllByOrderByCreatedAtDesc(PageRequest.of(Math.max(0,page),Math.min(100,Math.max(1,size)),Sort.unsorted())).map(this::view);}
    @Transactional public ReviewView moderate(long id,boolean visible){Review review=reviews.findById(id).orElseThrow(()->new IllegalArgumentException("Không tìm thấy đánh giá."));review.setVisible(visible);summaries.deleteById(review.getProductId());return view(review);}
    public long visibleCount(long productId){return reviews.countByProductIdAndVisibleTrue(productId);}
    @Transactional(readOnly=true) public List<Review> visibleEntities(long productId){return reviews.findByProductIdAndVisibleTrueOrderByCreatedAtDesc(productId);}
    private ReviewView view(Review r){return new ReviewView(r.getId(),r.getProductId(),r.getOrderId(),r.getRating(),r.getContent(),r.isVisible(),r.getCreatedAt());}
    public record ReviewView(Long id,Long productId,Long orderId,int rating,String content,boolean visible,java.time.Instant createdAt){}
}
