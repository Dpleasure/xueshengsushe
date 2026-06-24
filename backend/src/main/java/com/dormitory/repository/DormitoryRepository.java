package com.dormitory.repository;

import com.dormitory.entity.Dormitory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 宿舍数据访问层
 */
@Repository
public interface DormitoryRepository extends JpaRepository<Dormitory, Long> {
    
    /**
     * 根据宿舍号查找
     */
    List<Dormitory> findByNumberContaining(String number);

    /**
     * 根据宿舍号精确查找
     */
    Optional<Dormitory> findByNumber(String number);

    boolean existsByNumber(String number);
    
    /**
     * 根据宿舍楼查找
     */
    List<Dormitory> findByBuilding(String building);
    
    /**
     * 根据宿舍号或宿舍楼查找
     */
    @Query("SELECT d FROM Dormitory d WHERE d.number LIKE %:keyword% OR d.building LIKE %:keyword%")
    List<Dormitory> findByNumberOrBuildingContaining(@Param("keyword") String keyword);
    
    /**
     * 统计宿舍楼中的宿舍数量
     */
    @Query("SELECT COUNT(d) FROM Dormitory d WHERE d.building = :building")
    long countByBuilding(@Param("building") String building);

    long countByBuildingAndOccupiedGreaterThan(String building, Integer occupied);

    boolean existsByBuildingAndOccupiedGreaterThan(String building, Integer occupied);
}
