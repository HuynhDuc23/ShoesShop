package com.huynhduc.application.service.impl;

import com.huynhduc.application.elasticsearch.ProductDocument;
import com.huynhduc.application.entity.*;
import com.huynhduc.application.exception.BadRequestException;
import com.huynhduc.application.exception.InternalServerException;
import com.huynhduc.application.exception.NotFoundException;
import com.huynhduc.application.model.dto.DetailProductInfoDTO;
import com.huynhduc.application.model.dto.PageableDTO;
import com.huynhduc.application.model.dto.ProductInfoDTO;
import com.huynhduc.application.model.dto.ShortProductInfoDTO;
import com.huynhduc.application.model.mapper.ProductMapper;
import com.huynhduc.application.model.request.*;
import com.huynhduc.application.repository.*;
import com.huynhduc.application.service.ProductService;
import com.huynhduc.application.service.PromotionService;
import com.huynhduc.application.service.minio.MinioService;
import com.huynhduc.application.utils.PageUtil;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.web.multipart.MultipartFile;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.huynhduc.application.constant.Contant.*;

@Component
public class ProductServiceImpl implements ProductService {
    @Autowired
    private MinioService minioService ;

    @Autowired
    private ProductSearchRepository productSearchRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductSizeRepository productSizeRepository;

    @Autowired
    private PromotionService promotionService;

    @Autowired
    private PromotionRepository promotionRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductVariantRepository productVariantRepository;

    @Autowired
    private ProductVariantAttributeRepository productVariantAttributeRepository;

    @Autowired
    private ProductAttributeRepository productAttributeRepository;
    @Autowired
    private CategoryRepository categoryRepository;

    @Override
    public Page<Product> adminGetListProduct(String id, String name, String category, String brand, Integer page) {
        page--;
        if (page < 0) {
            page = 0;
        }
        Pageable pageable = PageRequest.of(page, LIMIT_PRODUCT, Sort.by("created_at").descending());
        return productRepository.adminGetListProducts(id, name, category, brand, pageable);
    }

    @Override
    public Product createProduct(CreateProductRequest createProductRequest) {
        // Kiểm tra có danh mục
        if (createProductRequest.getCategoryIds().isEmpty()) {
            throw new BadRequestException("Danh mục trống!");
        }

        // Kiểm tra có ảnh sản phẩm
        if (createProductRequest.getImages().isEmpty()) {
            throw new BadRequestException("Ảnh sản phẩm trống!");
        }
        // Làm sạch tên sản phẩm: loại bỏ tất cả khoảng trắng
        String rawName = createProductRequest.getName();
        String cleanedName = rawName.replaceAll("\\s+", ""); // hoặc dùng .trim() nếu chỉ cần xóa đầu/cuối

        // Kiểm tra tên sản phẩm trùng (dùng tên đã làm sạch)
        Product product = productRepository.findByName(cleanedName);
        if (product != null) {
            throw new BadRequestException("Tên sản phẩm đã tồn tại trong hệ thống, Vui lòng chọn tên khác!");
        }
        // Gán lại tên đã xử lý vào request
        createProductRequest.setName(cleanedName);
        ArrayList<String> imageUrls = new ArrayList<>();
        for(MultipartFile file : createProductRequest.getImages()){
            String url = this.minioService.uploadFile(file);
            System.out.println(url + "data");
            imageUrls.add(url);
        }
        // Chuyển DTO sang Entity
        product = ProductMapper.toProduct(createProductRequest);
        product.setImages(imageUrls);
        // Sinh ID
        String id = RandomStringUtils.randomAlphanumeric(6);
        product.setId(id);
        product.setTotalSold(0);
        product.setCreatedAt(new Timestamp(System.currentTimeMillis()));
        product.setModifiedAt(new Timestamp(System.currentTimeMillis()));
        try {
            productRepository.save(product);
            productSearchRepository.deleteAll();
        } catch (Exception ex) {
            throw new InternalServerException("Lỗi khi thêm sản phẩm");
        }

        return product;
    }


    @Override
    public void updateProduct(CreateProductRequest createProductRequest, String id) {
        //Kiểm tra sản phẩm có tồn tại
        Optional<Product> product = productRepository.findById(id);
        if (product.isEmpty()) {
            throw new NotFoundException("Không tìm thấy sản phẩm!");
        }
        //Kiểm tra tên sản phẩm có tồn tại
        Product rs = productRepository.findByName(createProductRequest.getName());
        if (rs != null) {
            if (!createProductRequest.getId().equals(rs.getId()))
                throw new BadRequestException("Tên sản phẩm đã tồn tại trong hệ thống, Vui lòng chọn tên khác!");
        }
        //Kiểm tra có danh muc
        if (createProductRequest.getCategoryIds().isEmpty()) {
            throw new BadRequestException("Danh mục trống!");
        }
        Product result = ProductMapper.toProduct(createProductRequest);
        result.setId(id);
        result.setModifiedAt(new Timestamp(System.currentTimeMillis()));
        ArrayList<String> data = new ArrayList<>();
        Optional<Product> files = this.productRepository.findById(createProductRequest.getId());
        for(var file : files.get().getImages()){
           data.add(file);
        }
        result.setImages(data);
        try {
            productRepository.save(result);
        } catch (Exception e) {
            throw new InternalServerException("Có lỗi khi sửa sản phẩm!");
        }
        // ✅ Đồng bộ với Elasticsearch
        try {
            ProductDocument doc = new ProductDocument(
                    result.getId(),
                    result.getName(),
                    result.getSlug(),
                    result.getPrice(),
                    result.getView(),
                    result.getImages(),
                    result.getTotalSold()
            );

            productSearchRepository.save(doc); // Lưu lại vào Elasticsearch
        } catch (Exception e) {
            System.err.println("Lỗi khi đồng bộ dữ liệu với Elasticsearch: " + e.getMessage());
        }
    }

    @Override
    public Product getProductById(String id) {
        Optional<Product> product = productRepository.findById(id);
        if (product.isEmpty()) {
            throw new NotFoundException("Không tìm thấy sản phẩm trong hệ thống!");
        }
        return product.get();
    }

    @Override
    public void deleteProduct(String[] ids) {
        for (String id : ids) {
            productSearchRepository.deleteById(id);
            productRepository.deleteById(id);
        }
    }

    @Override
    public void deleteProductById(String id) {
        // Check product exist
        Optional<Product> rs = productRepository.findById(id);
        if (rs.isEmpty()) {
            throw new NotFoundException("Sản phẩm không tồn tại");
        }

        // If have order, can't delete
        int countOrder = orderRepository.countByProductId(id);
        if (countOrder > 0) {
            throw new BadRequestException("Sản phẩm đã được đặt hàng không thể xóa");
        }

        try {
            // Delete product size
            productSizeRepository.deleteByProductId(id);
            productRepository.deleteById(id);
            // ✅ Xóa trong Elasticsearch
            productSearchRepository.deleteById(id);
        } catch (Exception ex) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            throw new InternalServerException("Lỗi khi xóa sản phẩm");
        }
    }

    @Override
    public List<ProductInfoDTO> getListBestSellProducts() {
        List<ProductInfoDTO> productInfoDTOS = productRepository.getListBestSellProducts(LIMIT_PRODUCT_SELL);
        return checkPublicPromotion(productInfoDTOS);
    }

    @Override
    public List<ProductInfoDTO> getListNewProducts() {
        List<ProductInfoDTO> productInfoDTOS = productRepository.getListNewProducts(LIMIT_PRODUCT_NEW);
        return checkPublicPromotion(productInfoDTOS);

    }

    @Override
    public List<ProductInfoDTO> getListViewProducts() {
        List<ProductInfoDTO> productInfoDTOS = productRepository.getListViewProducts(LIMIT_PRODUCT_VIEW);
        return checkPublicPromotion(productInfoDTOS);
    }

    @Override
    public DetailProductInfoDTO getDetailProductById(String id) {
        Optional<Product> rs = productRepository.findById(id);
        if (rs.isEmpty()) {
            throw new NotFoundException("Sản phẩm không tồn tại");
        }
        Product product = rs.get();

        if (product.getStatus() != 1) {
            throw new NotFoundException("Sản phâm không tồn tại");
        }
        DetailProductInfoDTO dto = new DetailProductInfoDTO();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setPrice(product.getSalePrice());
        dto.setViews(product.getView());
        dto.setSlug(product.getSlug());
        dto.setTotalSold(product.getTotalSold());
        dto.setDescription(product.getDescription());
        dto.setBrand(product.getBrand());
        dto.setFeedbackImages(product.getImageFeedBack());
        dto.setProductImages(product.getImages());
        dto.setComments(product.getComments());

        //Cộng sản phẩm xem
        product.setView(product.getView() + 1);
        productRepository.save(product);

        //Kiểm tra có khuyến mại
        Promotion promotion = promotionService.checkPublicPromotion();
        if (promotion != null) {
            dto.setCouponCode(promotion.getCouponCode());
            dto.setPromotionPrice(promotionService.calculatePromotionPrice(dto.getPrice(), promotion));
        } else {
            dto.setCouponCode("");
        }
        return dto;

    }

    @Override
    public List<ProductInfoDTO> getRelatedProducts(String id) {
        Optional<Product> product = productRepository.findById(id);
        if (product.isEmpty()) {
            throw new NotFoundException("Sản phẩm không tồn tại");
        }
        List<ProductInfoDTO> products = productRepository.getRelatedProducts(id, LIMIT_PRODUCT_RELATED);
        return checkPublicPromotion(products);
    }

    @Override
    public List<Integer> getListAvailableSize(String id) {
        return productSizeRepository.findAllSizeOfProduct(id);
    }

    @Override
    public void createSizeCount(CreateSizeCountRequest createSizeCountRequest) {

        //Kiểm trả size
        boolean isValid = false;
        for (int size : SIZE_VN) {
            if (size == createSizeCountRequest.getSize()) {
                isValid = true;
                break;
            }
        }
        if (!isValid) {
            throw new BadRequestException("Size không hợp lệ");
        }

        //Kiểm trả sản phẩm có tồn tại
        Optional<Product> product = productRepository.findById(createSizeCountRequest.getProductId());
        if (product.isEmpty()) {
            throw new NotFoundException("Không tìm thấy sản phẩm trong hệ thống!");
        }
//       Optional<ProductSize> productSizeOld = productSizeRepository.getProductSizeBySize(createSizeCountRequest.getSize(),createSizeCountRequest.getProductId());
        ProductSize productSize = new ProductSize();
        productSize.setProductId(createSizeCountRequest.getProductId());
        productSize.setSize(createSizeCountRequest.getSize());
        productSize.setQuantity(createSizeCountRequest.getCount());

        productSizeRepository.save(productSize);
    }

    @Override
    public List<ProductSize> getListSizeOfProduct(String id) {
        return productSizeRepository.findByProductId(id);
    }

    @Override
    public List<ShortProductInfoDTO> getListProduct() {
        return productRepository.getListProduct();
    }

    @Override
    public List<ShortProductInfoDTO> getAvailableProducts() {
        return productRepository.getAvailableProducts();
    }

    @Override
    public boolean checkProductSizeAvailable(String id, int size) {
        ProductSize productSize = productSizeRepository.checkProductAndSizeAvailable(id, size);
        if (productSize != null) {
            return true;
        }
        return false;
    }

    @Override
    public List<ProductInfoDTO> checkPublicPromotion(List<ProductInfoDTO> products) {
        //Kiểm tra có khuyến mại
        Promotion promotion = promotionService.checkPublicPromotion();
        if (promotion != null) {
            //Tính giá sản phẩm khi có khuyến mại
            for (ProductInfoDTO product : products) {
                long discountValue = promotion.getMaximumDiscountValue();
                if (promotion.getDiscountType() == DISCOUNT_PERCENT) {
                    long tmp = product.getPrice() * promotion.getDiscountValue() / 100;
                    if (tmp < discountValue) {
                        discountValue = tmp;
                    }
                }
                long promotionPrice = product.getPrice() - discountValue;
                if (promotionPrice > 0) {
                    product.setPromotionPrice(promotionPrice);
                } else {
                    product.setPromotionPrice(0);
                }
            }
        }

        return products;
    }

    @Override
    public PageableDTO filterProduct(FilterProductRequest req) {

        PageUtil pageUtil = new PageUtil(LIMIT_PRODUCT_SHOP, req.getPage());

        //Lấy danh sách sản phẩm và tổng số sản phẩm
        int totalItems;
        List<ProductInfoDTO> products;

        if (req.getSizes().isEmpty()) {
            //Nếu không có size
            products = productRepository.searchProductAllSize(req.getBrands(), req.getCategories(), req.getMinPrice(), req.getMaxPrice(), LIMIT_PRODUCT_SHOP, pageUtil.calculateOffset());
            totalItems = productRepository.countProductAllSize(req.getBrands(), req.getCategories(), req.getMinPrice(), req.getMaxPrice());
        } else {
            //Nếu có size
            products = productRepository.searchProductBySize(req.getBrands(), req.getCategories(), req.getMinPrice(), req.getMaxPrice(), req.getSizes(), LIMIT_PRODUCT_SHOP, pageUtil.calculateOffset());
            totalItems = productRepository.countProductBySize(req.getBrands(), req.getCategories(), req.getMinPrice(), req.getMaxPrice(), req.getSizes());
        }

        //Tính tổng số trang
        int totalPages = pageUtil.calculateTotalPage(totalItems);

        return new PageableDTO(checkPublicPromotion(products), totalPages, req.getPage());

    }

    @Override
    public PageableDTO searchProductByKeyword(String keyword, Integer page) {
        // Validate
        if (keyword == null) {
            keyword = "";
        }
        if (page == null) {
            page = 1;
        }

        PageUtil pageInfo = new PageUtil(LIMIT_PRODUCT_SEARCH, page);

        //Lấy danh sách sản phẩm theo key
        List<ProductInfoDTO> products = productRepository.searchProductByKeyword(keyword, LIMIT_PRODUCT_SEARCH, pageInfo.calculateOffset());

        //Lấy số sản phẩm theo key
        int totalItems = productRepository.countProductByKeyword(keyword);

        //Tính số trang
        int totalPages = pageInfo.calculateTotalPage(totalItems);

        return new PageableDTO(checkPublicPromotion(products), totalPages, page);
    }
    public PageableDTO searchProductInES(String keyword, Integer page) {
        if (keyword == null) keyword = "";
        // Loại bỏ tất cả khoảng trắng (bao gồm cả đầu, cuối và giữa các từ)
        keyword = keyword.replaceAll("\\s+", "").trim();
        if (page == null || page < 1) page = 1;
        Pageable pageable = PageRequest.of(page - 1, LIMIT_PRODUCT_SEARCH);
        if (keyword.isEmpty()) {
            return new PageableDTO(Collections.emptyList(), 0, page); // Trả về danh sách rỗng và tổng trang = 0
        }
        // Tìm kiếm trong Elasticsearch
        Page<ProductDocument> resultPage = productSearchRepository
                .findByNameContainingIgnoreCase(keyword,pageable);

        List<ProductInfoDTO> dtos;

        // Nếu không có kết quả từ Elasticsearch, fallback sang MySQL
        if (resultPage.isEmpty()) {
            System.out.println("SQL");

            // Lấy dữ liệu từ MySQL
            PageUtil pageInfo = new PageUtil(LIMIT_PRODUCT_SEARCH, page);
            List<ProductInfoDTO> products = productRepository.searchProductByKeyword(
                    keyword, LIMIT_PRODUCT_SEARCH, pageInfo.calculateOffset());

            // Lưu dữ liệu vào Elasticsearch
            List<ProductDocument> docs = products.stream()
                    .map(dto -> new ProductDocument(
                            dto.getId(),
                            dto.getName(),
                            dto.getSlug(),
                            dto.getPrice(),
                            dto.getViews(),
                            dto.getImages(),
                            dto.getTotalSold(),
                            dto.getPromotionPrice()
                    )).toList();

            productSearchRepository.saveAll(docs); // Lưu vào Elasticsearch

            dtos = products;
            return new PageableDTO(checkPublicPromotion(dtos),
                    pageInfo.calculateTotalPage(productRepository.countProductByKeyword(keyword)), page);
        }
        // Nếu có kết quả từ Elasticsearch
        System.out.println("ELK");
        dtos = resultPage.getContent().stream()
                .map(doc -> new ProductInfoDTO(
                        doc.getId(),
                        doc.getName(),
                        doc.getSlug(),
                        doc.getPrice(),
                        doc.getViews(),
                        doc.getImages(),
                        doc.getTotalSold()
                )).toList();

        return new PageableDTO(checkPublicPromotion(dtos), resultPage.getTotalPages(), page);
    }


    @Override
    public Promotion checkPromotion(String code) {
        return promotionRepository.checkPromotion(code);
    }

    @Override
    public long getCountProduct() {
        return productRepository.count();
    }

    @Override
    public void updatefeedBackImages(String id, UpdateFeedBackRequest req) {
        // Check product exist
        Optional<Product> rs = productRepository.findById(id);
        if (rs.isEmpty()) {
            throw new NotFoundException("Sản phẩm không tồn tại");
        }

        Product product = rs.get();
        product.setImageFeedBack(req.getFeedBackImages());
        try {
            productRepository.save(product);
        } catch (Exception ex) {
            throw new InternalServerException("Lỗi khi cập nhật hình ảnh on feet");
        }
    }

    @Override
    public List<Product> getAllProduct() {
        return productRepository.findAll();
    }

    @Override
    public Product createProductWithVariant(CreateProductWithVariantRequest createProductWithVariantRequest) {
        // Kiểm tra danh mục sản phẩm
        if (createProductWithVariantRequest.getCategoryIds().isEmpty()) {
            throw new BadRequestException("Phải chọn ít nhất một danh mục cho sản phẩm");
        }
        // Kiểm tra ảnh sản phẩm
        if (createProductWithVariantRequest.getImages().isEmpty()) {
            throw new BadRequestException("Phải có ít nhất một ảnh cho sản phẩm");
        }
        // Kiểm tra tên sản phẩm có tồn tại không
        Product existingProduct = productRepository.findByName(createProductWithVariantRequest.getName());
        if (existingProduct != null) {
            throw new BadRequestException("Tên sản phẩm đã tồn tại trong hệ thống, vui lòng chọn tên khác!");
        }

        // Tạo sản phẩm mới
        Product newProduct = new Product();
        newProduct.setId(RandomStringUtils.randomAlphanumeric(6));
        newProduct.setName(createProductWithVariantRequest.getName());
        newProduct.setDescription(createProductWithVariantRequest.getDescription());
        newProduct.setImages(createProductWithVariantRequest.getImages());
        // Lưu sản phẩm vào database
        Product savedProduct = productRepository.save(newProduct);

        // Duyệt qua từng biến thể sản phẩm để lưu
        for (var variantRequest : createProductWithVariantRequest.getVariants()) {
            ProductVariant productVariant = new ProductVariant();
            productVariant.setProduct(savedProduct);
            productVariant.setPrice(variantRequest.getPrice());
            productVariant.setStockQuantity(variantRequest.getStockQuantity());

            // Lưu biến thể sản phẩm
            ProductVariant savedVariant = productVariantRepository.save(productVariant);

            // Lưu thuộc tính của biến thể
            List<ProductVariantAttribute> attributes = variantRequest.getAttributes().stream().map(attrReq -> {
                ProductAttribute attribute = productAttributeRepository.findById(attrReq.getAttributeId())
                        .orElseThrow(() -> new BadRequestException("Thuộc tính không tồn tại!"));

                ProductVariantAttribute variantAttribute = new ProductVariantAttribute();
                variantAttribute.setProductVariant(savedVariant); // Gán biến thể
                variantAttribute.setAttribute(attribute);
                variantAttribute.setValue(attrReq.getValue());
                return variantAttribute;
            }).collect(Collectors.toList());
            // Lưu thuộc tính vào database
            productVariantAttributeRepository.saveAll(attributes);
        }

        return savedProduct;
    }

}
