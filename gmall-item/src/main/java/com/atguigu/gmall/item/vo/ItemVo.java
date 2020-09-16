package com.atguigu.gmall.item.vo;

import com.atguigu.gmall.pms.entity.CategoryEntity;
import com.atguigu.gmall.pms.entity.SkuImagesEntity;
import com.atguigu.gmall.pms.vo.GroupVo;
import com.atguigu.gmall.pms.vo.SaleAttrValueVo;
import com.atguigu.gmall.sms.vo.ItemSaleVo;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
public class ItemVo {
    private List<CategoryEntity> categories;

    private Long brandId;
    private String brandName;

    private Long spuId;

    private String spuName;

    private Long skuId;

    private String title;

    private String subTitle;

    private String defaultImage;

    private BigDecimal price;

    private BigDecimal weight;

    private List<ItemSaleVo> sales;

    private Boolean store=false;

    private List<SkuImagesEntity> images;

    private List<SaleAttrValueVo> saleAttrs;

    private Map<Long,String> saleAttr;

    private String skuJsons;

    private List<String> spuImages;

    private List<GroupVo> groups;
}
