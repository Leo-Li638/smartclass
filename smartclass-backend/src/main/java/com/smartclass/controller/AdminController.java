package com.smartclass.controller;

import com.smartclass.common.Result;
import com.smartclass.dto.UserQueryDTO;
import com.smartclass.dto.UserSaveDTO;
import com.smartclass.entity.Clazz;
import com.smartclass.entity.Notice;
import com.smartclass.entity.Subject;
import com.smartclass.service.ClazzService;
import com.smartclass.service.NoticeService;
import com.smartclass.service.StatsService;
import com.smartclass.service.SubjectService;
import com.smartclass.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 管理端接口:看板、用户、班级、学科、公告
 */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserService userService;
    private final ClazzService clazzService;
    private final SubjectService subjectService;
    private final NoticeService noticeService;
    private final StatsService statsService;

    @GetMapping("/dashboard")
    public Result<?> dashboard() {
        return Result.success(statsService.adminDashboard());
    }

    // ==================== 用户管理 ====================

    @GetMapping("/users")
    public Result<?> users(UserQueryDTO dto) {
        return Result.success(userService.page(dto));
    }

    @PostMapping("/users")
    public Result<Void> saveUser(@Valid @RequestBody UserSaveDTO dto) {
        userService.save(dto);
        return Result.success();
    }

    @PostMapping("/users/{id}/reset-password")
    public Result<Void> resetPassword(@PathVariable Long id, @RequestBody java.util.Map<String, String> body) {
        userService.resetPassword(id, body.get("password"));
        return Result.success();
    }

    @PostMapping("/users/{id}/status/{status}")
    public Result<Void> changeStatus(@PathVariable Long id, @PathVariable Integer status) {
        userService.changeStatus(id, status);
        return Result.success();
    }

    @DeleteMapping("/users/{id}")
    public Result<Void> deleteUser(@PathVariable Long id) {
        userService.delete(id);
        return Result.success();
    }

    @GetMapping("/teachers")
    public Result<?> teachers() {
        return Result.success(userService.teachers());
    }

    // ==================== 班级管理 ====================

    @GetMapping("/classes")
    public Result<?> classes() {
        return Result.success(clazzService.list());
    }

    @PostMapping("/classes")
    public Result<Void> saveClass(@RequestBody Clazz clazz) {
        clazzService.save(clazz);
        return Result.success();
    }

    @DeleteMapping("/classes/{id}")
    public Result<Void> deleteClass(@PathVariable Long id) {
        clazzService.delete(id);
        return Result.success();
    }

    // ==================== 学科管理 ====================

    @GetMapping("/subjects")
    public Result<?> subjects() {
        return Result.success(subjectService.list());
    }

    @PostMapping("/subjects")
    public Result<Void> saveSubject(@RequestBody Subject subject) {
        subjectService.save(subject);
        return Result.success();
    }

    @DeleteMapping("/subjects/{id}")
    public Result<Void> deleteSubject(@PathVariable Long id) {
        subjectService.delete(id);
        return Result.success();
    }

    // ==================== 公告管理 ====================

    @GetMapping("/notices")
    public Result<?> notices() {
        return Result.success(noticeService.listAll());
    }

    @PostMapping("/notices")
    public Result<Void> saveNotice(@RequestBody Notice notice) {
        noticeService.save(notice);
        return Result.success();
    }

    @DeleteMapping("/notices/{id}")
    public Result<Void> deleteNotice(@PathVariable Long id) {
        noticeService.delete(id);
        return Result.success();
    }
}
