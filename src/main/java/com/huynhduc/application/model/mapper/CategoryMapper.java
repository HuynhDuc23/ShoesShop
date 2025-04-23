package com.huynhduc.application.model.mapper;

import com.github.slugify.Slugify;
import com.huynhduc.application.entity.Category;
import com.huynhduc.application.model.dto.CategoryDTO;
import com.huynhduc.application.model.request.CreateCategoryRequest;

import java.sql.Timestamp;

public class CategoryMapper {
    public static CategoryDTO toCategoryDTO(Category category){
        CategoryDTO categoryDTO = new CategoryDTO();
        categoryDTO.setId(category.getId());
        categoryDTO.setName(category.getName());
        categoryDTO.setOrder(category.getOrder());
        categoryDTO.setStatus(category.isStatus());
        categoryDTO.setCreatedAt(category.getCreatedAt());
        categoryDTO.setUpdatedAt(category.getModifiedAt());
        return categoryDTO;
    }

    public static Category toCategory(CreateCategoryRequest createCategoryRequest){
        Category category = new Category();
        category.setName(createCategoryRequest.getName());
        category.setOrder(0);
        category.setStatus(createCategoryRequest.isStatus());
        category.setCreatedAt(new Timestamp(System.currentTimeMillis()));
        Slugify slug = new Slugify();
        category.setSlug(slug.slugify(createCategoryRequest.getName()));
        category.setModifiedAt(new Timestamp(System.currentTimeMillis()));
        return category;
    }
}
