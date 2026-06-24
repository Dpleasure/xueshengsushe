package com.dormitory.repository;

import com.dormitory.entity.RoomChange;
import com.dormitory.entity.RoomChange.ChangeStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 换寝记录数据访问层
 */
@Repository
public interface RoomChangeRepository extends JpaRepository<RoomChange, Long> {
    
    /**
     * 根据学生A查找换寝记录
     */
    List<RoomChange> findByStudentAContaining(String studentA);
    
    /**
     * 根据学生B查找换寝记录
     */
    List<RoomChange> findByStudentBContaining(String studentB);
    
    /**
     * 根据学生A或学生B查找换寝记录
     */
    List<RoomChange> findByStudentAContainingOrStudentBContaining(String studentA, String studentB);

    /**
     * 检查指定学生是否有特定状态的换寝申请
     */
    boolean existsByStudentAAndStatus(String studentA, ChangeStatus status);
}
