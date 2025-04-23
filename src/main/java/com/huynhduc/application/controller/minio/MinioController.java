package com.huynhduc.application.controller.minio;

import com.huynhduc.application.service.minio.MinioService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/files")
public class MinioController {

    private final MinioService minioService;

    public MinioController(MinioService minioService) {
        this.minioService = minioService;
    }

    @PostMapping(value = "/upload")
    public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file) {
        String fileName = minioService.uploadFile(file);
        return ResponseEntity.ok("File uploaded successfully: " + fileName);
    }
    @GetMapping(value = "/all-images")
    public ResponseEntity<?> getAllImagesFromMinio() {
        return ResponseEntity.ok(minioService.getAllImages());
    }
    @DeleteMapping("/delete/image/{fileName}")
    public ResponseEntity<?> deleteImage(@PathVariable("fileName") String fileName) {
        minioService.deleteImageFromMinio(fileName);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body("Image deleted successfully");
    }
}