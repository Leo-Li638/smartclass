package com.smartclass.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 管理端新增/编辑用户参数
 */
@Data
public class UserSaveDTO {

    private Long id;

    @NotBlank(message = "用户名不能为空")
    private String username;

    /** 新增时必填,编辑时不传则不修改密码 */
    private String password;

    @NotBlank(message = "姓名不能为空")
    private String realName;

    @NotBlank(message = "角色不能为空")
    private String role;

    private Long clazzId;

    private String phone;

    private String email;

    private String gender;

    private Integer status;
}
