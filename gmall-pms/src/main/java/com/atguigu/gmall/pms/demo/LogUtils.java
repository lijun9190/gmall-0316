package com.atguigu.gmall.pms.demo;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.Test;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Random;

@Component
@Aspect
public class LogUtils {

    @Around("@annotation(MyTest)")
    public void logBefore(ProceedingJoinPoint joinPoint) throws Throwable {
        Object[] args = joinPoint.getArgs();
        System.out.println(Arrays.asList(args));
        System.out.println("目标类="+joinPoint.getTarget().getClass());
        Signature signature = joinPoint.getSignature();

        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        Method method = methodSignature.getMethod();
        MyTest annotation = method.getAnnotation(MyTest.class);
        String prefix = annotation.prefix();
        joinPoint.proceed(joinPoint.getArgs());

        String lock = annotation.lock();
        System.out.println("signature = " + signature);
        System.out.println("methodSignature = " + methodSignature);
        System.out.println("annotation = " + annotation);
        System.out.println("prefix = " + prefix);
        System.out.println("lock = " + lock);
    }

    @Test
    public void test01(){
        System.out.println(new Random().nextInt(10));
    }
}