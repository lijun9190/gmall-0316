package com.atguigu.gmall.cart.listener;


import com.atguigu.gmall.cart.feign.GmallPmsClient;
import com.atguigu.gmall.cart.service.CartService;
import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.pms.entity.SkuEntity;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import jdk.nashorn.internal.ir.IfNode;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.List;

@Component
public class CartListener {
    @Autowired
    GmallPmsClient pmsClient;

    @Autowired
    StringRedisTemplate redisTemplate;

    private static final String CACHE_PREFIX="cart:price:";

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "cart_item_queue", durable = "true"),
            exchange = @Exchange(value = "SPU_ITEM_EXCHANGE", ignoreDeclarationExceptions = "true", type = ExchangeTypes.TOPIC),
            key = {"item.update"}
    ))
    public void listener(Long spuId, Channel channel, Message message) throws IOException {
        ResponseVo<List<SkuEntity>> spuEntitiy = pmsClient.querySkusBySpuId(spuId);
        List<SkuEntity> data = spuEntitiy.getData();

        if (!CollectionUtils.isEmpty(data)) {
            data.forEach(skuEntity -> {

                redisTemplate.opsForValue().setIfPresent(CACHE_PREFIX+skuEntity.getId().toString(), skuEntity.getPrice().toString());
            });
        }
        channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
    }
}
