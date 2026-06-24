package com.dormitory.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * 用户实体类
 * 包含管理员和学生
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "用户名不能为空")
    @Size(min = 4, max = 50, message = "用户名长度需在4-50字符之间")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "用户名只能包含字母、数字和下划线") // 安全性：防止特殊字符
    @Column(unique = true, nullable = false, length = 50)
    private String username;

    @NotBlank(message = "学号不能为空")
    @Size(max = 50, message = "学号长度不能超过50")
    @Pattern(regexp = "^[a-zA-Z0-9]+$", message = "学号只能包含字母和数字") // 安全性：规范格式
    @Column(name = "student_id", unique = true, nullable = false, length = 50)
    private String studentId;

    @NotBlank(message = "密码不能为空") // 注意：通常DTO层校验原始密码复杂度，Entity层存的是加密串
    @Size(max = 100, message = "密码字段过长")
    @Column(nullable = false, length = 100)
    private String password;

    @NotBlank(message = "姓名不能为空")
    @Size(max = 50, message = "姓名长度不能超过50")
    @Column(nullable = false, length = 50)
    private String name;

    @NotNull(message = "用户角色不能为空")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;

    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确") // 安全性：正则校验
    @Column(length = 20)
    private String phone;

    @Email(message = "邮箱格式不正确")
    @Size(max = 100, message = "邮箱长度不能超过100")
    @Column(length = 100)
    private String email;

    @Size(max = 255, message = "头像字段过长")
    @Column(length = 255)
    private String avatar = "👤";

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum UserRole {
        ADMIN("管理员"),
        STUDENT("学生");
        private final String description;
        UserRole(String description) { this.description = description; }
        public String getDescription() { return description; }
    }
}
