package com.smartclass.controller;

import com.smartclass.common.Result;
import com.smartclass.entity.Course;
import com.smartclass.entity.CourseMaterial;
import com.smartclass.service.CourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 课程接口:师生查看课程,教师/管理员维护课程与学习资料
 */
@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;

    @GetMapping
    public Result<?> list(@RequestParam(required = false) String keyword,
                          @RequestParam(required = false) String grade) {
        return Result.success(courseService.list(keyword, grade));
    }

    @GetMapping("/mine")
    public Result<?> mine() {
        return Result.success(courseService.myCourses());
    }

    @PostMapping
    public Result<Void> save(@RequestBody Course course) {
        courseService.save(course);
        return Result.success();
    }

    @PostMapping("/{id}/status/{status}")
    public Result<Void> changeStatus(@PathVariable Long id, @PathVariable Integer status) {
        courseService.changeStatus(id, status);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        courseService.delete(id);
        return Result.success();
    }

    @GetMapping("/{id}/materials")
    public Result<?> materials(@PathVariable Long id) {
        return Result.success(courseService.materials(id));
    }

    @PostMapping("/materials")
    public Result<Void> saveMaterial(@RequestBody CourseMaterial material) {
        courseService.saveMaterial(material);
        return Result.success();
    }

    @DeleteMapping("/materials/{id}")
    public Result<Void> deleteMaterial(@PathVariable Long id) {
        courseService.deleteMaterial(id);
        return Result.success();
    }
}
