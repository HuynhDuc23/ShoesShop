package com.huynhduc.application.security;

import com.huynhduc.application.constant.Contant;
import com.huynhduc.application.entity.User;
import com.huynhduc.application.repository.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;
import java.util.Collections;
import java.util.Map;
@Component
public class CustomOAuth2UserService extends DefaultOAuth2UserService {
    private final UserRepository userRepository ;
    public CustomOAuth2UserService(UserRepository userRepository){
        this.userRepository = userRepository ;
    }


    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        System.out.println(">> CustomOAuth2UserService đang chạy");
       // call api
        OAuth2User oAuth2User = super.loadUser(userRequest);
        Map<String, Object> attributes = oAuth2User.getAttributes();

        // get provider
        String registeredClientId = userRequest.getClientRegistration().getRegistrationId();
        // Process oAuth2User or map it to your local user database
        String email = (String) attributes.get("email");
        String fullName = (String) attributes.get("name");
        if(email!=null){
            User user = userRepository.findByEmail(email);
            if(user ==null){
                User newUser = User.builder()
                        .fullName(fullName)
                        .email(email)
                        .provider("GOOGLE")
                        .build();
                userRepository.save(newUser);
            }
        }
        // “Tôi đã xử lý thông tin người dùng Google rồi, và đây là đối tượng OAuth2User (người dùng đã xác thực) để hệ thống sử dụng.”
        return new DefaultOAuth2User(
                Collections.singletonList(new SimpleGrantedAuthority(Contant.ROLE_USER)),
                oAuth2User.getAttributes(),
                "email"); // trả ve thon tin cua nguoi dung
        // Spring Security cần một đối tượng OAuth2User để đại diện cho người dùng đã đăng nhập thành công bằng OAuth2.
        //DefaultOAuth2User là một class đơn giản để làm việc đó.
        // principal.getName(); // sẽ trả về giá trị oAuth2User.getAttribute("email")
    }
}
