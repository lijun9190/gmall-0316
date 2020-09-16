package com.atguigu.gmall.pms.service.impl;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.bouncycastle.jcajce.provider.symmetric.SM4;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;

import com.atguigu.gmall.pms.mapper.AttrMapper;
import com.atguigu.gmall.pms.entity.AttrEntity;
import com.atguigu.gmall.pms.service.AttrService;

import javax.crypto.SealedObject;


@Service("attrService")
public class AttrServiceImpl extends ServiceImpl<AttrMapper, AttrEntity> implements AttrService {

    @Override
    public PageResultVo queryPage(PageParamVo paramVo) {
        IPage<AttrEntity> page = this.page(
                paramVo.getPage(),
                new QueryWrapper<AttrEntity>()
        );

        return new PageResultVo(page);
    }


    @Override
    public List<AttrEntity> queryByCidAndType(Long catId, Long type, Long searchType) {
        QueryWrapper wrapper=new QueryWrapper();

            wrapper.eq("category_id", catId);


        if(type!=null){
            wrapper.eq("type", type);

        }

        if(searchType!=null){
            wrapper.eq("search_type", searchType);
        }
        return list(wrapper);
    }

}