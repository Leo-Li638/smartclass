package com.smartclass.vo;

import lombok.Builder;
import lombok.Data;

/**
 * 登录成功返回:token + 基础用户信息
 */
@Data
@Builder
public class LoginVO {

    private String token;

    private Long userId;

    private String username;

    private String realName;

    private String role;

    private String avatarText;
}
