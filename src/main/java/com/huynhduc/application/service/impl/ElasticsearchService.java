package com.huynhduc.application.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.huynhduc.application.elasticsearch.ProductDocumentSearch;
import lombok.RequiredArgsConstructor;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ElasticsearchService {

    private final RestHighLevelClient client;
    private final ObjectMapper objectMapper;
    private static final String INDEX = "products_search";
    public void indexProduct(ProductDocumentSearch product) {
        try {
            IndexRequest request = new IndexRequest(INDEX)
                    .id(product.getId())
                    .source(objectMapper.writeValueAsString(product), XContentType.JSON);
            client.index(request, RequestOptions.DEFAULT);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void updateProduct(ProductDocumentSearch product) {
        indexProduct(product);
    }
    public void deleteProduct(String id) {
        try {
            DeleteRequest request = new DeleteRequest(INDEX, id);
            client.delete(request, RequestOptions.DEFAULT);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<ProductDocumentSearch> searchProducts(String keyword) {
        List<ProductDocumentSearch> products = new ArrayList<>();
        try {
            SearchRequest searchRequest = new SearchRequest(INDEX);
            SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();

            // 🔥 Tìm theo nhiều trường
            sourceBuilder.query(QueryBuilders.multiMatchQuery(keyword, "name", "brandName", "categoryNames"));

            searchRequest.source(sourceBuilder);
            SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);

            for (SearchHit hit : searchResponse.getHits().getHits()) {
                products.add(objectMapper.readValue(hit.getSourceAsString(), ProductDocumentSearch.class));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return products;
    }
}
