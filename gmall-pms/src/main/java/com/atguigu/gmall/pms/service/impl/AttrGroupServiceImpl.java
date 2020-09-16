package com.atguigu.gmall.pms.service.impl;

import com.atguigu.gmall.pms.entity.AttrEntity;
import com.atguigu.gmall.pms.entity.SkuAttrValueEntity;
import com.atguigu.gmall.pms.entity.SpuAttrValueEntity;
import com.atguigu.gmall.pms.mapper.AttrMapper;
import com.atguigu.gmall.pms.mapper.SkuAttrValueMapper;
import com.atguigu.gmall.pms.mapper.SpuAttrValueMapper;
import com.atguigu.gmall.pms.service.SkuAttrValueService;
import com.atguigu.gmall.pms.vo.AttrValueVo;
import com.atguigu.gmall.pms.vo.GroupVo;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.data.annotation.Id;
import org.springframework.data.web.config.QuerydslWebConfiguration;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;

import com.atguigu.gmall.pms.mapper.AttrGroupMapper;
import com.atguigu.gmall.pms.entity.AttrGroupEntity;
import com.atguigu.gmall.pms.service.AttrGroupService;
import org.springframework.util.CollectionUtils;


@Service("attrGroupService")
public class AttrGroupServiceImpl extends ServiceImpl<AttrGroupMapper, AttrGroupEntity> implements AttrGroupService {
    @Override
    public PageResultVo queryPage(PageParamVo paramVo) {
        IPage<AttrGroupEntity> page = this.page(
                paramVo.getPage(),
                new QueryWrapper<AttrGroupEntity>()
        );

        return new PageResultVo(page);
    }

    @Autowired
    AttrMapper attrMapper;


    @Override
    public List<AttrGroupEntity> queryByCId(Long catId) {
        List<AttrGroupEntity> groupEntityList = list(new QueryWrapper<AttrGroupEntity>().eq("category_id", catId));
        for (AttrGroupEntity attrGroupEntity : groupEntityList) {
            Long Id= attrGroupEntity.getId();
            List<AttrEntity> attrEntities = attrMapper.selectList(new QueryWrapper<AttrEntity>().eq("group_id", Id).eq("type", 1));
            attrGroupEntity.setAttrEntities(attrEntities);

        }
        return groupEntityList;
    }

    @Autowired
    SpuAttrValueMapper spuAttrValueMapper;

    @Autowired
    SkuAttrValueMapper skuAttrValueMapper;
    @Override
    public List<GroupVo> queryGroupByCidAndSpuIdAndSkuId(long cid, Long spuId, Long skuId) {
      List<AttrGroupEntity> groupEntityList= list(new QueryWrapper<AttrGroupEntity>().eq("category_id", cid));
      if(CollectionUtils.isEmpty(groupEntityList)){
          return null;
      }

     return groupEntityList.stream().map(groupEntity ->{
          GroupVo groupVo = new GroupVo();
            groupVo.setGroupId(groupEntity.getId());
            groupVo.setGroupName(groupEntity.getName());

           List<AttrEntity> entities= attrMapper.selectList(new QueryWrapper<AttrEntity>().eq("group_id", groupEntity.getId()));
           if(!CollectionUtils.isEmpty(entities)){
               List<Long> attrIds = entities.stream().map(AttrEntity::getGroupId).collect(Collectors.toList());
               List<AttrValueVo> attrValueVoList=new ArrayList<>();
              List<SkuAttrValueEntity> skuAttrValueEntityList= skuAttrValueMapper.selectList(new QueryWrapper<SkuAttrValueEntity>().in("attr_id", attrIds).eq("sku_id", skuId));
             if(!CollectionUtils.isEmpty(skuAttrValueEntityList)){
                 attrValueVoList.addAll(skuAttrValueEntityList.stream().map(skuAttrValueEntity -> {
                     AttrValueVo attrValueVo = new AttrValueVo();
                     BeanUtils.copyProperties(skuAttrValueEntity, attrValueVo);
                     return attrValueVo;
                 }).collect(Collectors.toList()));
             }
              List<SpuAttrValueEntity> spuAttrValueEntityList = spuAttrValueMapper.selectList(new QueryWrapper<SpuAttrValueEntity>().in("attr_id", attrIds).eq("spu_id", spuId));
              if(!CollectionUtils.isEmpty(spuAttrValueEntityList)){
                  attrValueVoList.addAll(spuAttrValueEntityList.stream().map(spuAttrValueEntity -> {
                      AttrValueVo attrValueVo = new AttrValueVo();
                      BeanUtils.copyProperties(spuAttrValueEntity, attrValueVo);
                      return attrValueVo;
                  }).collect(Collectors.toList()));
              }

               groupVo.setAttrs(attrValueVoList);
           }
          return groupVo;
      }).collect(Collectors.toList());

    }

}