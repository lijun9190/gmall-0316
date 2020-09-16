package com.atguigu.gmall.item.controller;


import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.item.service.ItemService;
import com.atguigu.gmall.item.vo.ItemVo;
import net.sf.jsqlparser.expression.LongValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class ItemController {
    @Autowired
    ItemService itemService;


    @GetMapping("{skuId}.html")
    public String toItem(@PathVariable("skuId") Long skuId, Model model){

        ItemVo itemVo = itemService.loadData(skuId);
        model.addAttribute("itemVo", itemVo);
        System.out.println("skuid:"+itemVo.getSkuId());
       return "item";

    }
}
