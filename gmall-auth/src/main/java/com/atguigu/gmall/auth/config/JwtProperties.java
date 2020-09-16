package com.atguigu.gmall.auth.config;

import com.atguigu.gmall.common.utils.RsaUtils;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.annotation.PostConstruct;
import java.io.File;
import java.security.PrivateKey;
import java.security.PrivilegedAction;
import java.security.PublicKey;

@Data
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {
    private String pubKeyPath;
    private String priKeyPath;
    private String secret;
    private int expire;
    private String cookieName;
    private String unick;

    private PublicKey publicKey;
    private  PrivateKey privateKey;
    
    @PostConstruct
    public void init(){
        //获取公钥私钥文件
        try {
            File pubFile = new File(pubKeyPath);
            File priFile = new File(priKeyPath);

            if(!pubFile.exists() || !priFile.exists()){
                RsaUtils.generateKey(pubKeyPath, priKeyPath, secret);
            }
            publicKey = RsaUtils.getPublicKey(pubKeyPath);
            privateKey = RsaUtils.getPrivateKey(priKeyPath);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
