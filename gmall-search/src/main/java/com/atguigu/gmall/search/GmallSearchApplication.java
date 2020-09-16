package com.atguigu.gmall.search;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients
@SpringBootApplication
public class GmallSearchApplication {

    public static void main(String[] args) {
        SpringApplication.run(GmallSearchApplication.class, args);
    }

    @Test
    public void run(){
        String str="8G-16G-32G";
        String[] split = StringUtils.split(str, "-");
        System.out.println(split[0]);
        System.out.println(split[1]);
        System.out.println(split[2]);

    }
}
