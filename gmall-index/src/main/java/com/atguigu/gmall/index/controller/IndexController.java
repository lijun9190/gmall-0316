package com.atguigu.gmall.index.controller;


import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.index.service.IndexService;
import com.atguigu.gmall.pms.entity.CategoryEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
public class IndexController {

    @Autowired
    IndexService indexService;

    @GetMapping

    public String toIndex(Model model){
        List<CategoryEntity> categoryEntities=indexService.queryLv1Categories();
        model.addAttribute("categories", categoryEntities);
        return "index";
    }

    @GetMapping("/index/cates/{pid}")
    @ResponseBody
    public ResponseVo<List<CategoryEntity>> queryLvl2CategoriesWithSub(@PathVariable("pid")Long pid){
        List<CategoryEntity> categoryVos = this.indexService.queryLvl2CategoriesWithSub(pid);
        return ResponseVo.ok(categoryVos);
    }

}
