package com.huynhduc.application.controller.admin;

import com.github.javafaker.Faker;
import com.huynhduc.application.entity.Brand;
import com.huynhduc.application.entity.User;
import com.huynhduc.application.model.mapper.BrandMapper;
import com.huynhduc.application.model.request.CreateBrandRequest;
import com.huynhduc.application.repository.BrandRepository;
import com.huynhduc.application.security.CustomUserDetails;
import com.huynhduc.application.service.BrandService;
import com.huynhduc.application.service.ImageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.sql.Timestamp;
import java.util.List;

@Controller
public class BrandController {
    @Autowired
    private BrandRepository brandRepository ;

    @Autowired
    private BrandService brandService;

    @Autowired
    private ImageService imageService;

    @GetMapping("/admin/brands")
    public String homePage(Model model,
                           @RequestParam(defaultValue = "", required = false) String id,
                           @RequestParam(defaultValue = "", required = false) String name,
                           @RequestParam(defaultValue = "", required = false) String status,
                           @RequestParam(defaultValue = "1", required = false) Integer page) {

        //Lấy tất cả các anh của user upload
        User user = ((CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUser();
        List<String> images = imageService.getListImageOfUser(user.getId());
        model.addAttribute("images", images);

        Page<Brand> brands = brandService.adminGetListBrands(id, name, status, page);
        model.addAttribute("brands", brands.getContent());
        model.addAttribute("totalPages", brands.getTotalPages());
        model.addAttribute("currentPage", brands.getPageable().getPageNumber() + 1);
        return "admin/brand/list";
    }

    @PostMapping("/api/admin/brands")
    public ResponseEntity<Object> createBrand(@Valid @RequestBody CreateBrandRequest createBrandRequest) {
        Brand brand = brandService.createBrand(createBrandRequest);
        return ResponseEntity.ok(BrandMapper.toBrandDTO(brand));
    }

    @PutMapping("/api/admin/brands/{id}")
    public ResponseEntity<Object> updateBrand(@Valid @RequestBody CreateBrandRequest createBrandRequest, @PathVariable long id) {
        brandService.updateBrand(createBrandRequest, id);
        return ResponseEntity.ok("Sửa nhãn hiệu thành công!");
    }

    @DeleteMapping("/api/admin/brands/{id}")
    public ResponseEntity<Object> deleteBrand(@PathVariable long id) {
        brandService.deleteBrand(id);
        return ResponseEntity.ok("Xóa nhãn hiệu thành công!");
    }
    @GetMapping("/api/admin/brands/{id}")
    public ResponseEntity<Object> getBrandById(@PathVariable long id){
        Brand brand = brandService.getBrandById(id);
        return ResponseEntity.ok(brand);
    }
    @GetMapping("/fake/brand")
    public ResponseEntity<?>fakeBrand() {
        for(int i = 0 ; i < 100000 ;i++){
            generateFakeBrand();
        }
        return ResponseEntity.status(HttpStatus.OK).body("ok");
    }
    public Brand generateFakeBrand() {
        Faker faker = new Faker();
        Brand brand = new Brand();
        brand.setName(faker.company().name() + " " + faker.number().randomNumber()); // tránh trùng
        brand.setDescription(faker.lorem().sentence(10));
        brand.setThumbnail("http://localhost:9000/resources/giay04.jpg");
        brand.setStatus(true);
        brand.setCreatedAt(new Timestamp(System.currentTimeMillis()));
        brand.setModifiedAt(new Timestamp(System.currentTimeMillis()));
        this.brandRepository.save(brand);
        return brand;
    }


}
