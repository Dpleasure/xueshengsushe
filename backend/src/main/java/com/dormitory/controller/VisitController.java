package com.dormitory.controller;

import com.dormitory.Json.RestBean;
import com.dormitory.entity.Visit;
import com.dormitory.service.VisitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 来访登记控制器
 */
@RestController
@RequestMapping("/api/visits")
@CrossOrigin(origins = "*")
public class VisitController {

    @Autowired
    private VisitService visitService;

    /**
     * 获取所有来访记录 (管理员)
     */
    @GetMapping
    public RestBean<List<Visit>> getAllVisits() {
        return RestBean.success(visitService.getAllVisits());
    }

    /**
     * 根据学生学号获取来访记录 (学生)
     */
    @GetMapping("/student/{studentId}")
    public RestBean<List<Visit>> getVisitsByStudentId(@PathVariable String studentId) {
        return RestBean.success(visitService.getVisitsByStudentId(studentId));
    }

    /**
     * 提交来访申请 (学生)
     */
    @PostMapping("/apply")
    public RestBean<Visit> applyForVisit(@RequestBody Visit visit) {
        return RestBean.success(visitService.applyForVisit(visit));
    }

    /**
     * 同意来访申请 (管理员)
     */
    @PostMapping("/{id}/approve")
    public RestBean<Visit> approveVisit(@PathVariable Long id) {
        try {
            Visit updatedVisit = visitService.approveVisit(id);
            return RestBean.success(updatedVisit);
        } catch (Exception e) {
            return RestBean.failure(e.getMessage());
        }
    }

    /**
     * 拒绝来访申请 (管理员)
     */
    @PostMapping("/{id}/reject")
    public RestBean<Visit> rejectVisit(@PathVariable Long id) {
        try {
            Visit updatedVisit = visitService.rejectVisit(id);
            return RestBean.success(updatedVisit);
        } catch (Exception e) {
            return RestBean.failure(e.getMessage());
        }
    }

    /**
     * 删除来访记录 (管理员)
     */
    @DeleteMapping("/{id}")
    public RestBean<Void> deleteVisit(@PathVariable Long id) {
        visitService.deleteVisit(id);
        return RestBean.success();
    }
}
