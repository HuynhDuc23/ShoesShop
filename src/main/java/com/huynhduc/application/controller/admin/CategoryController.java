package com.huynhduc.application.controller.admin;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.huynhduc.application.entity.Category;
import com.huynhduc.application.model.mapper.CategoryMapper;
import com.huynhduc.application.model.request.CreateCategoryRequest;
import com.huynhduc.application.repository.CategoryRepository;
import com.huynhduc.application.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.sql.Timestamp;

@Controller
public class CategoryController {
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private CategoryService categoryService;

    @GetMapping("/admin/categories")
    public String homePage(Model model,
                           @RequestParam(defaultValue = "",required = false) String id,
                           @RequestParam(defaultValue = "",required = false) String name,
                           @RequestParam(defaultValue = "",required = false) String status,
                           @RequestParam(defaultValue = "1",required = false) Integer page) throws JsonProcessingException {

        Page<Category> categories = categoryService.adminGetListCategory(id,name,status,page);
        model.addAttribute("categories",categories.getContent());
        model.addAttribute("totalPages",categories.getTotalPages());
        model.addAttribute("currentPage", categories.getPageable().getPageNumber() + 1);

        return "admin/category/list";
    }

    // cache
    @GetMapping("/api/admin/categories")
    public ResponseEntity<?> adminGetListCategories(@RequestParam(defaultValue = "",required = false) String id,
                                                         @RequestParam(defaultValue = "",required = false) String name,
                                                         @RequestParam(defaultValue = "",required = false) String status,
                                                         @RequestParam(defaultValue = "0",required = false) Integer page) throws JsonProcessingException {
        Page<Category> categories = categoryService.adminGetListCategory(id,name,status,page);
        return ResponseEntity.ok(categories);

    }
    @GetMapping("/api/admin/categories/{id}")
    public ResponseEntity<Object> getCategoryById(@PathVariable long id) {
        Category category = categoryService.getCategoryById(id);
        return ResponseEntity.ok(CategoryMapper.toCategoryDTO(category));
    }

    @PostMapping("/api/admin/categories")
    public ResponseEntity<Object> createCategory(@Valid @RequestBody CreateCategoryRequest createCategoryRequest) {
        Category category = categoryService.createCategory(createCategoryRequest);
        return ResponseEntity.ok(CategoryMapper.toCategoryDTO(category));
    }

    @PutMapping("/api/admin/categories/{id}")
    public ResponseEntity<Object> updateCategory(@Valid @RequestBody CreateCategoryRequest createCategoryRequest, @PathVariable long id) {
        categoryService.updateCategory(createCategoryRequest, id);
        return ResponseEntity.ok("Sửa danh mục thành công!");
    }

    @DeleteMapping("/api/admin/categories/{id}")
    public ResponseEntity<Object> deleteCategory(@PathVariable long id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.ok("Xóa danh mục thành công!");
    }

    @PutMapping("/api/admin/categories")
    public ResponseEntity<Object> updateOrderCategory(@RequestBody int[] ids){
        categoryService.updateOrderCategory(ids);
        return ResponseEntity.ok("Thay đổi thứ tự thành công!");
    }
    @GetMapping("/fake/category")
    public ResponseEntity<?> fakeCategory(){
        for (long i = 1; i <= 10000000; i++) {
            createFakeCategory(i);
        }
        return ResponseEntity.status(HttpStatus.OK).body("ok");
    }
    private void createFakeCategory(long i) {
        Category category = new Category();
        category.setName("Category " + i);
        category.setSlug("category-" + i);
        category.setOrder((int) i);
        category.setStatus(true);
        category.setCreatedAt(new Timestamp(System.currentTimeMillis()));
        category.setModifiedAt(new Timestamp(System.currentTimeMillis()));

        categoryRepository.save(category);
    }
}
