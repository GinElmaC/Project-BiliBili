package com.bilibili.admin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * admin管理端的入口
 */
@SpringBootApplication(scanBasePackages = {"com.bilibili"})
public class BilibiliAdminRunApplication {
    public static void main(String[] args) {
        SpringApplication.run(BilibiliAdminRunApplication.class,args);
    }
}
