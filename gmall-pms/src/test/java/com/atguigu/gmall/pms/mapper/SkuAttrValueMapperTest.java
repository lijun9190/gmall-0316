package com.atguigu.gmall.pms.mapper;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.jws.Oneway;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class SkuAttrValueMapperTest {


    @Autowired
    SkuAttrValueMapper skuAttrValueMapper;
    @Test
    void querySaleAttrValuesMappingSkuIdBySpuId() {
        List<Map<String, Object>> maps = skuAttrValueMapper.querySaleAttrValuesMappingSkuIdBySpuId(7L);
        System.out.println("maps = " + maps);
    }
}