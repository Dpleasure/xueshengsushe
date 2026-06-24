package com.dormitory.controller;

import com.dormitory.Json.RestBean;
import com.dormitory.entity.User;
import com.dormitory.service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 学生控制器
 */
@RestController
@RequestMapping("/students")
@CrossOrigin(origins = "*")
public class StudentController {
    
    @Autowired
    private StudentService studentService;
    
    /**
     * 获取所有学生
     */
    @GetMapping
    public RestBean<List<User>> getAllStudents() {
        return RestBean.success(studentService.getAllStudents());
    }
    
    /**
     * 根据ID获取学生
     */
    @GetMapping("/{id}")
    public RestBean<User> getStudentById(@PathVariable Long id) {
        return studentService.getStudentById(id)
                .map(RestBean::success)
                .orElse(RestBean.failure("学生不存在"));
    }
    
    /**
     * 搜索学生
     */
    @GetMapping("/search")
    public RestBean<List<User>> searchStudents(@RequestParam(required = false) String keyword) {
        return RestBean.success(studentService.searchStudents(keyword));
    }
    
    /**
     * 添加学生
     */
    @PostMapping
    public RestBean<User> addStudent(@RequestBody User student) {
        return RestBean.success(studentService.addStudent(student));
    }
    
    /**
     * 更新学生
     */
    @PutMapping("/{id}")
    public RestBean<User> updateStudent(@PathVariable Long id, @RequestBody User student) {
        student.setId(id);
        return RestBean.success(studentService.updateStudent(student));
    }
    
    /**
     * 删除学生
     */
    @DeleteMapping("/{id}")
    public RestBean<String> deleteStudent(@PathVariable Long id) {
        studentService.deleteStudent(id);
        return RestBean.success("删除成功");
    }
    
    /**
     * 批量删除学生
     */
    @DeleteMapping("/batch")
    public RestBean<String> deleteStudents(@RequestBody List<Long> ids) {
        studentService.deleteStudents(ids);
        return RestBean.success("批量删除成功");
    }
}
