package com.atguigu.gmall.pms.service.impl;

import com.alibaba.druid.sql.visitor.SQLASTOutputVisitor;
import com.atguigu.gmall.pms.mapper.SkuAttrValueMapper;
import com.atguigu.gmall.pms.service.SkuAttrValueService;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class SkuAttrValueServiceImplTest {


    @Autowired
    SkuAttrValueService skuAttrValueService;


    @Test
    void querySaleAttrValuesMappingSkuIdBySpuId() {
        System.out.println(skuAttrValueService.querySaleAttrValuesMappingSkuIdBySpuId(7L));

    }

}