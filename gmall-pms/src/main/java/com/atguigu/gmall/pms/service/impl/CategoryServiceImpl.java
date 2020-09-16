package com.atguigu.gmall.pms.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;

import com.atguigu.gmall.pms.mapper.CategoryMapper;
import com.atguigu.gmall.pms.entity.CategoryEntity;
import com.atguigu.gmall.pms.service.CategoryService;


@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, CategoryEntity> implements CategoryService {

    @Override
    public PageResultVo queryPage(PageParamVo paramVo) {
        IPage<CategoryEntity> page = this.page(
                paramVo.getPage(),
                new QueryWrapper<CategoryEntity>()
        );

        return new PageResultVo(page);
    }


    @Override
    public List<CategoryEntity> queryCategoryByPid(Long parentId) {

        QueryWrapper<CategoryEntity> qw=new QueryWrapper<>();
      if(parentId != -1){
          qw.eq("parent_id", parentId);
      }
      return baseMapper.selectList(qw);

    }

    @Override
    public List<CategoryEntity> queryCategoriesWithSubByPid(Long pid) {
        List<CategoryEntity> categoryEntities=baseMapper.queryCategoriesWithSubByPid(pid);
        return categoryEntities;
    }

    @Override
    public List<CategoryEntity> queryAllCategoriesByCid3(Long cid) {
        CategoryEntity entityLv3 = getById(cid);
        if (entityLv3 != null) {
            CategoryEntity entityLv2 = getById(entityLv3.getParentId());

            CategoryEntity entityLv1 = getById(entityLv2.getParentId());
            return Arrays.asList(entityLv1,entityLv2,entityLv3);
        }
        return null;
    }

}