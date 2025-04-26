package com.huynhduc.application.controller.admin;

import com.github.javafaker.Faker;
import com.huynhduc.application.entity.Post;
import com.huynhduc.application.entity.User;
import com.huynhduc.application.model.dto.PageableDTO;
import com.huynhduc.application.model.dto.PostDTO;
import com.huynhduc.application.model.request.CreatePostRequest;
import com.huynhduc.application.repository.PostRepository;
import com.huynhduc.application.repository.UserRepository;
import com.huynhduc.application.security.CustomUserDetails;
import com.huynhduc.application.service.ImageService;
import com.huynhduc.application.service.PostService;
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
public class PostController {
    @Autowired
    private PostRepository postRepository ;
    @Autowired
    private UserRepository userRepository ;
    @Autowired
    private PostService postService;

    @Autowired
    private ImageService imageService;

    @GetMapping("/admin/posts")
    public String getPostManagePage(Model model,
                                    @RequestParam(defaultValue = "1") Integer page,
                                    @RequestParam(defaultValue = "") String title,
                                    @RequestParam(defaultValue = "") String status) {
        if (!status.equals("") && !status.equals("0") && !status.equals("1")) {
            return "error/500";
        }

        Page<Post> result = postService.adminGetListPosts(title, status, page);
        model.addAttribute("posts", result.getContent());
        model.addAttribute("totalPages", result.getTotalPages());
        model.addAttribute("currentPage", result.getPageable().getPageNumber() +1);

        return "admin/post/list";
    }

    @GetMapping("/admin/posts/create")
    public String getPostCreatePage(Model model) {
        // Get list image of user
        User user = ((CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUser();
        List<String> images = imageService.getListImageOfUser(user.getId());
        model.addAttribute("images", images);

        return "admin/post/create";
    }

    @GetMapping("/api/admin/posts")
    public ResponseEntity<Object> getListPosts(@RequestParam(defaultValue = "", required = false) String title,
                                               @RequestParam(defaultValue = "", required = false) String status,
                                               @RequestParam(defaultValue = "1", required = false) Integer page) {
        Page<Post> posts = postService.adminGetListPosts(title, status, page);
        return ResponseEntity.ok(posts);
    }

    @PostMapping("/api/admin/posts")
    public ResponseEntity<Object> createPost(@Valid @RequestBody CreatePostRequest createPostRequest) {
        User user = ((CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUser();
        Post post = postService.createPost(createPostRequest, user);
        return ResponseEntity.ok(post);
    }

    @GetMapping("/admin/posts/{slug}/{id}")
    public String getPostDetailPage(Model model, @PathVariable long id) {
        // Get list image of user
        User user = ((CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUser();
        List<String> images = imageService.getListImageOfUser(user.getId());
        model.addAttribute("images", images);

        Post post = postService.getPostById(id);
        model.addAttribute("post", post);

        return "admin/post/edit";
    }

    @PutMapping("/api/admin/posts/{id}")
    public ResponseEntity<Object> updatePost(@Valid @RequestBody CreatePostRequest createPostRequest, @PathVariable long id) {
        User user = ((CustomUserDetails) (SecurityContextHolder.getContext().getAuthentication().getPrincipal())).getUser();
        postService.updatePost(createPostRequest, user, id);
        return ResponseEntity.ok("Cập nhật thành công");
    }

    @DeleteMapping("/api/admin/posts/{id}")
    public ResponseEntity<Object> deletePost(@PathVariable long id) {
        postService.deletePost(id);
        return ResponseEntity.ok("Xóa thành công");
    }
    @GetMapping("/fake/post")
    public ResponseEntity<?> fakePost() {
        for(int i = 1 ; i < 100000 ;i++){
            Faker faker = new Faker();
            Post post = new Post();
            post.setTitle(faker.lorem().sentence(6));  // Tiêu đề ngẫu nhiên
            post.setContent(faker.lorem().paragraph(5));  // Nội dung ngẫu nhiên
            post.setDescription(faker.lorem().paragraph(2));  // Mô tả ngẫu nhiên
            post.setSlug(faker.lorem().word() + "-" + faker.number().digits(5));  // Slug ngẫu nhiên
            post.setThumbnail("/media/static/4909fce4-075f-4999-8a0d-b31287c25b6d.jpeg");  // Thumbnails giả
            post.setCreatedAt(new Timestamp(System.currentTimeMillis()));  // Thời gian tạo
            post.setModifiedAt(new Timestamp(System.currentTimeMillis()));  // Thời gian chỉnh sửa
            post.setPublishedAt(new Timestamp(System.currentTimeMillis()));  // Thời gian xuất bản
            post.setStatus(1);  // Trạng thái ngẫu nhiên 0 hoặc 1

            // Thêm user giả cho createdBy và modifiedBy
            User user = this.userRepository.findById(1L).get();
            post.setCreatedBy(user);
            post.setModifiedBy(user);
            this.postRepository.save(post);
        }
        return ResponseEntity.status(HttpStatus.OK).body("ok");
    }
}
