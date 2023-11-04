package com.xuecheng;

import com.spring4all.swagger.EnableSwagger2Doc;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author fantasy
 * @description 内容管理服务启动类
 * @date 2023-11-03
 */
@SpringBootApplication
@EnableSwagger2Doc
public class ContentApplication {
    public static void main(String[] args) {
        SpringApplication.run(ContentApplication.class);
        System.out.println("swagger文档：http://localhost:63040/content/swagger-ui.html");
    }
}
