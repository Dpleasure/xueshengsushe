package com.dormitory.controller;

import com.dormitory.Json.RestBean;
import com.dormitory.entity.User;
import com.dormitory.repository.UserRepository;
import com.dormitory.service.UserDetailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 管理员控制器
 */
@RestController
@RequestMapping("/admins")
@CrossOrigin(origins = "*")
public class AdminController {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private UserDetailService userDetailService;
    
    /**
     * 获取所有管理员
     */
    @GetMapping
    public RestBean<List<User>> getAllAdmins() {
        List<User> admins = userRepository.findAll().stream()
                .filter(user -> user.getRole() == User.UserRole.ADMIN)
                .collect(Collectors.toList());
        return RestBean.success(admins);
    }
    
    /**
     * 根据ID获取管理员
     */
    @GetMapping("/{id}")
    public RestBean<User> getAdminById(@PathVariable Long id) {
        return userRepository.findById(id)
                .filter(user -> user.getRole() == User.UserRole.ADMIN)
                .map(RestBean::success)
                .orElse(RestBean.failure("管理员不存在"));
    }
    
    /**
     * 搜索管理员
     */
    @GetMapping("/search")
    public RestBean<List<User>> searchAdmins(@RequestParam(required = false) String keyword) {
        List<User> admins = userRepository.findAll().stream()
                .filter(user -> user.getRole() == User.UserRole.ADMIN)
                .filter(user -> keyword == null || keyword.trim().isEmpty() || 
                        user.getUsername().contains(keyword) || user.getName().contains(keyword))
                .collect(Collectors.toList());
        return RestBean.success(admins);
    }
    
    /**
     * 添加管理员
     */
    @PostMapping
    public RestBean<User> addAdmin(@RequestBody User admin) {
        admin.setRole(User.UserRole.ADMIN);
        return RestBean.success(userRepository.save(admin));
    }
    
    /**
     * 更新管理员
     */
    @PutMapping("/{id}")
    public RestBean<User> updateAdmin(@PathVariable Long id, @RequestBody User admin) {
        // 查找现有管理员
        return userRepository.findById(id)
                .filter(user -> user.getRole() == User.UserRole.ADMIN)
                .map(existingAdmin -> {
                    // 更新基本信息
                    existingAdmin.setUsername(admin.getUsername());
                    existingAdmin.setName(admin.getName());
                    existingAdmin.setStudentId(admin.getStudentId());
                    existingAdmin.setPhone(admin.getPhone());
                    existingAdmin.setEmail(admin.getEmail());
                    
                    // 只有当密码不为空时才更新密码
                    if (admin.getPassword() != null && !admin.getPassword().trim().isEmpty()) {
                        existingAdmin.setPassword(admin.getPassword());
                    }
                    
                    // 确保角色保持为管理员
                    existingAdmin.setRole(User.UserRole.ADMIN);
                    
                    return RestBean.success(userRepository.save(existingAdmin));
                })
                .orElse(RestBean.failure("管理员不存在"));
    }
    
    /**
     * 删除管理员
     */
    @DeleteMapping("/{id}")
    public RestBean<String> deleteAdmin(@PathVariable Long id) {
        userRepository.deleteById(id);
        return RestBean.success("删除成功");
    }
    
    /**
     * 用户注册
     */
    @PostMapping("/register")
    public RestBean<String> register(@RequestBody User user) {
        try {
            String result = userDetailService.register(user);
            if ("注册成功".equals(result)) {
                return RestBean.success(result);
            } else {
                return RestBean.failure(400, result);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return RestBean.failure(500, "注册失败: " + e.getMessage());
        }
    }
}
