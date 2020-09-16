package com.atguigu.gmall.pms.mapper;

import com.atguigu.gmall.pms.entity.AttrEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Component;

/**
 * 商品属性
 * 
 * @author lijun
 * @email lijun@atguigu.com
 * @date 2020-08-22 15:14:50
 */
@Mapper
@Component
public interface AttrMapper extends BaseMapper<AttrEntity> {
	
}
