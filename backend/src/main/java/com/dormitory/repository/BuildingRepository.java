package com.dormitory.repository;

import com.dormitory.entity.Building;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 宿舍楼数据访问层
 */
@Repository
public interface BuildingRepository extends JpaRepository<Building, Long> {

    boolean existsByName(String name);

    Optional<Building> findByName(String name);

    /**
     * 根据名称查找宿舍楼
     */
    List<Building> findByNameContaining(String name);

    /**
     * 根据位置查找宿舍楼
     */
    List<Building> findByLocationContaining(String location);

    /**
     * 根据名称或位置查找宿舍楼
     */
    @Query("SELECT b FROM Building b WHERE b.name LIKE %:keyword% OR b.location LIKE %:keyword%")
    List<Building> findByNameOrLocationContaining(@Param("keyword") String keyword);
}
