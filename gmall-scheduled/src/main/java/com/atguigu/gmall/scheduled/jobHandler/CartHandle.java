package com.atguigu.gmall.scheduled.jobHandler;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.scheduled.mapper.CartMapper;
import com.atguigu.gmall.scheduled.pojo.Cart;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.annotation.XxlJob;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.BoundListOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Component
public class CartHandle {
    private static final String KEY = "cart:async:exception";

    private static final String KEY_PREFIX = "cart:info:";

    private static final String CACHE_PREFIX = "cart:price:";

    @Autowired
    StringRedisTemplate redisTemplate;

    @Autowired
    CartMapper cartMapper;

    @XxlJob("cartHandler")
    public ReturnT<String> synchronize(String param) {
        BoundListOperations<String, String> listOps = redisTemplate.boundListOps(KEY);
        if (listOps.size() == 0) {
            return ReturnT.SUCCESS;
        }
        String userId = listOps.rightPop();
        while (StringUtils.isNotEmpty(userId)) {
            //1。删除mysql中该userId的购物车记录
            cartMapper.delete(new QueryWrapper<Cart>().eq("user_id", userId));
            //2.获取该用户的redis记录
            BoundHashOperations<String, Object, Object> hashOps = redisTemplate.boundHashOps(KEY_PREFIX + userId);
            List<Object> cartJsons = hashOps.values();
            if (CollectionUtils.isEmpty(cartJsons)) {
                userId = listOps.rightPop();
                continue;
            }
            //3.添加到MySQL
            cartJsons.forEach(cart -> {
                cartMapper.insert(JSON.parseObject(cart.toString(), Cart.class));
            });
            userId = listOps.rightPop();
        }
        return ReturnT.SUCCESS;
    }
}
