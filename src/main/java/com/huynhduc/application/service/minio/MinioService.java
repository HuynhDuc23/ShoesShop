package com.huynhduc.application.service.minio;

import io.minio.*;
import io.minio.messages.Item;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class MinioService {

    private final MinioClient minioClient;
    @Value("${minio.endpoint}")
    private String endpoint;

    @Value("${minio.bucket-name}")
    private String bucketName;
    @Value("${minio.accessKey}")
    private String secretKey ;
    @Value("${minio.secretKey}")
    private String accessKey;


    public MinioService(MinioClient minioClient) {
        this.minioClient = minioClient;
    }

    public String uploadFile(MultipartFile file) {
        try {
            // Kiểm tra bucket có tồn tại không
            boolean found = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
            if (!found) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
                System.out.println("Bucket '" + bucketName + "' được tạo thành công!");
            }
            // Tạo tên file duy nhất
            String fileName = UUID.randomUUID() + "-" + file.getOriginalFilename();

            // Xác định loại nội dung (content type)
            String contentType = file.getContentType();
            System.out.println(contentType.toString());
            // Upload file lên MinIO
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(fileName)
                            .stream(file.getInputStream(), file.getSize(), -1)
                            .contentType(contentType)
                            .build()
            );

            System.out.println("File uploaded successfully: " + fileName);
            String urlMinio = endpoint + "/" + bucketName + "/" + fileName;
            return urlMinio ;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Lỗi khi upload file lên MinIO: " + e.getMessage(), e);
        }
    }
    // get All images from Mino
    public List<String> getAllImages() {
        List<String> imageUrls = new ArrayList<>();
        try {
             MinioClient minioClient = MinioClient.builder()
                     .endpoint(endpoint)
                     .credentials(accessKey,secretKey)
                     .build();
             var results = minioClient.listObjects(ListObjectsArgs.builder().bucket(bucketName).build());
             for(var result : results ){
                 Item item = result.get();
                 String url = endpoint + "/" + bucketName + "/" + item.objectName();
                 imageUrls.add(url);
             }
        }catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Lỗi khi lấy danh sách ảnh từ MinIO: " + e.getMessage(), e);
        }
        return imageUrls;
    }
        public  void deleteImageFromMinio(String name){
        if(!isFileExist(name)){
            throw new RuntimeException("File không tồn tại");
        }
        try {
            MinioClient minioClient = MinioClient.builder()
                    .endpoint(endpoint)
                    .credentials(accessKey,secretKey)
                    .build();
            minioClient.removeObject(RemoveObjectArgs.builder().bucket(bucketName)
                            .object(name)
                            .build());
        }catch(Exception exception){
            exception.printStackTrace();
            throw new RuntimeException("Lỗi khi xóa ảnh từ MinIO: " + exception.getMessage(), exception);
        }
    }
    // check is file exist
    public boolean isFileExist(String name){
        try {
            MinioClient minioClient = MinioClient.builder()
                    .endpoint(endpoint)
                    .credentials(accessKey,secretKey)
                    .build();
            return minioClient.statObject(StatObjectArgs.builder().bucket(bucketName).object(name).build()) != null;
        }catch(Exception exception){
            return false;
        }
    }

}