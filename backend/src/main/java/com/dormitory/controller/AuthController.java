package com.dormitory.controller;

import com.dormitory.Json.RestBean;
import com.dormitory.entity.User;
import com.dormitory.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.Map;

/**
 * 认证控制器
 */
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private AuthService authService;

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public RestBean<?> login(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String password = request.get("password");
        String studentId = request.get("studentId");
        String role = request.get("role");

        Map<String, Object> response = authService.login(username, password, studentId, role);

        if (Boolean.TRUE.equals(response.get("success"))) {
            // 返回用户信息（包括 token）
            return RestBean.success(response);
        } else {
            String message = (String) response.get("message");
            return RestBean.failure(401, message);
        }
    }

    /**
     * 发送验证码
     */
    @PostMapping("/send-code")
    public RestBean<String> sendVerificationCode(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        try {
            String result = authService.sendVerificationCode(email);
            return RestBean.success(result);
        } catch (Exception e) {
            return RestBean.failure(400, e.getMessage());
        }
    }

    /**
     * 用户注册
     * 前端需传入 user 对象信息以及 verificationCode
     */
    @PostMapping("/register")
    public RestBean<String> register(@RequestBody Map<String, Object> request) {
        // 手动解析参数
        String verificationCode = (String) request.get("verificationCode");
        
        // 构建 User 对象 (这里简单处理，实际可以使用 ObjectMapper)
        User user = new User();
        user.setUsername((String) request.get("username"));
        user.setPassword((String) request.get("password"));
        user.setStudentId((String) request.get("studentId"));
        user.setName((String) request.get("name"));
        user.setPhone((String) request.get("phone"));
        user.setEmail((String) request.get("email"));
        // 默认角色为学生，如果前端传了也可以设置
        user.setRole(User.UserRole.STUDENT);

        String result = authService.register(user, verificationCode);
        if ("注册成功".equals(result)) {
            return RestBean.success(result);
        } else {
            return RestBean.failure(400, result);
        }
    }

    /**
     * 忘记密码 - 重置管理员密码
     */
    @PostMapping("/forgot-password")
    public RestBean<String> forgotPassword(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String email = request.get("email");
        String phone = request.get("phone");
        String newPassword = request.get("newPassword");
        String verificationCode = request.get("verificationCode");

        String result = authService.resetAdminPassword(username, email, phone, newPassword, verificationCode);

        if ("密码重置成功".equals(result)) {
            return RestBean.success(result);
        } else {
            return RestBean.failure(400, result);
        }
    }
}
