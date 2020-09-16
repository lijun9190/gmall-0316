package com.atguigu.gmall.pms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;
import com.atguigu.gmall.pms.entity.CategoryEntity;

import java.util.List;

/**
 * 商品三级分类
 *
 * @author lijun
 * @email lijun@atguigu.com
 * @date 2020-08-22 15:14:50
 */
public interface CategoryService extends IService<CategoryEntity> {

    PageResultVo queryPage(PageParamVo paramVo);


    List<CategoryEntity> queryCategoryByPid(Long parentId);

    List<CategoryEntity> queryCategoriesWithSubByPid(Long pid);

    List<CategoryEntity> queryAllCategoriesByCid3(Long cid);
}

