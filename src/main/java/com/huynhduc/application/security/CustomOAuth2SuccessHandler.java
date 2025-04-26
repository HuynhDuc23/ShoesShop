//package com.huynhduc.application.security;
//
//import com.huynhduc.application.constant.Contant;
//import com.huynhduc.application.entity.User;
//import com.huynhduc.application.service.UserService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.GrantedAuthority;
//import org.springframework.security.oauth2.core.user.OAuth2User;
//import org.springframework.security.web.DefaultRedirectStrategy;
//import org.springframework.security.web.RedirectStrategy;
//import org.springframework.security.web.WebAttributes;
//import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
//
//import javax.servlet.ServletException;
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
//import javax.servlet.http.HttpSession;
//import java.io.IOException;
//import java.util.Collection;
//import java.util.HashMap;
//import java.util.Map;
//
//public class CustomOAuth2SuccessHandler implements AuthenticationSuccessHandler {
//    private final UserService userService ;
//    @Autowired
//    public CustomOAuth2SuccessHandler(UserService userService) {
//        this.userService = userService;
//    }
//    private final RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();
//
//    protected String determineTargetUrl(final Authentication authentication) {
//
//
//        Map<String, String> roleTargetUrlMap = new HashMap<>();
//        roleTargetUrlMap.put(Contant.ROLE_USER, "/");
//        roleTargetUrlMap.put(Contant.ROLE_ADMIN, "/admin/dashboard");
//
//        final Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
//        for (final GrantedAuthority grantedAuthority : authorities) {
//            String authorityName = grantedAuthority.getAuthority();
//            if(roleTargetUrlMap.containsKey(authorityName)) {
//                return roleTargetUrlMap.get(authorityName);
//            }
//        }
//        throw new IllegalStateException();
//    }
//    protected void clearAuthenticationAttributes(HttpServletRequest request, Authentication authentication) {
//        HttpSession session = request.getSession(false);
//        if (session == null) {
//            return;
//        }
//        // 1. Xóa thông tin lỗi đăng nhập cũ nếu có
//        session.removeAttribute(WebAttributes.AUTHENTICATION_EXCEPTION);
//        // 2. Lấy email từ đối tượng Authentication
//        String email = authentication.getName();
//        session.setAttribute("email",email);
//
//        User user = userService.findByEmail(email);
//       // session.setAttribute("fullName",user.getFullName());
////       session.setAttribute("phoneNumber",user.getPhoneNumber());
////        session.setAttribute("address",user.getAddress());
//    }
//
//    @Override
//    public void onAuthenticationSuccess(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Authentication authentication) throws IOException, ServletException {
//        String targetUrl = determineTargetUrl(authentication);
//        System.out.println(targetUrl);
//        if (httpServletResponse.isCommitted()) {
//            return;
//        }
//        redirectStrategy.sendRedirect(httpServletRequest, httpServletResponse, targetUrl);
//        clearAuthenticationAttributes(httpServletRequest, authentication);
//    }
//}
