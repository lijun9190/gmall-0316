package com.atguigu.gmall.cart.service;



import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

@Service
public class AsyncTest {
    @Async
    public void executor1(Integer a, Integer b) {
        try {
            System.out.println("异步方法executor1开始执行" + Thread.currentThread().getName());
            TimeUnit.SECONDS.sleep(5);
            int i = 1 / 0;
            System.out.println("异步方法executor1结束执行。。。。");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Async
    public void executor2(int a, int b) {
        try {
            System.out.println("异步方法executor2开始执行");
            TimeUnit.SECONDS.sleep(4);
            int i = 1 / 0;
            System.out.println("异步方法executor2结束执行。。。。");
        } catch (InterruptedException e) {

            System.out.println("service方法中捕获异常后的打印：" + e.getMessage());
        }
    }


}
