package com.atguigu.gmall.gateway;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;

@SpringBootTest
class GmallGatewayApplicationTests {

    @Test
    void contextLoads() {
    }

    public static void main(String[] args) {
        List<String> str = new ArrayList<>();
        str.add("/aa");
        str.add("/bb");
        str.add("/cc");
        String path = "/aa/bb/cc";
        if (str.stream().allMatch(s -> path.startsWith(s))) {
            System.out.println("匹配成功");
        } else {
            System.out.println("匹配失败");
        }
    }

}
