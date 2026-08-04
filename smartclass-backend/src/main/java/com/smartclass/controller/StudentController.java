package com.smartclass.controller;

import com.smartclass.common.Result;
import com.smartclass.dto.PlanGenerateDTO;
import com.smartclass.dto.PracticeSubmitDTO;
import com.smartclass.dto.SubmitDTO;
import com.smartclass.service.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 学生端接口:作业、自主练习、错题本、智能推题、学习计划、看板
 */
@RestController
@RequestMapping("/api/student")
@RequiredArgsConstructor
public class StudentController {

    private final HomeworkService homeworkService;
    private final PracticeService practiceService;
    private final WrongBookService wrongBookService;
    private final RecommendService recommendService;
    private final PlanService planService;
    private final StatsService statsService;
    private final SubjectService subjectService;

    // ==================== 作业 ====================

    @GetMapping("/homeworks")
    public Result<?> homeworks(@RequestParam(defaultValue = "1") Long current,
                               @RequestParam(defaultValue = "10") Long size,
                               @RequestParam(required = false) Integer status) {
        return Result.success(homeworkService.studentPage(current, size, status));
    }

    @GetMapping("/homeworks/{id}")
    public Result<?> homeworkDetail(@PathVariable Long id) {
        return Result.success(homeworkService.studentDetail(id));
    }

    @PostMapping("/homeworks/submit")
    public Result<?> submit(@Valid @RequestBody SubmitDTO dto) {
        return Result.success(homeworkService.submit(dto));
    }

    @GetMapping("/homeworks/{id}/my-submit")
    public Result<?> mySubmit(@PathVariable Long id) {
        return Result.success(homeworkService.mySubmit(id));
    }

    @GetMapping("/submits/{id}")
    public Result<?> submitDetail(@PathVariable Long id) {
        return Result.success(homeworkService.submitDetail(id));
    }

    // ==================== 自主练习 ====================

    @GetMapping("/practice")
    public Result<?> practice(@RequestParam(required = false) Long subjectId,
                              @RequestParam(required = false) Integer difficulty,
                              @RequestParam(required = false) Integer count) {
        return Result.success(practiceService.start(subjectId, difficulty, count));
    }

    @PostMapping("/practice/submit")
    public Result<?> practiceSubmit(@Valid @RequestBody PracticeSubmitDTO dto) {
        return Result.success(practiceService.submit(dto));
    }

    // ==================== 错题本 ====================

    @GetMapping("/wrong-book")
    public Result<?> wrongBook(@RequestParam(required = false) Long subjectId,
                               @RequestParam(required = false) Integer mastered) {
        return Result.success(wrongBookService.myList(subjectId, mastered));
    }

    @PostMapping("/wrong-book/redo")
    public Result<?> redo(@Valid @RequestBody PracticeSubmitDTO dto) {
        return Result.success(wrongBookService.redo(dto));
    }

    @PostMapping("/wrong-book/{id}/mastered")
    public Result<Void> markMastered(@PathVariable Long id) {
        wrongBookService.markMastered(id);
        return Result.success();
    }

    @DeleteMapping("/wrong-book/{id}")
    public Result<Void> removeWrong(@PathVariable Long id) {
        wrongBookService.remove(id);
        return Result.success();
    }

    // ==================== 智能推题 ====================

    @GetMapping("/recommend/knowledge-stats")
    public Result<?> knowledgeStats(@RequestParam(required = false) Long subjectId) {
        return Result.success(recommendService.analyze(subjectId));
    }

    @GetMapping("/recommend")
    public Result<?> recommend(@RequestParam(required = false) Long subjectId,
                               @RequestParam(required = false) Integer count) {
        return Result.success(recommendService.recommend(subjectId, count));
    }

    // ==================== 学习计划 ====================

    @PostMapping("/plans")
    public Result<?> generatePlan(@Valid @RequestBody PlanGenerateDTO dto) {
        return Result.success(planService.generate(dto));
    }

    @GetMapping("/plans")
    public Result<?> plans() {
        return Result.success(planService.myPlans());
    }

    @GetMapping("/plans/{id}")
    public Result<?> planDetail(@PathVariable Long id) {
        return Result.success(planService.planDetail(id));
    }

    @DeleteMapping("/plans/{id}")
    public Result<Void> deletePlan(@PathVariable Long id) {
        planService.delete(id);
        return Result.success();
    }

    // ==================== 看板与基础数据 ====================

    @GetMapping("/dashboard")
    public Result<?> dashboard() {
        return Result.success(statsService.studentDashboard());
    }

    @GetMapping("/subjects")
    public Result<?> subjects() {
        return Result.success(subjectService.list());
    }
}
