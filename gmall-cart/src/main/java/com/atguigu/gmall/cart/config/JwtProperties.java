package com.atguigu.gmall.cart.config;

import com.atguigu.gmall.common.utils.RsaUtils;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.annotation.PostConstruct;
import java.io.File;
import java.security.PublicKey;

@Data
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {
    private String pubKeyPath;
    private String cookieName;
    private  String userKeyName;
    private  Integer expire;


    private PublicKey publicKey;

    
    @PostConstruct
    public void init(){
        //获取公钥私钥文件
        try {
            File pubFile = new File(pubKeyPath);
            publicKey = RsaUtils.getPublicKey(pubKeyPath);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
