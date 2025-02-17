package com.bilibili.web;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * web端的启动入口
 */
@SpringBootApplication(scanBasePackages = {"com.bilibili"})
@MapperScan(basePackages = "com.bilibili.mappers")
public class BilibiliRunApplication {
    public static void main(String[] args) {
        SpringApplication.run(BilibiliRunApplication.class,args);
    }
}
