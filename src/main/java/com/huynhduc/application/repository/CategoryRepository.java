package com.huynhduc.application.repository;

import com.huynhduc.application.entity.Category;
import com.huynhduc.application.model.dto.ChartDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    Category findByName(String name);

    @Query(value = "SELECT count(category_id) FROM product_category WHERE category_id = ?1", nativeQuery = true)
    int checkProductInCategory(long id);

    @Query(value = "SELECT * FROM category c " +
            "WHERE c.id LIKE CONCAT('%',?1,'%') " +
            "AND c.name LIKE CONCAT('%',?2,'%') " +
            "AND c.status LIKE CONCAT('%',?3,'%')", nativeQuery = true)
    Page<Category> adminGetListCategory(String id, String name, String status, Pageable pageable);

    @Query(name = "getProductOrderCategories",nativeQuery = true)
    List<ChartDTO> getListProductOrderCategories();

    //    List<Category> findByProducts_Name(String name);
    @Query(value = "select * from category limit 5", nativeQuery = true)
    List<Category> findCategory();

    // tìm tất cả category theo danh sách id
    List<Category> findAllByIdIn(List<Long> ids);
}
