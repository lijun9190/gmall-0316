package com.atguigu.gmall.pms.demo;


import org.springframework.stereotype.Component;

@Component
public class Calcaulator implements Calculate {
    @Override
    @MyTest(prefix = "123",lock = "locked")
    public Integer add(Integer a, Integer b) {
        System.out.println("瓦达西瓦");
        return null;
    }
}
