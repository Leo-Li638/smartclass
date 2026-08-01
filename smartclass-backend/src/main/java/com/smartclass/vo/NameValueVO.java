package com.smartclass.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 通用图表数据点
 */
@Data
@AllArgsConstructor
public class NameValueVO {

    private String name;

    private Object value;
}
