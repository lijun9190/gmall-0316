package com.atguigu.gmall.pms.service.impl;

import com.atguigu.gmall.pms.entity.*;
import com.atguigu.gmall.pms.feign.GMallSmsClient;
import com.atguigu.gmall.pms.mapper.SkuMapper;
import com.atguigu.gmall.pms.mapper.SpuDescMapper;
import com.atguigu.gmall.pms.service.*;
import com.atguigu.gmall.pms.vo.SkuVo;
import com.atguigu.gmall.pms.vo.SpuAttrValueVo;
import com.atguigu.gmall.pms.vo.SpuVo;
import com.atguigu.gmall.sms.vo.SkuSaleVo;
import io.seata.spring.annotation.GlobalTransactional;
import org.apache.commons.lang.StringUtils;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;

import com.atguigu.gmall.pms.mapper.SpuMapper;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;


@Service("spuService")
public class SpuServiceImpl extends ServiceImpl<SpuMapper, SpuEntity> implements SpuService {

    @Override
    public PageResultVo queryPage(PageParamVo paramVo) {
        IPage<SpuEntity> page = this.page(
                paramVo.getPage(),
                new QueryWrapper<SpuEntity>()
        );

        return new PageResultVo(page);
    }

    @Override
    public PageResultVo selectList(PageParamVo pageParamVo, Long categoryId) {
        // 封装查询条件
        QueryWrapper<SpuEntity> wrapper = new QueryWrapper<>();
        // 如果分类id不为0，要根据分类id查，否则查全部
        if (categoryId != 0) {
            wrapper.eq("category_id", categoryId);
        }

        String key = pageParamVo.getKey();
        if (StringUtils.isNotBlank(key)) {
            wrapper.and(t -> t.like("name", key).or().like("id", key));
        }
        IPage<SpuEntity> page = this.page(pageParamVo.getPage(), wrapper);
        return new PageResultVo(page);
    }

    @Autowired
    SpuDescMapper spuDescMapper;
    @Autowired
    SpuAttrValueService spuAttrValueService;

    @Autowired
    SkuMapper skuMapper;

    @Autowired
    SkuImagesService skuImagesService;

    @Autowired
    SkuAttrValueService skuAttrValueService;

    @Autowired
    GMallSmsClient gMallSmsClient;

    @Autowired
    SpuDescService spuDescService;

    @Autowired
    RabbitTemplate rabbitTemplate;

    @GlobalTransactional
    @Override
    public void bigSave(SpuVo spu) {
        spu.setPublishStatus(1); // 默认是已上架
        spu.setCreateTime(new Date());
        spu.setUpdateTime(spu.getCreateTime());
        /*防止id注入*/
        spu.setId(null);
        save(spu);
        Long id = spu.getId();

    spuDescService.saveSpuDesc(spu, id);


        /*spu_attr_value*/
        List<SpuAttrValueVo> baseAttrs = spu.getBaseAttrs();
        if (!CollectionUtils.isEmpty(baseAttrs)) {
            List<SpuAttrValueEntity> spuAttrValueEntityList = baseAttrs.stream().map(spuAttrValueVo -> {
                SpuAttrValueEntity spuAttrValueEntity = new SpuAttrValueEntity();
                BeanUtils.copyProperties(spuAttrValueVo, spuAttrValueEntity);
                spuAttrValueEntity.setId(null);
                spuAttrValueEntity.setSort(0);
                spuAttrValueEntity.setSpuId(id);
                return spuAttrValueEntity;
            }).collect(Collectors.toList());
            //批量插入用service.saveBatch()方法
            spuAttrValueService.saveBatch(spuAttrValueEntityList);
        }

        /*sku表*/
        List<SkuVo> skus = spu.getSkus();
        if (CollectionUtils.isEmpty(skus)) {
            return;
        }
        skus.forEach(sku -> {
            sku.setId(null);
            sku.setSpuId(id);
            sku.setCatagoryId(spu.getCategoryId());
            sku.setBrandId(spu.getBrandId());
            List<String> images = sku.getImages();
            if (!CollectionUtils.isEmpty(images)) {

                sku.setDefaultImage(sku.getDefaultImage() == null ? images.get(0) : sku.getDefaultImage());
            }
            skuMapper.insert(sku);
            Long skuId = sku.getId();
//            int a= 10/0;

            /*保存sku信息*/
            if (!CollectionUtils.isEmpty(images)) {
                List<SkuImagesEntity> collect = images.stream().map(image -> {
                    SkuImagesEntity skuImagesEntity = new SkuImagesEntity();
                    skuImagesEntity.setId(null);
                    skuImagesEntity.setSkuId(skuId);
                    skuImagesEntity.setUrl(image);
                    skuImagesEntity.setSort(0);
                    skuImagesEntity.setDefaultStatus(0);

                    if (StringUtils.equals(sku.getDefaultImage(), image)) {
                        skuImagesEntity.setDefaultStatus(1);
                    }
                    return skuImagesEntity;
                }).collect(Collectors.toList());
                skuImagesService.saveBatch(collect);
            }

            /*sku_attr_value*/
            List<SkuAttrValueEntity> saleAttrs = sku.getSaleAttrs();
            if (!CollectionUtils.isEmpty(saleAttrs)) {
                saleAttrs.forEach(attr -> {
                    attr.setId(null);
                    attr.setSort(0);
                    attr.setSkuId(skuId);
                });
                skuAttrValueService.saveBatch(saleAttrs);
            }
            SkuSaleVo skuSaleVo = new SkuSaleVo();
            BeanUtils.copyProperties(sku, skuSaleVo);
            skuSaleVo.setSkuId(skuId);
            gMallSmsClient.remoteFeign(skuSaleVo);
            rabbitTemplate.convertAndSend("PMS_SPU_EXCHANGE", "item.insert",id);
        });


    }
    @Autowired
    RabbitTemplate template;

    @Override
    public void mq(SpuEntity spu) {
    template.convertAndSend("SPU_ITEM_EXCHANGE", "item.update", spu.getId());
    }
}