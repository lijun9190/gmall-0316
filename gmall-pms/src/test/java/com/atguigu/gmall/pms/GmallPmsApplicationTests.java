package com.atguigu.gmall.pms;

import com.atguigu.gmall.pms.demo.Calcaulator;
import com.atguigu.gmall.pms.demo.Calculate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Random;

@SpringBootTest
class GmallPmsApplicationTests {

    @Autowired
    Calculate calculate;
    @Test
    void contextLoads() {
        System.out.println("calculate = " + calculate);
        calculate.add(100, 200);
    }




}
