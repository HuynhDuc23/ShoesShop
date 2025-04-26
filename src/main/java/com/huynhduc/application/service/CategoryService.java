package com.huynhduc.application.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.huynhduc.application.entity.Category;
import com.huynhduc.application.model.dto.CategoryDTO;
import com.huynhduc.application.model.request.CreateCategoryRequest;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public interface CategoryService {
    List<Category> getListCategories();

    Category getCategoryById(long id);

    Category createCategory(CreateCategoryRequest createCategoryRequest);

    void updateCategory(CreateCategoryRequest createCategoryRequest, long id);

    void deleteCategory(long id);

    Page<Category> adminGetListCategory(String id, String name, String status, int page) throws JsonProcessingException;

    void updateOrderCategory(int[] ids);

    //Đếm số danh mục
    long getCountCategories();

    List<Category> findAllByIds(ArrayList<Long> categoryIds);
}
