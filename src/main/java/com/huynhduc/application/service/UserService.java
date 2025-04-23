package com.huynhduc.application.service;
import com.huynhduc.application.entity.User;
import com.huynhduc.application.model.dto.UserDTO;
import com.huynhduc.application.model.request.ChangePasswordRequest;
import com.huynhduc.application.model.request.CreateUserRequest;
import com.huynhduc.application.model.request.UpdateProfileRequest;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface UserService {
    List<UserDTO> getListUsers();

    Page<User> adminListUserPages(String fullName, String phone, String email, Integer page);

    User createUser(CreateUserRequest createUserRequest);

    void changePassword(User user, ChangePasswordRequest changePasswordRequest);

    User updateProfile(User user, UpdateProfileRequest updateProfileRequest);

    public void processOAuthPostLogin(String email,String name);
    public User findByEmail(String email);
}
