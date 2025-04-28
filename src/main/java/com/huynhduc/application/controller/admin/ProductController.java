package com.huynhduc.application.controller.admin;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.huynhduc.application.elasticsearch.ProductDocumentSearch;
import com.huynhduc.application.entity.*;
import com.huynhduc.application.model.request.CreateProductRequest;
import com.huynhduc.application.model.request.CreateSizeCountRequest;
import com.huynhduc.application.model.request.UpdateFeedBackRequest;
import com.huynhduc.application.repository.CategoryRepository;
//import com.huynhduc.application.repository.ProductSearchRepository;
import com.huynhduc.application.security.CustomUserDetails;
import com.huynhduc.application.service.*;
import com.huynhduc.application.service.impl.ElasticsearchService;
import com.huynhduc.application.service.minio.MinioService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.awt.*;
import java.io.*;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.huynhduc.application.constant.Contant.SIZE_VN;

@Slf4j
@Controller
public class ProductController {

    @Autowired
    private MinioService minioService;
    @Autowired
    private ElasticsearchService elasticsearchService ;
    @Autowired
    private ExcelExportService excelExportService ;

//    @Autowired
//    private ProductSearchRepository productSearchRepository ;
    @Autowired
    private ServletContext context;

    @Autowired
    private ProductService productService;

    @Autowired
    private BrandService brandService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private ImageService imageService;

    @Autowired
    CategoryRepository categoryRepository ;

    @GetMapping("/admin/products")
    public String homePages(Model model,
                            @RequestParam(defaultValue = "", required = false) String id,
                            @RequestParam(defaultValue = "", required = false) String name,
                            @RequestParam(defaultValue = "", required = false) String category,
                            @RequestParam(defaultValue = "", required = false) String brand,
                            @RequestParam(defaultValue = "1", required = false) Integer page) {

        //Lấy danh sách nhãn hiệu
        List<Brand> brands = brandService.getListBrand();
        model.addAttribute("brands", brands);
        //Lấy danh sách danh mục
        List<Category> categories = categoryService.getListCategories();
        model.addAttribute("categories", categories);
        //Lấy danh sách sản phẩm
        Page<Product> products = productService.adminGetListProduct(id, name, category, brand, page);
        model.addAttribute("products", products.getContent());
        model.addAttribute("totalPages", products.getTotalPages());
        model.addAttribute("currentPage", products.getPageable().getPageNumber() + 1);

        return "admin/product/list";
    }
    @GetMapping("/admin/products/create")
    public String getProductCreatePage(Model model) {
        //Lấy danh sách anh của user
        User user = ((CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUser();
        List<String> images = imageService.getListImageOfUser(user.getId());
        model.addAttribute("images", images);

        //Lấy danh sách nhãn hiệu
        List<Brand> brands = brandService.getListBrand();
        model.addAttribute("brands", brands);

        //Lấy danh sách danh mục
        List<Category> categories = categoryService.getListCategories();
        model.addAttribute("categories", categories);

        return "admin/product/create";
    }

    @GetMapping("/admin/products/{slug}/{id}")
    public String getProductUpdatePage(Model model, @PathVariable String id) {

        // Lấy thông tin sản phẩm theo id
        Product product = productService.getProductById(id);
        model.addAttribute("product", product);

        // Lấy danh sách ảnh của user
        User user = ((CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUser();
        List<String> images = imageService.getListImageOfUser(user.getId());
        model.addAttribute("images", images);

        // Lấy danh sách danh mục
        List<Category> categories = categoryService.getListCategories();
        model.addAttribute("categories", categories);

        // Lấy danh sách nhãn hiệu
        List<Brand> brands = brandService.getListBrand();
        model.addAttribute("brands", brands);

        //Lấy danh sách size
        model.addAttribute("sizeVN", SIZE_VN);

        //Lấy size của sản phẩm
        List<ProductSize> productSizes = productService.getListSizeOfProduct(id);
        model.addAttribute("productSizes", productSizes);

        return "admin/product/edit";
    }

    @GetMapping("/api/admin/products")
    public ResponseEntity<Object> getListProducts(@RequestParam(defaultValue = "", required = false) String id,
                                                  @RequestParam(defaultValue = "", required = false) String name,
                                                  @RequestParam(defaultValue = "", required = false) String category,
                                                  @RequestParam(defaultValue = "", required = false) String brand,
                                                  @RequestParam(defaultValue = "1", required = false) Integer page) {
        Page<Product> products = productService.adminGetListProduct(id, name, category, brand, page);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/api/admin/products/{id}")
    public ResponseEntity<Object> getProductDetail(@PathVariable String id) {
        Product rs = productService.getProductById(id);
        return ResponseEntity.ok(rs);
    }

    @PostMapping(value = "/api/admin/products")
    public ResponseEntity<Object> createProduct(@Valid @RequestPart("product") String productJson,
                                                @RequestPart("images") MultipartFile[] images ) throws JsonProcessingException {
        // Parse JSON
        ObjectMapper objectMapper = new ObjectMapper();
        CreateProductRequest productRequest = objectMapper.readValue(productJson, CreateProductRequest.class);
        ArrayList<MultipartFile> arrayListFile = new ArrayList(Arrays.asList(images));
        productRequest.setImages(arrayListFile);
        Product product = productService.createProduct(productRequest);
        // luu vao elactic search
        ProductDocumentSearch productDocumentSearch = new ProductDocumentSearch();
        productDocumentSearch.setId(product.getId());
        productDocumentSearch.setName(product.getName());
        productDocumentSearch.setDescription(product.getDescription());
        productDocumentSearch.setBrandName(this.brandService.getBrandById(productRequest.getBrandId()).getName());
        productDocumentSearch.setSlug(product.getSlug());
        productDocumentSearch.setViews(10);
        productDocumentSearch.setTotalSold((int) product.getTotalSold());
        List<String> nameCategory = new ArrayList<>();
        List<Integer> idCategory = new ArrayList<>();
        for(var idCate : productRequest.getCategoryIds()){
            Category category =  this.categoryRepository.findById(Integer.toUnsignedLong(idCate)).get();
            nameCategory.add(category.getName());
        }
        productDocumentSearch.setCategoryNames(nameCategory);
        productDocumentSearch.setPrice(product.getPrice());
        productDocumentSearch.setSalePrice(product.getSalePrice());
        productDocumentSearch.setStatus(product.getStatus());
        // luu images trong els
        ArrayList<String> imageUrls = new ArrayList<>();
        for(MultipartFile file : arrayListFile){
            String url = this.minioService.uploadFile(file);
            imageUrls.add(url);
        }
        productDocumentSearch.setImages(imageUrls);
        elasticsearchService.indexProduct(productDocumentSearch);
        return ResponseEntity.ok(product);
}

    @PatchMapping("/api/admin/products/{id}")
    public ResponseEntity<Object> updateProduct(@Valid @RequestBody CreateProductRequest createProductRequest, @PathVariable String id) {
        productService.updateProduct(createProductRequest, id);
        // cap nhat trong els
        return ResponseEntity.ok("Sửa sản phẩm thành công!");
    }
    @DeleteMapping("/api/admin/products")
    public ResponseEntity<Object> deleteProduct(@RequestBody String[] ids) {
        productService.deleteProduct(ids);
        return ResponseEntity.ok("Xóa sản phẩm thành công!");
    }

    @DeleteMapping("/api/admin/products/{id}")
    public ResponseEntity<Object> deleteProductById(@PathVariable String id) {
        productService.deleteProductById(id);
        return ResponseEntity.ok("Xóa sản phẩm thành công!");
    }

    @PatchMapping("/api/admin/products/sizes")
    public ResponseEntity<?> updateSizeCount(@Valid @RequestBody CreateSizeCountRequest createSizeCountRequest) {
        productService.createSizeCount(createSizeCountRequest);

        return ResponseEntity.ok("Cập nhật thành công!");
    }

    @PatchMapping("/api/admin/products/{id}/update-feedback-image")
    public ResponseEntity<?> updatefeedBackImages(@PathVariable String id, @Valid @RequestBody UpdateFeedBackRequest req) {
        productService.updatefeedBackImages(id, req);

        return ResponseEntity.ok("Cập nhật thành công");
    }
    @GetMapping("/api/products/export/excel")
    public void exportProductDataToExcelFile(HttpServletResponse response) throws IOException {
        List<Product> result = productService.getAllProduct();

        if (result == null || result.isEmpty()) {
            response.setContentType("text/plain;charset=UTF-8");
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("Danh sách sản phẩm rỗng!");
            return;
        }

        // Cấu hình response trước
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        String fileName = "DanhSachSanPham_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".xlsx";
        response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");

        // Sau đó mới gọi service để ghi file
        excelExportService.exportProductItems(result, response);
    }

}
