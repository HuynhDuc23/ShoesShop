package com.huynhduc.application.service.impl;

import com.huynhduc.application.entity.Category;
import com.huynhduc.application.exception.BadRequestException;
import com.huynhduc.application.exception.InternalServerException;
import com.huynhduc.application.exception.NotFoundException;
import com.huynhduc.application.model.mapper.CategoryMapper;
import com.huynhduc.application.model.request.CreateCategoryRequest;
import com.huynhduc.application.repository.CategoryRepository;
import com.huynhduc.application.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.util.*;

import static com.huynhduc.application.constant.Contant.LIMIT_CATEGORY;

@Component
public class CategoryServiceImpl implements CategoryService {
    @Autowired
    private CategoryRepository categoryRepository;

    @Override
    public Page<Category> adminGetListCategory(String id, String name, String status, int page) {
        page = Math.max(page - 1, 0);
        Pageable pageable = PageRequest.of(page, LIMIT_CATEGORY, Sort.by("created_at").descending());
        return categoryRepository.adminGetListCategory(id, name, status, pageable);
    }

    @Override
    @CacheEvict(value = "categoryList", allEntries = true)
    public void updateOrderCategory(int[] ids) {
        for (int id : ids) {
            Optional<Category> category = categoryRepository.findById((long) id);
            if (category.isPresent()) {
                category.get().setOrder(0);
                categoryRepository.save(category.get());
            }
        }
    }

    @Override
    @Cacheable(value = "categoryList", key = "'all_categories_count'")
    public long getCountCategories() {
        return categoryRepository.count();
    }

    @Override
    public List<Category> findAllByIds(ArrayList<Long> categoryIds) {
        return this.categoryRepository.findAllByIdIn(categoryIds);
    }

    @Override
    @Cacheable(value = "categoryList", key = "'all_categories_list'")
    public List<Category> getListCategories() {
        return categoryRepository.findCategory();
    }

    @Override
    @Cacheable(value = "categoryList", key = "'all_categories_id'")
    public Category getCategoryById(long id) {
        Optional<Category> category = categoryRepository.findById(id);
        if (category.isEmpty()) {
            throw new NotFoundException("Danh mục không tồn tại!");
        }
        return category.get();
    }

    @Override
    @CacheEvict(value = "categoryList", allEntries = true)
    public Category createCategory(CreateCategoryRequest createCategoryRequest) {
        Category category = categoryRepository.findByName(createCategoryRequest.getName());
        if (category != null) {
            throw new BadRequestException("Tên danh mục đã tồn tại trong hệ thống. Vui lòng chọn tên khác!");
        }
        category = CategoryMapper.toCategory(createCategoryRequest);
        categoryRepository.save(category);
        return category;
    }

    @Override
    @CacheEvict(value = "categoryList", allEntries = true)
    public void updateCategory(CreateCategoryRequest createCategoryRequest, long id) {
        Optional<Category> result = categoryRepository.findById(id);
        if (result.isEmpty()) {
            throw new NotFoundException("Danh mục không tồn tại!");
        }
        Category category = result.get();
        category.setName(createCategoryRequest.getName());
        category.setStatus(createCategoryRequest.isStatus());
        category.setModifiedAt(new Timestamp(System.currentTimeMillis()));
        try {
            categoryRepository.save(category);
        } catch (Exception e) {
            throw new InternalServerException("Lỗi khi chỉnh sửa danh mục");
        }
    }

    @Override
    @CacheEvict(value = "categoryList", allEntries = true)
    public void deleteCategory(long id) {
        Optional<Category> result = categoryRepository.findById(id);
        if (result.isEmpty()) {
            throw new NotFoundException("Danh mục không tồn tại!");
        }

        // Kiểm tra sản phẩm trong danh mục
        long count = categoryRepository.checkProductInCategory(id);
        if (count > 0) {
            throw new BadRequestException("Có sản phẩm thuộc danh mục không thể xóa!");
        }

        try {
            categoryRepository.deleteById(id);
        } catch (Exception ex) {
            throw new InternalServerException("Lỗi khi xóa danh mục!");
        }
    }
}
