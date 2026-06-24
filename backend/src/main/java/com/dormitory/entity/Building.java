package com.dormitory.entity;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 宿舍楼实体类
 */
import jakarta.persistence.*;
        import jakarta.validation.constraints.*;
        import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "buildings")
public class Building {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "楼宇名称不能为空")
    @Size(max = 50, message = "楼宇名称过长")
    @Column(nullable = false, length = 50, unique = true)
    private String name;

    @NotBlank(message = "位置不能为空")
    @Size(max = 100, message = "位置描述过长")
    @Column(nullable = false, length = 100)
    private String location;

    @NotNull(message = "容量不能为空")
    @Min(value = 1, message = "容量必须大于0") // 逻辑安全校验
    @Column(nullable = false)
    private Integer capacity;

    // ... timestamps ...
    @Column(name = "created_at") private LocalDateTime createdAt;
    @Column(name = "updated_at") private LocalDateTime updatedAt;
    @PrePersist protected void onCreate() { createdAt = LocalDateTime.now(); updatedAt = LocalDateTime.now(); }
    @PreUpdate protected void onUpdate() { updatedAt = LocalDateTime.now(); }
}