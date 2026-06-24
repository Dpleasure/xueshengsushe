package com.dormitory.repository;

import com.dormitory.entity.Accommodation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 住宿信息数据访问层
 */
@Repository
public interface AccommodationRepository extends JpaRepository<Accommodation, Long> {
    
    /**
     * 根据学生姓名查找住宿信息
     */
    List<Accommodation> findByStudentNameContaining(String studentName);
    
    /**
     * 根据宿舍号查找住宿信息
     */
    List<Accommodation> findByDormitoryContaining(String dormitory);

    void deleteByDormitory(String dormitory);
    
    /**
     * 根据学生姓名或宿舍号查找住宿信息
     */
    @Query("SELECT a FROM Accommodation a WHERE a.studentName LIKE %:studentName% AND a.dormitory LIKE %:dormitory%")
    List<Accommodation> findByStudentNameAndDormitoryContaining(@Param("studentName") String studentName, @Param("dormitory") String dormitory);

    /**
     * 检查学生是否已存在住宿记录
     */
    boolean existsByStudentName(String studentName);

    /**
     * 根据学生姓名精确查找住宿信息
     */
    java.util.Optional<Accommodation> findByStudentName(String studentName);

    /**
     * 根据学号查找住宿信息
     */
    List<Accommodation> findByStudentIdContaining(String studentId);

    /**
     * 根据学号或宿舍号查找住宿信息
     */
    @Query("SELECT a FROM Accommodation a WHERE a.studentId LIKE %:studentId% AND a.dormitory LIKE %:dormitory%")
    List<Accommodation> findByStudentIdAndDormitoryContaining(@Param("studentId") String studentId, @Param("dormitory") String dormitory);

    /**
     * 根据学号精确查找住宿信息
     */
    java.util.Optional<Accommodation> findByStudentId(String studentId);

    void deleteByStudentId(String studentId);

    void deleteByStudentName(String studentName);

}
