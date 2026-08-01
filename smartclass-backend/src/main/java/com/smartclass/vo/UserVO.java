package com.smartclass.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户列表返回对象(不含密码)
 */
@Data
public class UserVO {

    private Long id;

    private String username;

    private String realName;

    private String role;

    private Long clazzId;

    private String clazzName;

    private String phone;

    private String email;

    private String gender;

    private Integer status;

    private LocalDateTime createTime;
}
