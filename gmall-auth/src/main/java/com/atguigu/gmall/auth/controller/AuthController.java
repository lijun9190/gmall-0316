package com.atguigu.gmall.auth.controller;

import com.atguigu.gmall.auth.service.AuthService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;

@Controller
public class AuthController {

    @Autowired
    AuthService authService;


    /**
     * 跳转到登陆页面，获取returnUrl
     */
    @GetMapping("toLogin.html")
    public String toLogin(@RequestParam(value = "returnUrl",defaultValue = "http://gmall.com")String returnUrl, Model model){
        model.addAttribute("returnUrl", returnUrl);
        return "login";
    }

    @PostMapping("login")
    public String login(@RequestParam("loginName")String loginName,
                        @RequestParam("password")String password,
                        @RequestParam("returnUrl")String returnUrl,
                        HttpServletRequest Request,
                        HttpServletResponse response){
        authService.accredit(loginName,password,Request,response);
        return "redirect:"+returnUrl;
    }

    public static void main(String[] args) {
        String str="aa.bb.cc.dd";
        String[] split = StringUtils.split(str, ".");
        System.out.println(Arrays.asList(split ));
    }
}
