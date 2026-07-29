package com.smartclass.controller;

import com.smartclass.common.Result;
import com.smartclass.dto.LoginDTO;
import com.smartclass.dto.PasswordDTO;
import com.smartclass.dto.RegisterDTO;
import com.smartclass.entity.User;
import com.smartclass.service.AuthService;
import com.smartclass.service.ClazzService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 认证接口:登录、注册、个人信息
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final ClazzService clazzService;

    @PostMapping("/login")
    public Result<?> login(@Valid @RequestBody LoginDTO dto) {
        return Result.success(authService.login(dto));
    }

    @PostMapping("/register")
    public Result<Void> register(@Valid @RequestBody RegisterDTO dto) {
        authService.register(dto);
        return Result.success();
    }

    @GetMapping("/classes")
    public Result<?> classes() {
        return Result.success(clazzService.list());
    }

    @GetMapping("/info")
    public Result<?> info() {
        User user = authService.currentUser();
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("id", user.getId());
        data.put("username", user.getUsername());
        data.put("realName", user.getRealName());
        data.put("role", user.getRole());
        data.put("clazzId", user.getClazzId());
        data.put("clazzName", user.getClazzId() == null ? null
                : clazzService.nameMap().get(user.getClazzId()));
        data.put("phone", user.getPhone());
        data.put("email", user.getEmail());
        data.put("gender", user.getGender());
        return Result.success(data);
    }

    @PostMapping("/password")
    public Result<Void> changePassword(@Valid @RequestBody PasswordDTO dto) {
        authService.changePassword(dto);
        return Result.success();
    }
}
