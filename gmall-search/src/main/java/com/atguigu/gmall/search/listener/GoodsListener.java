package com.atguigu.gmall.search.listener;

import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.pms.entity.*;
import com.atguigu.gmall.search.feign.GmallPmsClient;
import com.atguigu.gmall.search.feign.GmallWmsClient;
import com.atguigu.gmall.search.pojo.Goods;
import com.atguigu.gmall.search.pojo.SearchAttrValueVo;
import com.atguigu.gmall.search.repositiry.GoodsRepository;
import com.atguigu.gmall.wms.entity.WareSkuEntity;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


@Component
public class GoodsListener {

    @Autowired
    GmallPmsClient gmallPmsClient;

    @Autowired
    GmallWmsClient gmallWmsClient;

    @Autowired
    GoodsRepository goodsRepository;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "search_goods_queue",durable = "true"),
            exchange = @Exchange(value = "PMS_SPU_EXCHANGE",ignoreDeclarationExceptions = "true",type = ExchangeTypes.TOPIC),
            key = ("item.insert")
    ))
    public void listener(Long spuId, Channel channel, Message message) throws IOException {
        System.out.println("传递过来的数据是："+spuId);
        ResponseVo<List<SkuEntity>> skuResponseVo = gmallPmsClient.querySkusBySpuId(spuId);
        List<SkuEntity> skuEntities = skuResponseVo.getData();
        if (!CollectionUtils.isEmpty(skuEntities)) {

            List<Goods> goodsList = skuEntities.stream().map(skuEntity -> {
                Goods goods = new Goods();
                goods.setSkuId(skuEntity.getId());
                goods.setTitle(skuEntity.getTitle());
                goods.setSubTitle(skuEntity.getSubtitle());
                goods.setPrice(skuEntity.getPrice().doubleValue());
                goods.setDefaultImage(skuEntity.getDefaultImage());
                ResponseVo<SpuEntity> spuEntityResponseVo = gmallPmsClient.querySpuById(spuId);
                SpuEntity data = spuEntityResponseVo.getData();
                if(data!=null){
                    goods.setCreateTime(data.getCreateTime());
                }
                ResponseVo<BrandEntity> brandEntityResponseVo = gmallPmsClient.queryBrandById(skuEntity.getBrandId());
                BrandEntity brandEntity = brandEntityResponseVo.getData();


                if (brandEntity != null) {
                    goods.setBrandId(brandEntity.getId());
                    goods.setBrandName(brandEntity.getName());
                    goods.setLogo(brandEntity.getLogo());
                }
                ResponseVo<CategoryEntity> categoryEntityResponseVo = gmallPmsClient.queryCategoryById(skuEntity.getCatagoryId());
                CategoryEntity categoryEntity = categoryEntityResponseVo.getData();
                if(categoryEntity!=null){
                    goods.setCategoryId(categoryEntity.getId());
                    goods.setCategoryName(categoryEntity.getName());
                }
                ResponseVo<List<WareSkuEntity>> listResponseVo = gmallWmsClient.queryWareSkuBySkuId(skuEntity.getId());
                List<WareSkuEntity> wareSkuEntities = listResponseVo.getData();
                if(!CollectionUtils.isEmpty(wareSkuEntities)){

                    goods.setStore(wareSkuEntities.stream().anyMatch(wareSkuEntity ->wareSkuEntity.getStock()-wareSkuEntity.getStockLocked()>0));
                    goods.setSales(wareSkuEntities.stream().map(WareSkuEntity::getSales).reduce((a, b)->a+b).get());


                }
                ResponseVo<List<SkuAttrValueEntity>> skuAttrValueResponseVo = gmallPmsClient.querySearchAttrValueBySkuId(skuEntity.getId());
                List<SkuAttrValueEntity> skuAttrValueEntities = skuAttrValueResponseVo.getData();
                List<SearchAttrValueVo> attrValueVos=new ArrayList<>();
                if(!CollectionUtils.isEmpty(skuAttrValueEntities)){
                    attrValueVos.addAll( skuAttrValueEntities.stream().map(skuAttrValueEntity -> {
                        SearchAttrValueVo searchAttrValueVo = new SearchAttrValueVo();
                        BeanUtils.copyProperties(skuAttrValueEntity, searchAttrValueVo);
                        return searchAttrValueVo;
                    }).collect(Collectors.toList()));
                }

                ResponseVo<List<SpuAttrValueEntity>> spuAttrValueResponseVo = gmallPmsClient.querySearchAttrValueBySpuId(spuId);
                List<SpuAttrValueEntity> spuAttrValueEntities = spuAttrValueResponseVo.getData();
                if(!CollectionUtils.isEmpty(spuAttrValueEntities)){
                    attrValueVos.addAll(spuAttrValueEntities.stream().map(spuAttrValueEntity -> {
                        SearchAttrValueVo searchAttrValueVo = new SearchAttrValueVo();

                        BeanUtils.copyProperties(spuAttrValueEntity, searchAttrValueVo);
                        return searchAttrValueVo;
                    }).collect(Collectors.toList()));
                }

                goods.setSearchAttrs(attrValueVos);
                return goods;
            }).collect(Collectors.toList());
            goodsRepository.saveAll(goodsList);
        }
        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
    }
}
