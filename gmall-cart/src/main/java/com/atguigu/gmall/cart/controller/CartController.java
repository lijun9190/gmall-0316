package com.atguigu.gmall.cart.controller;

import com.atguigu.gmall.cart.exception.CartException;
import com.atguigu.gmall.cart.interceptor.LoginInterceptor;
import com.atguigu.gmall.cart.pojo.Cart;
import com.atguigu.gmall.common.bean.UserInfo;
import com.atguigu.gmall.cart.service.AsyncTest;
import com.atguigu.gmall.cart.service.CartService;
import com.atguigu.gmall.common.bean.ResponseVo;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.ExecutionException;


@Controller
public class CartController {

    @Autowired
    CartService cartService;

    @GetMapping
    public String addCart(Cart cart) {
        if (cart == null || cart.getSkuId() == null) {
            throw new CartException("请选择加入购物车的商品");
        }
        cartService.addCart(cart);
        return "redirect:http://cart.gmall.com/addCart.html?skuId=" + cart.getSkuId();
    }

    @GetMapping("addCart.html")
    public String queryCartBySkuId(@RequestParam("skuId") Long skuId, Model model) throws JsonProcessingException {
        Cart cart = cartService.queryCartBySkuId(skuId);
        model.addAttribute("cart", cart);
        return "addCart";
    }

    @GetMapping("test")
    @ResponseBody
    public String test() {
        UserInfo userInfo = LoginInterceptor.getUserInfo();
        System.out.println(userInfo);
        return "hello cart!";
    }

    @GetMapping("cart.html")
    public String queryCartsByUserId(Model model) {
        List<Cart> carts = cartService.queryCartsByUserId();
        model.addAttribute("carts", carts);
        return "cart";
    }

    //修改购物车数量

    @PostMapping("updateNum")
    @ResponseBody
    public ResponseVo<Cart> updateCount(@RequestBody Cart cart) {
        cartService.updateCount(cart);
        return ResponseVo.ok();
    }


    /**
     * 删除购物车
     */
    @PostMapping("deleteCart")
    @ResponseBody
    public ResponseVo<Object> deleteCart(@RequestParam("skuId") Long skuId) {

        this.cartService.deleteCart(skuId);
        return ResponseVo.ok();
    }

    @Autowired
    AsyncTest asyncTest;

    @GetMapping("task")
    @ResponseBody
    public String task(Integer a,Integer b) throws ExecutionException, InterruptedException {
        long now = System.currentTimeMillis();
        System.out.println("controller.test方法开始执行！");
        this.asyncTest.executor1(b,b);
        asyncTest.executor2(a,b);
        System.out.println("controller.test方法结束执行！！！" + (System.currentTimeMillis() - now));
        return "hello cart!";
    }
}

