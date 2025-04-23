package com.huynhduc.application.service.impl;

import com.huynhduc.application.entity.User;
import com.huynhduc.application.exception.BadRequestException;
import com.huynhduc.application.model.dto.UserDTO;
import com.huynhduc.application.model.mapper.UserMapper;
import com.huynhduc.application.model.request.ChangePasswordRequest;
import com.huynhduc.application.model.request.CreateUserRequest;
import com.huynhduc.application.model.request.UpdateProfileRequest;
import com.huynhduc.application.repository.UserRepository;
import com.huynhduc.application.service.MailService;
import com.huynhduc.application.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.huynhduc.application.constant.Contant.LIMIT_USER;

@Component
public class UserServiceImpl implements UserService {

    @Autowired
    private MailService mailService ;

    @Autowired
    private UserRepository userRepository;

    @Override
    public List<UserDTO> getListUsers() {
        List<User> users = userRepository.findAll();
        List<UserDTO> userDTOS = new ArrayList<>();
        for (User user : users) {
            userDTOS.add(UserMapper.toUserDTO(user));
        }
        return userDTOS;
    }

    @Override
    public Page<User> adminListUserPages(String fullName, String phone, String email, Integer page) {
        page--;
        if (page < 0) {
            page = 0;
        }
        Pageable pageable = PageRequest.of(page, LIMIT_USER, Sort.by("created_at").descending());
        return userRepository.adminListUserPages(fullName, phone, email, pageable);
    }

    @Override
    public User createUser(CreateUserRequest createUserRequest) {
        User user = userRepository.findByEmail(createUserRequest.getEmail());
        if (user != null) {
            throw new BadRequestException("Email đã tồn tại trong hệ thống. Vui lòng sử dụng email khác!");
        }
        user = UserMapper.toUser(createUserRequest);
        userRepository.save(user);
        return user;
    }

    @Override
    public void changePassword(User user, ChangePasswordRequest changePasswordRequest) {
        //Kiểm tra mật khẩu
        if (!BCrypt.checkpw(changePasswordRequest.getOldPassword(), user.getPassword())) {
            throw new BadRequestException("Mật khẩu cũ không chính xác");
        }

        String hash = BCrypt.hashpw(changePasswordRequest.getNewPassword(), BCrypt.gensalt(12));
        user.setPassword(hash);
        userRepository.save(user);
    }

    @Override
    public User updateProfile(User user, UpdateProfileRequest updateProfileRequest) {
        user.setFullName(updateProfileRequest.getFullName());
        user.setPhone(updateProfileRequest.getPhone());
        user.setAddress(updateProfileRequest.getAddress());

        return userRepository.save(user);
    }

    @Override
    public void processOAuthPostLogin(String email, String name) {
        User user = userRepository.findByEmail(email);
        if (user == null) {
            user = new User();
            user.setEmail(email);
            user.setFullName(name);
            user.setProvider("GOOGLE");
            userRepository.save(user);
        }
        // Xu ly logic login
        System.out.println("oauth2");
    }

    @Override
    public User findByEmail(String email) {
       return this.userRepository.findByEmail(email);
    }
}
