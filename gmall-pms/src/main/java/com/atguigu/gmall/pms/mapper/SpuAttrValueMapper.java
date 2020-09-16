package com.atguigu.gmall.pms.mapper;

import com.atguigu.gmall.pms.entity.SpuAttrValueEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * spu属性值
 * 
 * @author lijun
 * @email lijun@atguigu.com
 * @date 2020-08-22 15:14:50
 */
@Mapper
public interface SpuAttrValueMapper extends BaseMapper<SpuAttrValueEntity> {

    List<SpuAttrValueEntity> querySearchAttrValueBySpuId(Long spuId);
}
