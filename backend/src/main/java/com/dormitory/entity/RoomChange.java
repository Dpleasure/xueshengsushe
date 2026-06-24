package com.dormitory.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 换寝记录实体类
 * 支持两种模式：
 * 1. 单个学生换寝：studentB 为空
 * 2. 两个学生互换：studentA 和 studentB 都有值
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
@Table(name = "room_changes")
public class RoomChange {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 50)
    @Column(name = "student_a", nullable = false, length = 50)
    private String studentA;

    @Size(max = 50)
    @Column(name = "student_b", length = 50)
    private String studentB;

    @NotBlank
    @Size(max = 20)
    @Column(name = "old_dormitory_a", nullable = false, length = 20)
    private String oldDormitoryA;

    @NotBlank
    @Size(max = 20)
    @Column(name = "old_bed_a", nullable = false, length = 20)
    private String oldBedA;

    @NotBlank
    @Size(max = 20)
    @Column(name = "new_dormitory_a", nullable = false, length = 20)
    private String newDormitoryA;

    @NotBlank
    @Size(max = 20)
    @Column(name = "new_bed_a", nullable = false, length = 20)
    private String newBedA;

    // B可能是空的（如果是单人申请换空床位），所以只加Size不加NotBlank
    @Size(max = 20)
    @Column(name = "old_dormitory_b", length = 20)
    private String oldDormitoryB;

    @Size(max = 20)
    @Column(name = "old_bed_b", length = 20)
    private String oldBedB;

    @Size(max = 20)
    @Column(name = "new_dormitory_b", length = 20)
    private String newDormitoryB;

    @Size(max = 20)
    @Column(name = "new_bed_b", length = 20)
    private String newBedB;

    @Column(name = "change_time")
    private LocalDateTime changeTime;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ChangeStatus status = ChangeStatus.PENDING;

    // ... timestamps ...
    @Column(name = "created_at") private LocalDateTime createdAt;
    @Column(name = "updated_at") private LocalDateTime updatedAt;
    @PrePersist protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (changeTime == null) changeTime = LocalDateTime.now();
    }
    @PreUpdate protected void onUpdate() { updatedAt = LocalDateTime.now(); }

    public enum ChangeStatus {
        PENDING("待处理"), APPROVED("已同意"), REJECTED("已拒绝");
        private final String description;
        ChangeStatus(String description) { this.description = description; }
        public String getDescription() { return description; }
    }
}
