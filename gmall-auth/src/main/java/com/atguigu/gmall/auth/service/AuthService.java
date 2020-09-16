package com.atguigu.gmall.auth.service;

import com.atguigu.gmall.auth.config.JwtProperties;
import com.atguigu.gmall.auth.feign.GmallUmsClient;
import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.common.exception.UserException;
import com.atguigu.gmall.common.utils.CookieUtils;

import com.atguigu.gmall.common.utils.IpUtil;
import com.atguigu.gmall.common.utils.JwtUtils;
import com.atguigu.gmall.ums.entity.UserEntity;
import net.bytebuddy.implementation.bytecode.Throw;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;
import sun.misc.Request;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

@EnableConfigurationProperties(JwtProperties.class)
@Service
public class AuthService {

    @Autowired
    GmallUmsClient umsClient;

    @Autowired
    JwtProperties jwtProperties;

    //构造器注入也可以

    public void accredit(String loginName, String password, HttpServletRequest request, HttpServletResponse response) {
        //1.远程调用ums数据接口查询用户信息
        ResponseVo<UserEntity> userEntityResponseVo = umsClient.queryUser(loginName, password);
        UserEntity userEntity = userEntityResponseVo.getData();

        if (userEntity == null) {
            throw new UserException("用户名或密码错误");
        }

        //3生成jwt载荷信息
        Map<String, Object> map = new HashMap<>();
        map.put("userId", userEntity.getId());
        map.put("userName", userEntity.getUsername());

        //4.防盗用，加入ip信息
        String ip = IpUtil.getIpAddressAtService(request);
        System.out.println("ip = " + ip);
        map.put("ip", ip);


        //5生成jwt
        try {
            //jwt类型的token(jwt是token一种）
            String token = JwtUtils.generateToken(map, jwtProperties.getPrivateKey(), jwtProperties.getExpire());

            //6.jwt放入cookie
            CookieUtils.setCookie(request, response, jwtProperties.getCookieName(), token, jwtProperties.getExpire() * 60);
            //7.把昵称放入cookie
            CookieUtils.setCookie(request, response, jwtProperties.getUnick(), userEntity.getNickname(), jwtProperties.getExpire() * 60);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
