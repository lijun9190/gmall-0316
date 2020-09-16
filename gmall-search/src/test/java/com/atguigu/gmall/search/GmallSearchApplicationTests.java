package com.atguigu.gmall.search;

import com.atguigu.gmall.common.bean.PageParamVo;
import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.pms.entity.*;
import com.atguigu.gmall.search.feign.GmallPmsClient;
import com.atguigu.gmall.search.feign.GmallWmsClient;
import com.atguigu.gmall.search.pojo.Goods;
import com.atguigu.gmall.search.pojo.SearchAttrValueVo;
import com.atguigu.gmall.search.repositiry.GoodsRepository;
import com.atguigu.gmall.wms.api.GmallWmsApi;
import com.atguigu.gmall.wms.entity.WareSkuEntity;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@SpringBootTest
class GmallSearchApplicationTests {

    @Autowired
    GoodsRepository goodsRepository;

    @Autowired
    GmallPmsClient gmallPmsClient;

    @Autowired
    GmallWmsClient gmallWmsClient;

    @Autowired
    ElasticsearchRestTemplate restTemplate;



    @Test
    void contextLoads() {
        restTemplate.createIndex(Goods.class);
        restTemplate.putMapping(Goods.class);
        Integer pageNum = 1;
        Integer pageSize = 100;
        do {
            PageParamVo paramVo = new PageParamVo(pageNum, pageSize, null);
            ResponseVo<List<SpuEntity>> responseVo = gmallPmsClient.querySpuByPageJson(paramVo);
            List<SpuEntity> spuEntities = responseVo.getData();
            if (CollectionUtils.isEmpty(spuEntities)) {
                return;
            }
            spuEntities.forEach((spuEntity -> {
                ResponseVo<List<SkuEntity>> skuResponseVo = gmallPmsClient.querySkusBySpuId(spuEntity.getId());
                List<SkuEntity> skuEntities = skuResponseVo.getData();
                if (!CollectionUtils.isEmpty(skuEntities)) {

                    List<Goods> goodsList = skuEntities.stream().map(skuEntity -> {
                        Goods goods = new Goods();
                        goods.setSkuId(skuEntity.getId());
                        goods.setTitle(skuEntity.getTitle());
                        goods.setSubTitle(skuEntity.getSubtitle());
                        goods.setPrice(skuEntity.getPrice().doubleValue());
                        goods.setDefaultImage(skuEntity.getDefaultImage());
                        goods.setCreateTime(spuEntity.getCreateTime());
                        ResponseVo<BrandEntity> brandEntityResponseVo = gmallPmsClient.queryBrandById(skuEntity.getBrandId());
                        BrandEntity brandEntity = brandEntityResponseVo.getData();


                        if (brandEntity != null) {
                            goods.setBrandId(brandEntity.getId());
                            goods.setBrandName(brandEntity.getName());
                            goods.setLogo(brandEntity.getLogo());
                        }
                        ResponseVo<CategoryEntity> categoryEntityResponseVo = gmallPmsClient.queryCategoryById(spuEntity.getCategoryId());
                        CategoryEntity categoryEntity = categoryEntityResponseVo.getData();
                        if(categoryEntity!=null){
                            goods.setCategoryId(categoryEntity.getId());
                            goods.setCategoryName(categoryEntity.getName());
                        }
                        ResponseVo<List<WareSkuEntity>> listResponseVo = gmallWmsClient.queryWareSkuBySkuId(skuEntity.getId());
                        List<WareSkuEntity> wareSkuEntities = listResponseVo.getData();
                        if(!CollectionUtils.isEmpty(wareSkuEntities)){

                            goods.setStore(wareSkuEntities.stream().anyMatch(wareSkuEntity ->wareSkuEntity.getStock()-wareSkuEntity.getStockLocked()>0));
                            goods.setSales(wareSkuEntities.stream().map(WareSkuEntity::getSales).reduce((a,b)->a+b).get());


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

                        ResponseVo<List<SpuAttrValueEntity>>spuAttrValueResponseVo = gmallPmsClient.querySearchAttrValueBySpuId(spuEntity.getId());
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

            }));
            pageSize = spuEntities.size();
            pageNum++;
        } while (pageSize == 100);
    }




}
