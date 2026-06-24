package com.dormitory.repository;

import com.dormitory.entity.Visit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 来访登记数据访问层
 */
@Repository
public interface VisitRepository extends JpaRepository<Visit, Long> {

    /**
     * 根据学生学号查找来访申请记录
     */
    List<Visit> findByStudentId(String studentId);

    void deleteByDormitory(String dormitory);
}
