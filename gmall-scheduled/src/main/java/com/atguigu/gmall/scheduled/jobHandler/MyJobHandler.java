package com.atguigu.gmall.scheduled.jobHandler;


import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.annotation.XxlJob;
import com.xxl.job.core.log.XxlJobLogger;
import org.springframework.stereotype.Component;

@Component
public class MyJobHandler {

    @XxlJob("myJobHandler")
    public ReturnT<String> test(String param) {
        XxlJobLogger.log("日志文件  " + param);
        System.out.println("这是我第一个xxljob任务=" + System.currentTimeMillis() + "  调度中心可以传递的参数=" + param);
        return ReturnT.SUCCESS;
    }
}
