package com.atguigu.gmall.cart;

import org.apache.commons.lang.StringUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

@SpringBootTest
class GmallCartApplicationTests {


    @Autowired
    StringRedisTemplate redisTemplate;

    @Test
    void contextLoads() {
        List<String> list=new ArrayList();
        if (CollectionUtils.isEmpty(list)) {
            System.out.println(list.size());
        }
    }



}
