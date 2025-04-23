package com.huynhduc.application.security;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.CacheControl;
import org.springframework.web.servlet.config.annotation.*;

import java.util.concurrent.TimeUnit;

@Configuration
@EnableWebMvc
public class WebMvcConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // Cho phép tất cả các đường dẫn
                .allowedOrigins("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("authorization", "content-type", "x-auth-token")
                .exposedHeaders("x-auth-token")
                .allowCredentials(false).maxAge(3600);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new ConfigInterceptor())
                .excludePathPatterns("/image/**", "/vendor/**", "/css/**", "/script/**", "/api/**", "/api/register", "/favicon.ico","/shop/**", "/adminlte/**", "/media/static/**");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/image/**").addResourceLocations("classpath:/static/image/")
                .setCacheControl(CacheControl.maxAge(2, TimeUnit.HOURS).cachePublic());
        registry.addResourceHandler("/vendor/**").addResourceLocations("classpath:/static/vendor/")
                .setCacheControl(CacheControl.maxAge(2, TimeUnit.HOURS).cachePublic());
        registry.addResourceHandler("/script/**").addResourceLocations("classpath:/static/script/")
                .setCacheControl(CacheControl.maxAge(2, TimeUnit.HOURS).cachePublic());
        registry.addResourceHandler("/css/**").addResourceLocations("classpath:/static/css/")
                .setCacheControl(CacheControl.maxAge(2, TimeUnit.HOURS).cachePublic());
        registry.addResourceHandler("/adminlte/**").addResourceLocations("classpath:/static/adminlte/")
                .setCacheControl(CacheControl.maxAge(2, TimeUnit.HOURS).cachePublic());
        registry.addResourceHandler("/shop/**").addResourceLocations("classpath:/static/shop/")
                .setCacheControl(CacheControl.maxAge(2, TimeUnit.HOURS).cachePublic());
    }
}
// Phương thức này đăng ký interceptor ConfigInterceptor để xử lý các yêu cầu trước và sau khi chúng được xử lý bởi controller

// addResourceHandler("/image/**").addResourceLocations("classpath:/static/image/"): Định nghĩa rằng các yêu cầu tới /image/** sẽ được phục vụ từ thư mục static/image trong classpath.​
//
//setCacheControl(CacheControl.maxAge(2, TimeUnit.HOURS).cachePublic()): Thiết lập bộ nhớ đệm cho các tài nguyên này trong 2 giờ, cho phép trình duyệt cache chúng để cải thiện hiệu suất.