package com.huynhduc.application.repository;

import com.huynhduc.application.elasticsearch.ProductDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductSearchRepository  extends ElasticsearchRepository<ProductDocument,String> {
    Page<ProductDocument> findByNameContainingIgnoreCase(String name, Pageable pageable);
}
