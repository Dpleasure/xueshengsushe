package com.dormitory.repository;

import com.dormitory.entity.Repair;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 报修记录数据访问层
 */
@Repository
public interface RepairRepository extends JpaRepository<Repair, Long> {
    
    /**
     * 根据报修人查找
     */
    List<Repair> findByReporterContaining(String reporter);
    
    /**
     * 根据宿舍号查找
     */
    List<Repair> findByDormitoryContaining(String dormitory);

    void deleteByDormitory(String dormitory);
    
    /**
     * 根据状态查找
     */
    List<Repair> findByStatus(Repair.RepairStatus status);
    
    /**
     * 根据报修人或宿舍号查找
     */
    @Query("SELECT r FROM Repair r WHERE r.reporter LIKE %:keyword% OR r.dormitory LIKE %:keyword%")
    List<Repair> findByReporterOrDormitoryContaining(@Param("keyword") String keyword);

    /**
     * 根据学号查找报修记录
     */
    List<Repair> findByStudentId(String studentId);
}
