package com.atguigu.gmall.pms.mapper;

import com.atguigu.gmall.pms.entity.SkuAttrValueEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

/**
 * sku销售属性&值
 * 
 * @author lijun
 * @email lijun@atguigu.com
 * @date 2020-08-22 15:14:50
 */
@Mapper
public interface SkuAttrValueMapper extends BaseMapper<SkuAttrValueEntity> {


    List<SkuAttrValueEntity> querySearchAttrValueBySkuId(Long skuId);

    List<Map<String, Object>> querySaleAttrValuesMappingSkuIdBySpuId(Long spuId);
}
