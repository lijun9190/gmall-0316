package com.atguigu.gmall.item.service;


import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.common.exception.ItemException;
import com.atguigu.gmall.item.config.ThreadPoolConfig;
import com.atguigu.gmall.item.feign.GmallPmsClient;
import com.atguigu.gmall.item.feign.GmallSmsClient;
import com.atguigu.gmall.item.feign.GmallWmsClient;
import com.atguigu.gmall.item.vo.ItemVo;
import com.atguigu.gmall.pms.api.GmallPmsApi;
import com.atguigu.gmall.pms.entity.*;
import com.atguigu.gmall.pms.vo.GroupVo;
import com.atguigu.gmall.pms.vo.SaleAttrValueVo;
import com.atguigu.gmall.sms.vo.ItemSaleVo;
import com.atguigu.gmall.wms.entity.WareSkuEntity;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.sound.sampled.TargetDataLine;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

@Service
public class ItemService {

    @Autowired
    GmallPmsClient pmsClient;

    @Autowired
    GmallWmsClient wmsClient;

    @Autowired
    GmallSmsClient smsClient;

    @Autowired
    ThreadPoolExecutor threadPoolExecutor;

    public ItemVo loadData(Long skuId) {
        ItemVo itemVo = new ItemVo();
        CompletableFuture<SkuEntity> skuCompletableFuture = CompletableFuture.supplyAsync(() -> {
//        1.根据skuId查询sku信息 Y
            ResponseVo<SkuEntity> skuEntityResponseVo = pmsClient.querySkuById(skuId);
            SkuEntity skuEntity = skuEntityResponseVo.getData();
            if (skuEntity == null) {
                throw new ItemException("该skuId对应的商品不存在");
            }
            itemVo.setSkuId(skuId);
            itemVo.setTitle(skuEntity.getTitle());
            itemVo.setSubTitle(skuEntity.getSubtitle());
            itemVo.setPrice(skuEntity.getPrice());
            itemVo.setWeight(new BigDecimal(skuEntity.getWeight()));
            itemVo.setDefaultImage(skuEntity.getDefaultImage());
            return skuEntity;
        }, threadPoolExecutor);

        CompletableFuture<Void> a1 = skuCompletableFuture.thenAcceptAsync(skuEntity -> {
            ResponseVo<List<CategoryEntity>> cateResponseVo = pmsClient.queryAllCategoriesByCid3(skuEntity.getCatagoryId());
            List<CategoryEntity> categoryEntities = cateResponseVo.getData();
            itemVo.setCategories(categoryEntities);
        }, threadPoolExecutor);

        CompletableFuture<Void> a2 = skuCompletableFuture.thenAcceptAsync(skuEntity -> {
//        2.根据cid3查询一二三级分类集合 Y
//        3.根据品牌id查询品牌信息 Y
            ResponseVo<BrandEntity> brandEntityResponseVo = pmsClient.queryBrandById(skuEntity.getBrandId());
            BrandEntity brandEntity = brandEntityResponseVo.getData();
            if (brandEntity != null) {
                itemVo.setBrandId(brandEntity.getId());
                itemVo.setBrandName(brandEntity.getName());
            }
        }, threadPoolExecutor);

        CompletableFuture<Void> a3 = skuCompletableFuture.thenAcceptAsync(skuEntity -> {

//        4.根据spuId查询spu信息 Y
            ResponseVo<SpuEntity> spuEntityResponseVo = pmsClient.querySpuById(skuEntity.getSpuId());
            SpuEntity spuEntity = spuEntityResponseVo.getData();
            if (spuEntity != null) {
                itemVo.setSpuId(spuEntity.getId());
                itemVo.setSpuName(spuEntity.getName());
            }
        }, threadPoolExecutor);

        CompletableFuture<Void> a4 = CompletableFuture.runAsync(() -> {
//        5.根据skuId查询优惠信息（sms） Y
            ResponseVo<List<ItemSaleVo>> listResponseVo = smsClient.querySaleVoBySkuId(skuId);
            List<ItemSaleVo> itemSaleVos = listResponseVo.getData();
            itemVo.setSales(itemSaleVos);
        }, threadPoolExecutor);


        CompletableFuture<Void> a5 = CompletableFuture.runAsync(() -> {
//        6.根据skuId查询库存信息 Y
            ResponseVo<List<WareSkuEntity>> wareResponse = wmsClient.queryWareSkuBySkuId(skuId);
            List<WareSkuEntity> wareSkuEntities = wareResponse.getData();
            if (!CollectionUtils.isEmpty(wareSkuEntities)) {
                itemVo.setStore(wareSkuEntities.stream().anyMatch(wareSkuEntity -> wareSkuEntity.getStock() - wareSkuEntity.getStockLocked() > 0));
            }

        }, threadPoolExecutor);

        CompletableFuture<Void> a6 = CompletableFuture.runAsync(() -> {
//        7.根据skuId查询sku的图片列表 Y
            ResponseVo<List<SkuImagesEntity>> images = pmsClient.querySkuImagesBySkuId(skuId);
            List<SkuImagesEntity> imagesEntities = images.getData();
            itemVo.setImages(imagesEntities);

        }, threadPoolExecutor);

        CompletableFuture<Void> a7 = skuCompletableFuture.thenAcceptAsync(skuEntity -> {
//        8.根据spuId查询spu下所有sku的销售属性组合 Y
            ResponseVo<List<SaleAttrValueVo>> saleAttrValueResponseVo = pmsClient.querySaleAttrValueBySpuId(skuEntity.getSpuId());
            List<SaleAttrValueVo> saleAttrValueVos = saleAttrValueResponseVo.getData();
            itemVo.setSaleAttrs(saleAttrValueVos);
        }, threadPoolExecutor);

        CompletableFuture<Void> a8 = CompletableFuture.runAsync(() -> {
//        9.根据skuId查询当前sku的销售属性  Y
            ResponseVo<List<SkuAttrValueEntity>> saleAttrVaResponse = pmsClient.querySaleAttrValuesBySkuId(skuId);
            List<SkuAttrValueEntity> skuAttrValueEntities = saleAttrVaResponse.getData();
            if (!CollectionUtils.isEmpty(skuAttrValueEntities)) {
                Map<Long, String> collect = skuAttrValueEntities.stream().collect(Collectors.toMap(SkuAttrValueEntity::getAttrId, SkuAttrValueEntity::getAttrValue));
                itemVo.setSaleAttr(collect);
            }

        }, threadPoolExecutor);


        CompletableFuture<Void> a9 = skuCompletableFuture.thenAcceptAsync(skuEntity -> {
//        10.根据spuId查询spu下所有销售属性组合和skuId的映射关系 Y
            ResponseVo<String> mappingResponseVo = pmsClient.querySaleAttrValuesMappingSkuIdBySpuId(skuEntity.getSpuId());
            String json = mappingResponseVo.getData();
            itemVo.setSkuJsons(json);

        }, threadPoolExecutor);


        CompletableFuture<Void> a10 = skuCompletableFuture.thenAcceptAsync(skuEntity -> {

//        11.根据spuId查询spu的海报信息列表 Y
            ResponseVo<SpuDescEntity> spuDescEntityResponseVo = pmsClient.querySpuDescById(skuEntity.getSpuId());
            SpuDescEntity data = spuDescEntityResponseVo.getData();
            if (data != null && StringUtils.isNoneBlank(data.getDecript())) {
                String[] split = StringUtils.split(data.getDecript(), ",");
                itemVo.setSpuImages(Arrays.asList(split));
            }
        }, threadPoolExecutor);

        CompletableFuture<Void> a11 = skuCompletableFuture.thenAcceptAsync(skuEntity -> {
//        12.根据cid3、spuId、skuId查询分组及组下的规格参数以及值
            ResponseVo<List<GroupVo>> groupListResponseVo = pmsClient.queryGroupByCidAndSpuIdAndSkuId(skuEntity.getCatagoryId(), skuEntity.getSpuId(), skuId);
            List<GroupVo> groupVos = groupListResponseVo.getData();
            itemVo.setGroups(groupVos);
        }, threadPoolExecutor);

        CompletableFuture.allOf(a1,a2,a3,a4,a5,a6,a7,a8,a9,a10,a11).join();
        return itemVo;
    }


}
