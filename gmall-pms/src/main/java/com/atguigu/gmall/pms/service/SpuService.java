package com.atguigu.gmall.pms.service;

import com.atguigu.gmall.pms.vo.SpuVo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;
import com.atguigu.gmall.pms.entity.SpuEntity;

/**
 * spu信息
 *
 * @author lijun
 * @email lijun@atguigu.com
 * @date 2020-08-22 15:14:50
 */
public interface SpuService extends IService<SpuEntity> {

    PageResultVo queryPage(PageParamVo paramVo);

    PageResultVo selectList(PageParamVo pageParamVo, Long categoryId);


    void bigSave(SpuVo spu);

    void mq(SpuEntity spu);
}

