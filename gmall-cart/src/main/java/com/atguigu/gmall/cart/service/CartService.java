package com.atguigu.gmall.cart.service;

import com.atguigu.gmall.cart.exception.CartException;
import com.atguigu.gmall.cart.feign.GmallPmsClient;
import com.atguigu.gmall.cart.feign.GmallSmsClient;
import com.atguigu.gmall.cart.feign.GmallWmsClient;
import com.atguigu.gmall.cart.interceptor.LoginInterceptor;
import com.atguigu.gmall.cart.pojo.Cart;
import com.atguigu.gmall.common.bean.UserInfo;
import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.pms.entity.SkuAttrValueEntity;
import com.atguigu.gmall.pms.entity.SkuEntity;

import com.atguigu.gmall.sms.vo.ItemSaleVo;
import com.atguigu.gmall.wms.entity.WareSkuEntity;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;


import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CartService {

    @Autowired
    GmallPmsClient pmsClient;

    @Autowired
    GmallSmsClient smsClient;

    @Autowired
    GmallWmsClient wmsClient;

    @Autowired
    StringRedisTemplate redisTemplate;


    @Autowired
    CartAsyncService asyncService;

    private static final String KEY_PREFIX = "cart:info:";

    private static final String CACHE_PREFIX = "cart:price:";

    private static final ObjectMapper MAPPER = new ObjectMapper();

    public void addCart(Cart cart) {
        //1组装key
        String userId = getUserId();
        String key = KEY_PREFIX + userId;
        //2.获取该用户的购物车
        //hashops相当于内层map
        BoundHashOperations<String, Object, Object> hashOps = redisTemplate.boundHashOps(key);
        String skuIdString = cart.getSkuId().toString();
        BigDecimal count = cart.getCount();
        try {
            if (hashOps.hasKey(skuIdString)) {
                try {
                    String json = hashOps.get(skuIdString).toString();
                    cart = MAPPER.readValue(json, Cart.class);
                    cart.setCount(cart.getCount().add(count));

                    //写回数据库

                    //2.mysql

                    asyncService.updateByUserIdAndUserkey(userId, cart, skuIdString);

                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }
            } else {
                cart.setUserId(userId);
                ResponseVo<SkuEntity> skuEntityResponseVo = pmsClient.querySkuById(cart.getSkuId());
                SkuEntity skuEntity = skuEntityResponseVo.getData();
                if (skuEntity == null) {
                    throw new CartException("你加入购物车的商品不存在");
                }
                cart.setDefaultImage(skuEntity.getDefaultImage());
                cart.setTitle(skuEntity.getTitle());
                cart.setPrice(skuEntity.getPrice());
                ResponseVo<List<SkuAttrValueEntity>> saleResponseVo = pmsClient.querySaleAttrValuesBySkuId(cart.getSkuId());
                List<SkuAttrValueEntity> saleResponseVoData = saleResponseVo.getData();
                String json = MAPPER.writeValueAsString(saleResponseVoData);
                cart.setSaleAttrs(json);

                ResponseVo<List<ItemSaleVo>> itemVo = smsClient.querySaleVoBySkuId(cart.getSkuId());
                List<ItemSaleVo> saleVos = itemVo.getData();

                cart.setSales(MAPPER.writeValueAsString(saleVos));

                ResponseVo<List<WareSkuEntity>> wareResponseVo = wmsClient.queryWareSkuBySkuId(cart.getSkuId());
                List<WareSkuEntity> wareData = wareResponseVo.getData();
                if (!CollectionUtils.isEmpty(wareData)) {

                    cart.setStore(wareData.stream().anyMatch(wareSkuEntity -> wareSkuEntity.getStock() - wareSkuEntity.getStockLocked() > 0));
                }

                cart.setCheck(true);

                //保存到mysql

                asyncService.addCart(userId.toString(),cart);
                redisTemplate.opsForValue().set(CACHE_PREFIX + skuIdString, cart.getPrice().toString());
            }
            //1.redis
            hashOps.put(skuIdString, MAPPER.writeValueAsString(cart));


        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    private String getUserId() {
        UserInfo userInfo = LoginInterceptor.getUserInfo();
        String userId = null;
        if (userInfo.getUserId() == null) {
            userId = userInfo.getUserKey();
        } else {
            userId = userInfo.getUserId().toString();
        }
        return userId;
    }

    public Cart queryCartBySkuId(Long skuId) throws JsonProcessingException {
        String userId = getUserId();
        String key = KEY_PREFIX + userId;
        BoundHashOperations<String, Object, Object> hashOps = redisTemplate.boundHashOps(key);
        if (hashOps.hasKey(skuId.toString())) {
            String json = hashOps.get(skuId.toString()).toString();
            if (StringUtils.isNoneBlank(json)) {
                return MAPPER.readValue(json, Cart.class);
            }
        }
        return null;
    }

    public List<Cart> queryCartsByUserId() {
        //1.获取userkey,查询未登录的购物车
        UserInfo userInfo = LoginInterceptor.getUserInfo();
        String userKey = userInfo.getUserKey();
        String unLoginKey = KEY_PREFIX + userKey;
        BoundHashOperations<String, Object, Object> hashOps = redisTemplate.boundHashOps(unLoginKey);
        List<Object> values = hashOps.values();
        List<Cart> unLoginCarts = null;
        if (!CollectionUtils.isEmpty(values)) {
            unLoginCarts = values.stream().map(cartJson -> {
                //反序列化
                try {
                    Cart cart = MAPPER.readValue(cartJson.toString(), Cart.class);
                    cart.setCurrentPrice(new BigDecimal(redisTemplate.opsForValue().get(CACHE_PREFIX + cart.getSkuId())));
                    return cart;
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }
                return null;
            }).collect(Collectors.toList());
        }

//2.
        Long userId = userInfo.getUserId();
        if (userId == null) {
            return unLoginCarts;
        }
        //3.合并购物车
        String loginUserId = KEY_PREFIX + userId;
        BoundHashOperations<String, Object, Object> loginHashOps = redisTemplate.boundHashOps(loginUserId);
        if (!CollectionUtils.isEmpty(unLoginCarts)) {
            unLoginCarts.forEach(cart -> {
                try {
                    /**
                     *     "haskey(里面必须是string类型)"
                     */
                    if (loginHashOps.hasKey(cart.getSkuId().toString())) {
                        String loginJson = loginHashOps.get(cart.getSkuId().toString()).toString();
                        BigDecimal UnCount = cart.getCount();
                        cart = MAPPER.readValue(loginJson, Cart.class);
                        cart.setCount(cart.getCount().add(UnCount));
                        asyncService.updateByUserIdAndUserkey(userId.toString(),cart,  cart.getSkuId().toString());
                    } else {
                        cart.setUserId(userId.toString());
                        asyncService.addCart(userId.toString(),cart);
                    }
                    loginHashOps.put(cart.getSkuId().toString(), MAPPER.writeValueAsString(cart));
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }
            });
        }

        //4删除未登录购物车
        redisTemplate.delete(unLoginKey);
        asyncService.delete(userKey);

        //5.查询登录状态的购物车
        List<Object> cartJsons = loginHashOps.values();
        if (!CollectionUtils.isEmpty(cartJsons)) {
            return cartJsons.stream().map(cartJson -> {
                try {
                    Cart cart = MAPPER.readValue(cartJson.toString(), Cart.class);
                    cart.setCurrentPrice(new BigDecimal(redisTemplate.opsForValue().get(CACHE_PREFIX + cart.getSkuId())));
                    return cart;
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }
                return null;
            }).collect(Collectors.toList());
        }

        return null;
    }

    public void updateCount(Cart cart) {
        String userId = getUserId();
        String key = KEY_PREFIX + userId;
        BoundHashOperations<String, Object, Object> hashOps = redisTemplate.boundHashOps(key);
        /**
         *     "haskey(里面必须是string类型)"
         */
        if (hashOps.hasKey(cart.getSkuId().toString())) {
            BigDecimal count = cart.getCount();
            String cartJson = hashOps.get(cart.getSkuId().toString()).toString();
            try {
                cart = MAPPER.readValue(cartJson, Cart.class);
                cart.setCount(count);
                hashOps.put(cart.getSkuId().toString(), MAPPER.writeValueAsString(cart));
                asyncService.updateByUserIdAndUserkey(userId, cart, cart.getSkuId().toString());
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }
    }


    public void deleteCart(Long skuId) {
        String userId = getUserId();
        String key = KEY_PREFIX + userId;
        BoundHashOperations<String, Object, Object> hashOps = redisTemplate.boundHashOps(key);
        if (hashOps.hasKey(skuId.toString())) {
            hashOps.delete(skuId.toString());
            asyncService.deleteCartByUserIdAndSkuId(userId, skuId);
        }
    }
}
