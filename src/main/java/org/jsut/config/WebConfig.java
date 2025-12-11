package org.jsut.config;

import org.jsut.interceptors.LoginInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private LoginInterceptor loginInterceptor;
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loginInterceptor)
                .excludePathPatterns(
                        "/user/login",
                        "/user/register",
                        "/article/public/**",
                        "/category/public/**" ,// <--- 新增这一行
                        "/index.html",
                        "/user/sendCode",      // <--- 放行发送验证码
                        "/user/loginByEmail",
                        "/assets/**",  // 放行静态资源
                        "/vite.svg",
                        "/",          // 放行根路径

                        "/h5/**"
                );
    }
}
