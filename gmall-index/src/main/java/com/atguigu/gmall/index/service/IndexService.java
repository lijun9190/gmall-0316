package com.atguigu.gmall.index.service;


import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.index.feign.GmallPmsClient;
import com.atguigu.gmall.pms.entity.CategoryEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class IndexService {

    @Autowired
    GmallPmsClient gmallPmsClient;

    @Autowired
    StringRedisTemplate redisTemplate;

    public List<CategoryEntity> queryLv1Categories() {
        ResponseVo<List<CategoryEntity>> listResponseVo = gmallPmsClient.queryCategory(0L);
        return listResponseVo.getData();
    }

    public List<CategoryEntity> queryLvl2CategoriesWithSub(Long pid) {
        ResponseVo<List<CategoryEntity>> listResponseVo = gmallPmsClient.queryCategoriesWithSubByPid(pid);
        List<CategoryEntity> categoryEntities = listResponseVo.getData();
        return categoryEntities;

    }
}
