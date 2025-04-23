package com.huynhduc.application.security;


import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ConfigInterceptor extends HandlerInterceptorAdapter {
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        if (modelAndView != null) {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getAuthorities() != null &&
                    !authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ANONYMOUS"))) {

                CustomUserDetails principal = (CustomUserDetails) authentication.getPrincipal();
                modelAndView.addObject("user_fullname", principal.getUser().getFullName());
                modelAndView.addObject("user_phone", principal.getUser().getPhone());
                modelAndView.addObject("user_email", principal.getUser().getEmail());
                modelAndView.addObject("user_address", principal.getUser().getAddress());
                modelAndView.addObject("isLogined", true);
            } else {
                modelAndView.addObject("isLogined", false);
            }
        }
    }
}
// công dụng
//  Mục đích chính của ConfigInterceptor
// Lớp ConfigInterceptor trong ứng dụng Spring MVC của bạn là một interceptor được sử dụng để can thiệp vào quá trình xử lý yêu cầu HTTP, cụ thể là sau khi controller xử lý xong nhưng trước khi view được render.​
// postHandle(): được gọi sau khi controller xử lý xong  nhưng trước khi render
// SecurityContextHolder.getContext().getAuthentication(): Lấy thông tin xác thực hiện tại từ Spring Security.
// Interceptor trong Spring MVC được sử dụng để can thiệp vào quá trình xử lý request trước và sau khi controller thực thi.


// Khi một người dùng chưa đăng nhập truy cập vào ứng dụng, Spring Security tạo một đối tượng AnonymousAuthenticationToken và gán vai trò ROLE_ANONYMOUS cho người dùng đó
