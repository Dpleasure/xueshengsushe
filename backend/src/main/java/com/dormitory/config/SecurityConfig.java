package com.dormitory.config;

import com.alibaba.fastjson2.JSONObject;
import com.dormitory.Json.RestBean;
import com.dormitory.service.UserDetailService;
import com.dormitory.util.JwtUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.io.IOException;

/**
 * Spring Security配置
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Autowired
    JwtUtil    JwtUtil;
    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;
    @Autowired
    UserDetailService userDetailService;
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/login").permitAll() // 允许登录接口公开访问
                .requestMatchers("/api/auth/register").permitAll() // 允许注册接口公开访问（之前是hasRole("ADMIN")，导致未登录无法注册）
                .requestMatchers("/api/auth/send-code").permitAll() // 允许发送验证码接口公开访问
                .requestMatchers("/api/auth/forgot-password").permitAll() // 允许忘记密码接口公开访问
                .requestMatchers("/uploads/**").permitAll() // 允许访问上传的图片文件
                .anyRequest().authenticated())
                .userDetailsService(userDetailService)
                .logout(c->c.logoutUrl("/api/auth/logout")
                        .logoutSuccessHandler(this::onAuthenticationSuccess))
                .exceptionHandling(c->c.authenticationEntryPoint(this::onAuthenticationFailure))  //防止乱输入地址重定向的登录还是返回数据
                .sessionManagement(c->c.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .cors(c->c.configurationSource(this.configSource()));




        return http.build();
    }
    private CorsConfigurationSource configSource() {
        CorsConfiguration c = new CorsConfiguration();
        c.addAllowedOriginPattern("*");
        c.addAllowedHeader("*");
        c.addAllowedMethod("*");
        c.addExposedHeader("*");
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", c);
        return source;
    }
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        response.setContentType("application/json;charset=utf-8");
        
        // 获取用户名和角色
        if(request.getRequestURI().endsWith("/login")) {
            String username = authentication.getName();
            String role = authentication.getAuthorities().stream()
                    .map(authority -> authority.getAuthority())
                    .findFirst()
                    .orElse("ROLE_USER");

            // 生成JWT Token
            String token = JwtUtil.generateToken(username, role);

            // 构建返回数据（使用Map包装token、username等信息）
            java.util.Map<String, Object> data = new java.util.HashMap<>();
            data.put("token", token);
            data.put("username", username);
            data.put("role", role);
            data.put("message", "登录成功");

            RestBean<java.util.Map<String, Object>> responseData = RestBean.success(data);

            response.getWriter().write(JSONObject.toJSONString(responseData));
        }
        else if(request.getRequestURI().endsWith("/logout")) {
            response.getWriter().write(JSONObject.toJSONString(RestBean.success("退出登录成功")));
        }

    }
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
        response.setContentType("application/json;charset=utf-8");
        response.getWriter().write(JSONObject.toJSONString(RestBean.failure(401, exception.getMessage())));
    }

}
