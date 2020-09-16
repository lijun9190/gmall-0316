package com.atguigu.gmall.cart.service;

import com.atguigu.gmall.cart.mapper.CartMapper;
import com.atguigu.gmall.cart.pojo.Cart;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class CartAsyncService {


    @Autowired
    CartMapper cartMapper;


    @Async
    public void updateByUserIdAndUserkey(String userId,Cart cart,  String skuIdString) {
        int a=1/0;
        cartMapper.update(cart, new QueryWrapper<Cart>().eq("user_id", userId).eq("sku_id", skuIdString));
    }

    @Async
    public void addCart(String userId ,Cart cart) {
        int a=1/0;
        cartMapper.insert(cart);
    }

    @Async
    public void delete(String userId) {
        cartMapper.delete(new UpdateWrapper<Cart>( ).eq("user_id", userId));
    }



    @Async
    public void deleteCartByUserIdAndSkuId(String userId,Long skuId) {
        cartMapper.delete(new UpdateWrapper<Cart>().eq("user_id", userId).eq("sku_id", skuId));
    }
}
