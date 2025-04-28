# ShoeShop - Website Bán Giày Online

Website bán giày online với các chức năng đặt hàng, tìm kiếm sản phẩm, quản lý đơn hàng và tối ưu hiệu suất bằng Redis và Elasticsearch.

## 🎯 Giới thiệu

Dự án được thực hiện nhằm xây dựng một hệ thống website bán giày đáp ứng nhu cầu thương mại điện tử cơ bản và tối ưu hóa tốc độ truy cập.

## ✨ Tính năng
- Xử lý bất đồng bộ khi đặt hàng sử dụng kafka cho bài toán order khi nhiều ngươời dùng đặt 1 đơn hàng  co 1 sản phẩm
- Chức năng sendmail
- Thực hiện upload ảnh sử dụng minio
- Xem sản phẩm , chi tiết , bài viết , người dùng có thể bình luận
- Đăng ký, đăng nhập người dùng
- Tìm kiếm sản phẩm nhanh với Elasticsearch
- Thêm sản phẩm vào giỏ hàng và thanh toán
- Quản lý đơn hàng, quản lý sản phẩm , tài khoản , khuyến mãi (admin)
- Tối ưu hiệu suất bằng Redis caching
- Thanh toán qua VNPay

## 🛠️ Công nghệ sử dụng

- Backend: Spring Boot / Spring MVC
- Frontend: HTML, CSS, JavaScript  / Thymeleaf
- Database: MySQL
- Cache: Redis
- Search Engine: Elasticsearch
- Công cụ khác: Git, Postman , JMeter
- File : Minio
## 🏗️ Kiến trúc hệ thống
- Kiến trúc Monolithic
- Redis làm lớp cache trung gian
- Elasticsearch hỗ trợ tìm kiếm sản phẩm

## 🚀 Hướng dẫn cài đặt

1. Clone repository: `git clone https://github.com/HuynhDuc23/ShoesShop`
2. Cài đặt:
    - Tải docker về local
    - Sử dụng file docker-compose.yml
    - gõ lệnh docker compose up để chạy ứng dụng (lưu ý nhớ cd vào project)
3. Import database từ file `/database/shoes.sql`
4. Cấu hình `application.properties`
5. Chạy lệnh:
    ```bash
    mvn clean install
    mvn spring-boot:run
    ```
6. Truy cập: `http://localhost:80`
7. Tài khoản sử dụng 
    - admin : admin@gmail.com , admin@123
    - người dùng : tự tạo

## 📂 Cấu trúc thư mục

- `/src/main/java/` - Mã nguồn Java
- `/src/main/resources/` - File cấu hình
- `/templates/` - Giao diện Thymeleaf
- `/static/` - Tài nguyên CSS, JS
- `/database/` - CSDL MySQL
- `/doc/` - Tài liệu báo cáo (nếu có)

## 👨‍💻 Tác giả

- TRANVUUYNHDUC - MSSV: 3120221154
- TRUONG DAI HOC SU PHAM - DAI HOC DA NANG

## 📄 License

Project này được thực hiện cho mục đích học tập, không sử dụng cho mục đích thương mại.
