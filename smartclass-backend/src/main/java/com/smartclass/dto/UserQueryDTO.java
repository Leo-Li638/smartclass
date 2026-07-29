package com.smartclass.dto;

import lombok.Data;

/**
 * 用户分页查询参数
 */
@Data
public class UserQueryDTO {

    private Long current = 1L;

    private Long size = 10L;

    private String keyword;

    private String role;

    private Long clazzId;

    private Integer status;
}
