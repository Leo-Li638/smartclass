package com.smartclass.controller;

import com.smartclass.common.Result;
import com.smartclass.dto.HomeworkSaveDTO;
import com.smartclass.dto.QuestionQueryDTO;
import com.smartclass.dto.QuestionSaveDTO;
import com.smartclass.entity.HomeworkSubmit;
import com.smartclass.entity.KnowledgePoint;
import com.smartclass.service.ClazzService;
import com.smartclass.service.HomeworkService;
import com.smartclass.service.QuestionService;
import com.smartclass.service.StatsService;
import com.smartclass.service.SubjectService;
import com.smartclass.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 教师端接口:题库、作业、学情分析
 */
@RestController
@RequestMapping("/api/teacher")
@RequiredArgsConstructor
public class TeacherController {

    private final QuestionService questionService;
    private final HomeworkService homeworkService;
    private final StatsService statsService;
    private final SubjectService subjectService;
    private final ClazzService clazzService;
    private final UserService userService;

    // ==================== 基础数据(学科/班级,教师只读) ====================

    @GetMapping("/subjects")
    public Result<?> subjects() {
        return Result.success(subjectService.list());
    }

    @GetMapping("/classes")
    public Result<?> classes() {
        return Result.success(clazzService.list());
    }

    @GetMapping("/students")
    public Result<?> students() {
        return Result.success(userService.students());
    }

    @GetMapping("/questions")
    public Result<?> questions(QuestionQueryDTO dto) {
        return Result.success(questionService.page(dto));
    }

    @GetMapping("/questions/list")
    public Result<?> questionList(@RequestParam(required = false) Long subjectId) {
        return Result.success(questionService.listBySubject(subjectId));
    }

    @GetMapping("/questions/{id}")
    public Result<?> questionDetail(@PathVariable Long id) {
        return Result.success(questionService.detail(id));
    }

    @PostMapping("/questions")
    public Result<Void> saveQuestion(@Valid @RequestBody QuestionSaveDTO dto) {
        questionService.save(dto);
        return Result.success();
    }

    @DeleteMapping("/questions/{id}")
    public Result<Void> deleteQuestion(@PathVariable Long id) {
        questionService.delete(id);
        return Result.success();
    }

    @GetMapping("/knowledge")
    public Result<?> knowledge(@RequestParam(required = false) Long subjectId) {
        return Result.success(questionService.knowledgeList(subjectId));
    }

    @PostMapping("/knowledge")
    public Result<Void> saveKnowledge(@RequestBody KnowledgePoint knowledgePoint) {
        questionService.saveKnowledge(knowledgePoint);
        return Result.success();
    }

    // ==================== 作业 ====================

    @GetMapping("/homeworks")
    public Result<?> homeworks(@RequestParam(defaultValue = "1") Long current,
                               @RequestParam(defaultValue = "10") Long size) {
        return Result.success(homeworkService.teacherPage(current, size));
    }

    @PostMapping("/homeworks")
    public Result<Void> saveHomework(@Valid @RequestBody HomeworkSaveDTO dto) {
        homeworkService.save(dto);
        return Result.success();
    }

    @PostMapping("/homeworks/{id}/publish")
    public Result<Void> publish(@PathVariable Long id) {
        homeworkService.publish(id);
        return Result.success();
    }

    @DeleteMapping("/homeworks/{id}")
    public Result<Void> deleteHomework(@PathVariable Long id) {
        homeworkService.delete(id);
        return Result.success();
    }

    @GetMapping("/homeworks/{id}/submits")
    public Result<?> submits(@PathVariable Long id) {
        List<HomeworkSubmit> submits = homeworkService.submitList(id);
        return Result.success(submits);
    }

    @PostMapping("/submits/{id}/grade")
    public Result<Void> grade(@PathVariable Long id, @RequestBody Map<String, String> body) {
        homeworkService.grade(id, body.get("comment"));
        return Result.success();
    }

    @GetMapping("/submits/{id}")
    public Result<?> submitDetail(@PathVariable Long id) {
        return Result.success(homeworkService.submitDetail(id));
    }

    // ==================== 学情分析 ====================

    @GetMapping("/homeworks/{id}/knowledge-analysis")
    public Result<?> knowledgeAnalysis(@PathVariable Long id) {
        return Result.success(statsService.homeworkKnowledgeAnalysis(id));
    }

    @GetMapping("/analysis/class-overview")
    public Result<?> classOverview() {
        return Result.success(statsService.teacherClassOverview());
    }
}
