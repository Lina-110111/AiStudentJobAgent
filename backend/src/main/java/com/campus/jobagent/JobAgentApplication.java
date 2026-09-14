package com.campus.jobagent;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * AI 大学生就业服务智能体 —— 后端启动类。
 *
 * <p>课程设计：2024 级《软件工程》课程设计，题目《基于AI的大学生就业服务智能体开发》。
 */
@EnableAsync
@MapperScan("com.campus.jobagent.modules.**.mapper")
@SpringBootApplication
public class JobAgentApplication {

    public static void main(String[] args) {
        SpringApplication.run(JobAgentApplication.class, args);
    }
}
