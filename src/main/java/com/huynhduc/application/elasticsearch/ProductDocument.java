//package com.huynhduc.application.elasticsearch;
//
//import com.huynhduc.application.entity.Product;
//import lombok.*;
//import org.springframework.data.elasticsearch.annotations.Document;
//import org.springframework.data.elasticsearch.annotations.Field;
//import org.springframework.data.elasticsearch.annotations.FieldType;
//
//import javax.persistence.Id;
//import java.util.ArrayList;
//import java.util.List;
//
//@Document(indexName = "products")
//@AllArgsConstructor
//@NoArgsConstructor
//@Getter
//@Setter
//@Builder
//public class ProductDocument {
//    @Id
//    private String id;
//    private String name;
//    private String slug;
//    private long price;
//    private int views;
//    private String images;
//    private int totalSold;
//    private long promotionPrice;
//
//    public ProductDocument(String id, String name, String slug, long price, int view, ArrayList<String> images, long totalSold) {
//    }
//
//    public ProductDocument(String id, int totalSold, String images, int views, long price, String slug, String name, long promotionPrice) {
//        this.id = id;
//        this.totalSold = totalSold;
//        this.images = images;
//        this.views = views;
//        this.price = price;
//        this.slug = slug;
//        this.name = name;
//        this.promotionPrice = promotionPrice;
//    }
//
//    public static ProductDocument from(Product product) {
//        return new ProductDocument(
//                product.getId(),
//                product.getName(),
//                product.getSlug(),
//                product.getPrice(),
//                product.getView(),
//                product.getImages(),
//                product.getTotalSold()
//        );
//    }
//}