package com.smartclass.controller;

import com.smartclass.common.Result;
import com.smartclass.service.NoticeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 公共接口:各角色可见的系统公告
 */
@RestController
@RequestMapping("/api/common")
@RequiredArgsConstructor
public class CommonController {

    private final NoticeService noticeService;

    @GetMapping("/notices")
    public Result<?> notices() {
        return Result.success(noticeService.listForCurrent());
    }
}
