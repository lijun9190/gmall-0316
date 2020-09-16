package com.atguigu.gmall.pms.service.impl;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.commons.lang.StringUtils;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;

import com.atguigu.gmall.pms.mapper.BrandMapper;
import com.atguigu.gmall.pms.entity.BrandEntity;
import com.atguigu.gmall.pms.service.BrandService;


@Service("brandService")
public class BrandServiceImpl extends ServiceImpl<BrandMapper, BrandEntity> implements BrandService {

    @Override
    public PageResultVo queryPage(PageParamVo paramVo) {

        IPage<BrandEntity> page = this.page(
                paramVo.getPage(),
                new QueryWrapper<BrandEntity>()
        );
        return new PageResultVo(page);
    }


    public static void main(String[] args) {
        if (StringUtils.equals("aaa", "aaaz")) {
            System.out.println("两者相等");
        }
    }

}