package com.atguigu.gmall.sms.service.impl;

import com.atguigu.gmall.sms.entity.SkuFullReductionEntity;
import com.atguigu.gmall.sms.entity.SkuLadderEntity;
import com.atguigu.gmall.sms.mapper.SkuFullReductionMapper;
import com.atguigu.gmall.sms.mapper.SkuLadderMapper;
import com.atguigu.gmall.sms.vo.ItemSaleVo;
import com.atguigu.gmall.sms.vo.SkuSaleVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;

import com.atguigu.gmall.sms.mapper.SkuBoundsMapper;
import com.atguigu.gmall.sms.entity.SkuBoundsEntity;
import com.atguigu.gmall.sms.service.SkuBoundsService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;


@Service("skuBoundsService")
public class SkuBoundsServiceImpl extends ServiceImpl<SkuBoundsMapper, SkuBoundsEntity> implements SkuBoundsService {

    @Override
    public PageResultVo queryPage(PageParamVo paramVo) {
        IPage<SkuBoundsEntity> page = this.page(
                paramVo.getPage(),
                new QueryWrapper<SkuBoundsEntity>()
        );

        return new PageResultVo(page);
    }

    @Autowired
    SkuFullReductionMapper skuFullReductionMapper;

    @Autowired
    SkuLadderMapper skuLadderMapper;

    @Transactional
    @Override
    public void saveSkuSaleInfo(SkuSaleVo skuSaleVo) {
        SkuBoundsEntity skuBoundsEntity = new SkuBoundsEntity();
        BeanUtils.copyProperties(skuSaleVo, skuBoundsEntity);
        List<Integer> work = skuSaleVo.getWork();
        if (!CollectionUtils.isEmpty(work)) {
            skuBoundsEntity.setWork(work.get(3) * 8 + work.get(2) * 4 + work.get(1) * 2 + work.get(0));
        }
        save(skuBoundsEntity);

        SkuFullReductionEntity skfrntity = new SkuFullReductionEntity();
        BeanUtils.copyProperties(skuSaleVo, skfrntity);
        skfrntity.setAddOther(skuSaleVo.getFullAddOther());
        skuFullReductionMapper.insert(skfrntity);

        SkuLadderEntity sklentity = new SkuLadderEntity();
        BeanUtils.copyProperties(skuSaleVo, sklentity);
        sklentity.setAddOther(skuSaleVo.getLadderAddOther());
        skuLadderMapper.insert(sklentity);

    }

    @Override
    public List<ItemSaleVo> querySaleVoBySkuId(Long skuId) {
        List<ItemSaleVo> itemSaleVos=new ArrayList<>();

        //积分类
        SkuBoundsEntity skuBoundsEntity = getOne(new QueryWrapper<SkuBoundsEntity>().eq("sku_id", skuId));
        if(skuBoundsEntity!=null){
            ItemSaleVo itemSaleVo = new ItemSaleVo();
            itemSaleVo.setType("积分");
            itemSaleVo.setDesc("song"+skuBoundsEntity.getGrowBounds().longValue()+"成长积分,  送"+skuBoundsEntity.getBuyBounds()+"购物积分");
            itemSaleVos.add(itemSaleVo);
        }
        //满减类
        SkuFullReductionEntity skuFullReductionEntity = skuFullReductionMapper.selectOne(new QueryWrapper<SkuFullReductionEntity>().eq("sku_id", skuId));
        if (skuFullReductionEntity != null) {
            ItemSaleVo itemSaleVo = new ItemSaleVo();
            itemSaleVo.setType("满减");
            itemSaleVo.setDesc("满" + skuFullReductionEntity.getFullPrice() + "减" + skuFullReductionEntity.getReducePrice());
            itemSaleVos.add(itemSaleVo);
        }

        SkuLadderEntity skuLadderEntity = skuLadderMapper.selectOne(new QueryWrapper<SkuLadderEntity>().eq("sku_id", skuId));
        if(skuLadderEntity!=null){
            ItemSaleVo itemSaleVo = new ItemSaleVo();
            itemSaleVo.setType("打折");
            itemSaleVo.setDesc("满" + skuLadderEntity.getFullCount() + "件，打" + skuLadderEntity.getDiscount().divide(new BigDecimal(10)) + "折");
            itemSaleVos.add(itemSaleVo);
        }


        return itemSaleVos;
    }


}