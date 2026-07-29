package com.smartclass.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 修改密码参数
 */
@Data
public class PasswordDTO {

    @NotBlank(message = "原密码不能为空")
    private String oldPassword;

    @NotBlank(message = "新密码不能为空")
    private String newPassword;
}
