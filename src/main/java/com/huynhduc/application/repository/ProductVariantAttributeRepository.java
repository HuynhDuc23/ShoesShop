package com.huynhduc.application.repository;

import com.huynhduc.application.entity.ProductVariantAttribute;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductVariantAttributeRepository extends JpaRepository<ProductVariantAttribute,String> {
    @Query(value = "SELECT pva.value \n" +
            "FROM product p  \n" +
            "INNER JOIN product_variants pr ON p.id = pr.product_id  \n" +
            "INNER JOIN product_variant_attributes pva ON pr.id = pva.variant_id  \n" +
            "INNER JOIN product_attributes prat ON pva.attribute_id = prat.id  \n" +
            "WHERE p.id =:productId and prat.name = 'Size';",nativeQuery = true)
    List<String> findValuesByProductIdAndAttributeName(@Param("productId") String productId);

}
