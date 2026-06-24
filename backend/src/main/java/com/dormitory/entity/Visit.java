package com.dormitory.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 来访登记实体类
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
@Table(name = "visits")
public class Visit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "访问宿舍不能为空")
    @Size(max = 20)
    @Column(nullable = false, length = 20)
    private String dormitory;

    @NotBlank(message = "访客姓名不能为空")
    @Size(max = 50)
    @Column(name = "visitor_name", nullable = false, length = 50)
    private String visitorName;

    @Size(max = 20)
    @Column(name = "student_id", length = 20)
    private String studentId;

    @Size(max = 50)
    @Column(name = "student_name", length = 50)
    private String studentName;

    @NotBlank(message = "访问事由不能为空")
    @Size(max = 500, message = "事由不能超过500字")
    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "visit_time")
    private LocalDateTime visitTime;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VisitStatus status;

    // ... timestamps ...
    @Column(name = "created_at") private LocalDateTime createdAt;
    @Column(name = "updated_at") private LocalDateTime updatedAt;
    @PrePersist protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (visitTime == null) visitTime = LocalDateTime.now();
    }
    @PreUpdate protected void onUpdate() { updatedAt = LocalDateTime.now(); }

    public enum VisitStatus { PENDING, APPROVED, REJECTED }
}
