package com.dormitory.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 宿舍实体类
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
@Table(name = "dormitories")
public class Dormitory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "宿舍编号不能为空")
    @Size(max = 20, message = "宿舍编号过长")
    @Column(nullable = false, length = 20)
    private String number;

    @NotBlank(message = "所属楼宇不能为空")
    @Size(max = 50, message = "楼宇名称过长")
    @Column(nullable = false, length = 50)
    private String building;

    @NotNull(message = "容量不能为空")
    @Min(value = 1, message = "容量至少为1")
    @Column(nullable = false)
    private Integer capacity;

    @NotNull
    @Min(value = 0, message = "已住人数不能为负")
    @Column(nullable = false)
    private Integer occupied = 0;

    // ... timestamps ...
    @Column(name = "created_at") private LocalDateTime createdAt;
    @Column(name = "updated_at") private LocalDateTime updatedAt;
    @PrePersist protected void onCreate() { createdAt = LocalDateTime.now(); updatedAt = LocalDateTime.now(); }
    @PreUpdate protected void onUpdate() { updatedAt = LocalDateTime.now(); }
}