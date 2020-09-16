package com.atguigu.gmall.pms.demo;

import java.lang.annotation.*;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MyTest {

    String prefix() default "myCache";

    String lock() default "lock";


}