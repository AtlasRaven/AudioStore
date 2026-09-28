package vn.audiostore.reports;

import vn.audiostore.auth.User;
import vn.audiostore.auth.UserRepository;
import vn.audiostore.catalog.Product;
import vn.audiostore.catalog.ProductRepository;
import vn.audiostore.orders.Order;
import vn.audiostore.orders.OrderItem;
import vn.audiostore.orders.OrderItemRepository;
import vn.audiostore.orders.OrderRepository;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReportService {
    private static final ZoneId ZONE=ZoneId.of("Asia/Ho_Chi_Minh");
    private final OrderRepository orders; private final OrderItemRepository items; private final ProductRepository products; private final UserRepository users;
    public ReportService(OrderRepository orders,OrderItemRepository items,ProductRepository products,UserRepository users){this.orders=orders;this.items=items;this.products=products;this.users=users;}

    @Transactional(readOnly=true)
    public RevenueReport revenue(LocalDate from,LocalDate to,String groupBy,String email,String role){
        if(!Set.of("day","month","year").contains(groupBy))throw new IllegalArgumentException("Chọn nhóm ngày, tháng hoặc năm.");
        Dataset dataset=dataset(from,to,email,role);
        Map<String,Accumulator> groups=new TreeMap<>();
        for(Order order:dataset.orders()){
            String key=periodKey(order,groupBy);
            Accumulator acc=groups.computeIfAbsent(key,k->new Accumulator());
            acc.revenue=acc.revenue.add(order.getTotal()); acc.profit=acc.profit.add(dataset.profitByOrder().getOrDefault(order.getId(),BigDecimal.ZERO)); acc.count++;
        }
        List<RevenuePoint> series=groups.entrySet().stream().map(e->new RevenuePoint(e.getKey(),money(e.getValue().revenue),money(e.getValue().profit),e.getValue().count)).toList();
        BigDecimal total=dataset.orders().stream().map(Order::getTotal).reduce(BigDecimal.ZERO,BigDecimal::add);
        BigDecimal profit=dataset.profitByOrder().values().stream().reduce(BigDecimal.ZERO,BigDecimal::add);
        return new RevenueReport(from,to,money(total),money(profit),dataset.orders().size(),series);
    }
    @Transactional(readOnly=true)
    public List<ProductRow> byProducts(LocalDate from,LocalDate to,String email,String role){return dataset(from,to,email,role).byProduct().values().stream().sorted(Comparator.comparingLong(ProductAccumulator::quantity).reversed().thenComparing(ProductAccumulator::revenue,Comparator.reverseOrder())).map(ProductAccumulator::row).toList();}
    @Transactional(readOnly=true)
    public List<CategoryRow> byCategories(LocalDate from,LocalDate to,String email,String role){
        Map<Long,Product> catalog=products.findAll().stream().collect(Collectors.toMap(Product::getId,p->p));
        Map<Long,CategoryAccumulator> sums=new HashMap<>();
        Dataset data=dataset(from,to,email,role);
        for(Order order:data.orders()) for(OrderItem item:data.linesByOrder().getOrDefault(order.getId(),List.of())){
            Product p=catalog.get(item.getProductId()); if(p==null)continue;
            BigDecimal gross=lineGross(item); BigDecimal share=discountShare(order,item,data.linesByOrder().get(order.getId())); BigDecimal net=gross.subtract(share);
            CategoryAccumulator a=sums.computeIfAbsent(p.getCategory().getId(),id->new CategoryAccumulator(id,p.getCategory().getName()));
            a.quantity+=item.getQuantity();a.revenue=a.revenue.add(net);a.profit=a.profit.add(net.subtract(item.getCostPrice().multiply(BigDecimal.valueOf(item.getQuantity()))));
        }
        return sums.values().stream().sorted(Comparator.comparing(CategoryAccumulator::revenue).reversed()).map(CategoryAccumulator::row).toList();
    }
    @Transactional(readOnly=true)
    public byte[] export(LocalDate from,LocalDate to,String email,String role){
        Dataset data=dataset(from,to,email,role); List<ProductRow> productRows=data.byProduct().values().stream().map(ProductAccumulator::row).sorted(Comparator.comparingLong(ProductRow::quantity).reversed()).toList();
        try(Workbook workbook=new XSSFWorkbook();ByteArrayOutputStream output=new ByteArrayOutputStream()){
            CellStyle header=workbook.createCellStyle();Font font=workbook.createFont();font.setBold(true);header.setFont(font);
            Sheet summary=workbook.createSheet("Doanh thu");
            String[] h={"Từ ngày","Đến ngày","Số đơn hoàn thành","Doanh thu","Lãi gộp"}; Row row=summary.createRow(0);writeHeaders(row,h,header);
            BigDecimal revenue=data.orders().stream().map(Order::getTotal).reduce(BigDecimal.ZERO,BigDecimal::add);BigDecimal profit=data.profitByOrder().values().stream().reduce(BigDecimal.ZERO,BigDecimal::add);
            Row values=summary.createRow(1);values.createCell(0).setCellValue(from.toString());values.createCell(1).setCellValue(to.toString());values.createCell(2).setCellValue(data.orders().size());values.createCell(3).setCellValue(revenue.doubleValue());values.createCell(4).setCellValue(profit.doubleValue());
            Sheet sheet=workbook.createSheet("Theo sản phẩm");Row ph=sheet.createRow(0);writeHeaders(ph,new String[]{"Mã sản phẩm","Sản phẩm","Danh mục","Số lượng","Doanh thu","Lãi gộp"},header);
            int r=1;for(ProductRow p:productRows){Row x=sheet.createRow(r++);x.createCell(0).setCellValue(p.sku());x.createCell(1).setCellValue(p.productName());x.createCell(2).setCellValue(p.categoryName());x.createCell(3).setCellValue(p.quantity());x.createCell(4).setCellValue(p.revenue().doubleValue());x.createCell(5).setCellValue(p.grossProfit().doubleValue());}
            for(int i=0;i<5;i++)summary.autoSizeColumn(i);for(int i=0;i<6;i++)sheet.autoSizeColumn(i);
            workbook.write(output);return output.toByteArray();
        }catch(IOException ex){throw new IllegalStateException("Không thể tạo file báo cáo.",ex);}
    }
    private Dataset dataset(LocalDate from,LocalDate to,String email,String role){
        if(from==null||to==null||from.isAfter(to))throw new IllegalArgumentException("Khoảng thời gian chưa hợp lệ.");
        var start=from.atStartOfDay(ZONE).toInstant();var end=to.plusDays(1).atStartOfDay(ZONE).toInstant();
        List<Order> selected=orders.findByStatusAndCreatedAtBetweenOrderByCreatedAtAsc("HOAN_THANH",start,end);
        if("STAFF_BAN_HANG".equals(role)){User owner=users.findByEmailIgnoreCase(email).orElseThrow();selected=selected.stream().filter(o->owner.getId().equals(o.getSalesOwnerId())).toList();}
        List<Long> ids=selected.stream().map(Order::getId).toList();
        List<OrderItem> allLines=ids.isEmpty()?List.of():items.findByOrderIdIn(ids);
        Map<Long,List<OrderItem>> lines=allLines.stream().collect(Collectors.groupingBy(OrderItem::getOrderId));
        Set<Long> productIds=allLines.stream().map(OrderItem::getProductId).collect(Collectors.toSet());
        Map<Long,Product> catalog=productIds.isEmpty()?Map.of():products.findAllById(productIds).stream().collect(Collectors.toMap(Product::getId,p->p));
        Map<Long,ProductAccumulator> productSums=new HashMap<>();Map<Long,BigDecimal> profitSums=new HashMap<>();
        for(Order order:selected){
            List<OrderItem> orderLines=lines.getOrDefault(order.getId(),List.of());BigDecimal orderProfit=BigDecimal.ZERO;
            for(OrderItem item:orderLines){
                Product p=catalog.get(item.getProductId());if(p==null)continue;
                BigDecimal gross=lineGross(item);BigDecimal net=gross.subtract(discountShare(order,item,orderLines));
                BigDecimal cost=item.getCostPrice().multiply(BigDecimal.valueOf(item.getQuantity()));BigDecimal lineProfit=net.subtract(cost);
                ProductAccumulator a=productSums.computeIfAbsent(item.getProductId(),id->new ProductAccumulator(item.getProductId(),p.getSku(),p.getName(),p.getCategory().getName()));
                a.quantity+=item.getQuantity();a.revenue=a.revenue.add(net);a.profit=a.profit.add(lineProfit);orderProfit=orderProfit.add(lineProfit);
            }
            profitSums.put(order.getId(),money(orderProfit));
        }
        return new Dataset(selected,lines,productSums,profitSums);
    }
    private BigDecimal lineGross(OrderItem item){return item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity()));}
    private BigDecimal discountShare(Order order,OrderItem line,List<OrderItem> all){
        if(order.getDiscount().signum()==0||order.getSubtotal().signum()==0)return BigDecimal.ZERO;
        BigDecimal gross=all.stream().map(this::lineGross).reduce(BigDecimal.ZERO,BigDecimal::add);
        return gross.signum()==0?BigDecimal.ZERO:order.getDiscount().multiply(lineGross(line)).divide(gross,2,RoundingMode.HALF_UP);
    }
    private String periodKey(Order order,String group){
        var date=order.getCreatedAt().atZone(ZONE).toLocalDate();
        return switch(group){case "year"->date.format(DateTimeFormatter.ofPattern("yyyy"));case "month"->date.format(DateTimeFormatter.ofPattern("yyyy-MM"));case "day"->date.toString();default->throw new IllegalArgumentException("Chọn nhóm ngày, tháng hoặc năm.");};
    }
    private static BigDecimal money(BigDecimal value){return value.setScale(2,RoundingMode.HALF_UP);}
    private void writeHeaders(Row row,String[] labels,CellStyle style){for(int i=0;i<labels.length;i++){Cell c=row.createCell(i);c.setCellValue(labels[i]);c.setCellStyle(style);}}
    private static class Accumulator{BigDecimal revenue=BigDecimal.ZERO;BigDecimal profit=BigDecimal.ZERO;long count;}
    private static class CategoryAccumulator{final Long id;final String name;long quantity;BigDecimal revenue=BigDecimal.ZERO;BigDecimal profit=BigDecimal.ZERO;CategoryAccumulator(Long id,String name){this.id=id;this.name=name;}CategoryRow row(){return new CategoryRow(id,name,quantity,money(revenue),money(profit));}BigDecimal revenue(){return revenue;}}
    private static class ProductAccumulator{final Long id;final String sku,name,category;long quantity;BigDecimal revenue=BigDecimal.ZERO;BigDecimal profit=BigDecimal.ZERO;ProductAccumulator(Long id,String sku,String name,String category){this.id=id;this.sku=sku;this.name=name;this.category=category;}ProductRow row(){return new ProductRow(id,sku,name,category,quantity,money(revenue),money(profit));}long quantity(){return quantity;}BigDecimal revenue(){return revenue;}}
    private record Dataset(List<Order> orders,Map<Long,List<OrderItem>> linesByOrder,Map<Long,ProductAccumulator> byProduct,Map<Long,BigDecimal> profitByOrder){}
    public record RevenueReport(LocalDate from,LocalDate to,BigDecimal revenue,BigDecimal grossProfit,long completedOrders,List<RevenuePoint> series){}
    public record RevenuePoint(String period,BigDecimal revenue,BigDecimal grossProfit,long completedOrders){}
    public record ProductRow(Long productId,String sku,String productName,String categoryName,long quantity,BigDecimal revenue,BigDecimal grossProfit){}
    public record CategoryRow(Long categoryId,String categoryName,long quantity,BigDecimal revenue,BigDecimal grossProfit){}
}
