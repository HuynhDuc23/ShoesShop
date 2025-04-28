package com.huynhduc.application.elasticsearch;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.elasticsearch.annotations.Document;

import java.util.List;
@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(indexName = "products_search")
public class ProductDocumentSearch {
    private String id;
    private String name;
    private String description;
    private String brandName; // 🔥 thêm brandName
    private List<String> categoryNames; // 🔥 thêm categoryNames
    private Long price;
    private Long salePrice;
    private List<String> images;
    private int status;
    private String slug;
    private int totalSold;
    private int views;
}
