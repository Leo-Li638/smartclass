package com.smartclass;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 智学云课堂 K12 教学系统启动类
 */
@SpringBootApplication
@MapperScan("com.smartclass.mapper")
public class SmartClassApplication {

    public static void main(String[] args) {
        SpringApplication.run(SmartClassApplication.class, args);
    }
}
