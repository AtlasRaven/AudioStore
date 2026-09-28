package vn.audiostore.config;

import vn.audiostore.catalog.*;
import vn.audiostore.inventory.Inventory;
import vn.audiostore.inventory.InventoryRepository;
import java.math.BigDecimal;
import java.util.Map;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SeedCatalog {
    @Bean CommandLineRunner seedInitialCatalog(CategoryRepository categories, BrandRepository brands, ProductRepository products, InventoryRepository inventory) {
        return args -> {
            for (String name : new String[]{"Loa", "Tai nghe", "Ampli", "Micro", "DAC"}) {
                if (categories.findAll().stream().noneMatch(c -> c.getName().equals(name))) categories.save(new Category(name, slug(name)));
            }
            for (String name : new String[]{"Audio-Technica", "JBL", "Sony", "Yamaha", "FiiO"}) {
                if (brands.findAll().stream().noneMatch(b -> b.getName().equals(name))) brands.save(new Brand(name, slug(name)));
            }
            if (products.count() > 0) return;
            Map<String,Category> cat = categories.findAll().stream().collect(java.util.stream.Collectors.toMap(Category::getName, c -> c));
            Map<String,Brand> brand = brands.findAll().stream().collect(java.util.stream.Collectors.toMap(Brand::getName, b -> b));
            String[][] rows = {
                {"SP001","JBL Flip 6","Loa","JBL","Loa Bluetooth chống nước, hợp mang theo khi đi chơi.","Bluetooth 5.1; Pin 12 giờ; Chống nước IP67","2890000","1900000"},
                {"SP002","JBL Charge 5","Loa","JBL","Loa di động có pin lớn và cổng sạc USB.","Bluetooth 5.1; Pin 20 giờ; IP67","3990000","2700000"},
                {"SP003","Yamaha HS5","Loa","Yamaha","Loa kiểm âm gần trường, phù hợp bàn làm việc và phòng thu nhỏ.","Công suất 70W; 54Hz–30kHz; XLR/TRS","5990000","4200000"},
                {"SP004","Yamaha HS8","Loa","Yamaha","Loa kiểm âm 8 inch cho phòng thu cần dải trầm sâu hơn.","Công suất 120W; 38Hz–30kHz; XLR/TRS","10900000","7900000"},
                {"SP005","Sony SRS-XB100","Loa","Sony","Loa Bluetooth nhỏ gọn, có dây đeo và khả năng chống nước.","Bluetooth; Pin 16 giờ; IP67","1290000","800000"},
                {"SP006","Audio-Technica ATH-M20x","Tai nghe","Audio-Technica","Tai nghe kiểm âm kín, dây liền phù hợp nghe và thu âm cơ bản.","Trở kháng 47 Ohm; 15Hz–20kHz; Dây 3m","1590000","950000"},
                {"SP007","Audio-Technica ATH-M50x","Tai nghe","Audio-Technica","Tai nghe kiểm âm gập được, đi kèm ba dây tháo rời.","Trở kháng 38 Ohm; 15Hz–28kHz; Driver 45mm","4290000","2950000"},
                {"SP008","Sony WH-1000XM5","Tai nghe","Sony","Tai nghe không dây có chống ồn chủ động và micro đàm thoại.","Bluetooth; Chống ồn; Pin đến 30 giờ","7990000","5600000"},
                {"SP009","Sony WF-1000XM5","Tai nghe","Sony","Tai nghe true wireless chống ồn, hộp sạc nhỏ gọn.","Bluetooth; Chống ồn; IPX4","5990000","4100000"},
                {"SP010","FiiO JT1","Tai nghe","FiiO","Tai nghe chụp tai dễ kéo, phù hợp nghe nhạc tại nhà.","Trở kháng 32 Ohm; 15Hz–30kHz; Dây tháo rời","1890000","1200000"},
                {"SP011","Yamaha A-S301","Ampli","Yamaha","Ampli tích hợp stereo với ngõ vào digital và phono.","60W/kênh; Optical; Coaxial; Phono MM","10900000","7900000"},
                {"SP012","Yamaha A-S501","Ampli","Yamaha","Ampli stereo công suất cao hơn, có ngõ vào optical và coaxial.","85W/kênh; Optical; Coaxial; Phono MM","15900000","11700000"},
                {"SP013","FiiO K11","DAC","FiiO","DAC kiêm ampli tai nghe để bàn, có màn hình hiển thị.","USB; Optical; Coaxial; 384kHz/DSD256","2990000","1900000"},
                {"SP014","FiiO K7","DAC","FiiO","DAC và ampli tai nghe cân bằng cho hệ thống nghe tại bàn.","USB; Optical; Coaxial; 32bit/384kHz","5990000","4200000"},
                {"SP015","FiiO BTR15","DAC","FiiO","DAC Bluetooth bỏ túi, hỗ trợ kết nối có dây và không dây.","Bluetooth 5.1; USB DAC; 3.5mm và 4.4mm","2990000","1950000"},
                {"SP016","Audio-Technica AT2020","Micro","Audio-Technica","Micro condenser thu âm giọng nói và nhạc cụ trong phòng yên tĩnh.","XLR; Condenser; 20Hz–20kHz","2790000","1850000"},
                {"SP017","Audio-Technica AT2040","Micro","Audio-Technica","Micro dynamic hướng tính, phù hợp podcast và phát thanh.","XLR; Dynamic; Hypercardioid","3290000","2200000"},
                {"SP018","Yamaha AG03MK2","Ampli","Yamaha","Mixer USB gọn cho livestream, podcast và thu âm tại nhà.","USB-C; 2 kênh; Loopback","4590000","3150000"},
                {"SP019","JBL Tune 720BT","Tai nghe","JBL","Tai nghe Bluetooth chụp tai, pin dùng dài ngày.","Bluetooth 5.3; Pin đến 76 giờ; Gập gọn","1590000","980000"},
                {"SP020","Sony ULT Field 1","Loa","Sony","Loa Bluetooth di động có dây đeo và chế độ tăng bass.","Bluetooth; Pin 12 giờ; IP67","2790000","1850000"}
            };
            for (String[] x : rows) {
                Product product=products.save(new Product(x[0],x[1],slug(x[1]),cat.get(x[2]),brand.get(x[3]),x[4],x[5],new BigDecimal(x[6]),new BigDecimal(x[7]),null,12));
                inventory.save(new Inventory(product.getId(),8,3));
            }
        };
    }
    private static String slug(String value) {
        return java.text.Normalizer.normalize(value.toLowerCase(java.util.Locale.ROOT), java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "").replace('đ','d').replaceAll("[^a-z0-9]+", "-").replaceAll("^-|-$", "");
    }
}
