package com.atguigu.gmall.scheduled.jdk;

import java.util.Timer;
import java.util.TimerTask;

public class TimerDemo {
    public static void main(String[] args) {
        System.out.println("任务开始时间="+System.currentTimeMillis());
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                System.out.println("任务结束时间="+System.currentTimeMillis());
            }
        }, 10000,1000);
    }
}
