package com.dormitory.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 报修记录实体类
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
@Table(name = "repairs")
public class Repair {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "报修人不能为空")
    @Size(max = 50)
    @Column(nullable = false, length = 50)
    private String reporter;

    @Size(max = 20)
    @Column(name = "student_id", length = 20)
    private String studentId;

    @NotBlank(message = "宿舍号不能为空")
    @Size(max = 20)
    @Column(nullable = false, length = 20)
    private String dormitory;

    @NotBlank(message = "报修描述不能为空")
    @Size(max = 2000, message = "描述内容过长，请精简") // 安全性：防止超长文本DoS攻击
    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @NotNull(message = "状态不能为空")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RepairStatus status;

    @Column(name = "repair_time")
    private LocalDateTime repairTime;

    // 图片通常存储路径或JSON，TEXT类型
    @Column(columnDefinition = "TEXT")
    private String images;

    // ... timestamps ...
    @Column(name = "created_at") private LocalDateTime createdAt;
    @Column(name = "updated_at") private LocalDateTime updatedAt;
    @PrePersist protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (repairTime == null) repairTime = LocalDateTime.now();
    }
    @PreUpdate protected void onUpdate() { updatedAt = LocalDateTime.now(); }

    public enum RepairStatus {
        PENDING("待处理"), PROCESSING("处理中"), COMPLETED("已完成");
        private final String description;
        RepairStatus(String description) { this.description = description; }
        public String getDescription() { return description; }
    }
}