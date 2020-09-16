package com.atguigu.gmall.item;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import springfox.documentation.swagger2.annotations.EnableSwagger2;


@EnableFeignClients
@SpringBootApplication
@EnableSwagger2
@MapperScan("com.atguigu.gmall.ums.mapper")
public class GmallItemApplication {

    public static void main(String[] args) {
        System.out.println();
        SpringApplication.run(GmallItemApplication.class, args);
    }

}
