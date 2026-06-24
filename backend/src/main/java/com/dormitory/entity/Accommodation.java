package com.dormitory.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 住宿信息实体类
 */
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "accommodations")
public class Accommodation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "学生姓名不能为空")
    @Size(max = 50, message = "姓名过长")
    @Column(name = "student_name", nullable = false, length = 50)
    private String studentName;

    @Size(max = 20, message = "学号过长")
    @Column(name = "student_id", length = 20)
    private String studentId;

    @NotBlank(message = "宿舍号不能为空")
    @Size(max = 20, message = "宿舍号过长")
    @Column(nullable = false, length = 20)
    private String dormitory;

    @NotBlank(message = "床位号不能为空")
    @Size(max = 20, message = "床位号过长")
    @Column(nullable = false, length = 20)
    private String bed;

    @Size(max = 50, message = "楼宇名称过长")
    @Column(name = "building", length = 50)
    private String building;

    @Column(name = "check_in_date")
    private LocalDate checkInDate;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ... @PrePersist, @PreUpdate ...
    @PrePersist
    protected void onCreate() { createdAt = LocalDateTime.now(); updatedAt = LocalDateTime.now(); }
    @PreUpdate
    protected void onUpdate() { updatedAt = LocalDateTime.now(); }
}
