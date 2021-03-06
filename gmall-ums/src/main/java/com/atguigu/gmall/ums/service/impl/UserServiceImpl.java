package com.atguigu.gmall.ums.service.impl;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;

import com.atguigu.gmall.ums.mapper.UserMapper;
import com.atguigu.gmall.ums.entity.UserEntity;
import com.atguigu.gmall.ums.service.UserService;
import org.springframework.util.CollectionUtils;


@Service("userService")
public class UserServiceImpl extends ServiceImpl<UserMapper, UserEntity> implements UserService {

    @Override
    public PageResultVo queryPage(PageParamVo paramVo) {
        IPage<UserEntity> page = this.page(
                paramVo.getPage(),
                new QueryWrapper<UserEntity>()
        );

        return new PageResultVo(page);
    }

    @Override
    public Boolean checkData(String data, Integer type) {
        QueryWrapper<UserEntity> wrapper = new QueryWrapper<>();
        switch (type) {
            case 1:
                wrapper.eq("username", data);
                break;
            case 2:
                wrapper.eq("phone", data);
                break;
            case 3:
                wrapper.eq("email", data);
                break;
            default:
                return null;
        }
        return baseMapper.selectCount(wrapper) == 0;

    }

    @Override
    public void register(UserEntity user, String code) {
        //1.校验短信验证码

        //2.生成盐
        String salt = StringUtils.substring(UUID.randomUUID().toString(), 0, 6);
        user.setSalt(salt);
        //3.对密码加盐加密
        user.setPassword(DigestUtils.md5Hex(user.getPassword() + salt));

        //4，新增用户
        user.setLevelId(1L);
        user.setSourceType(1);
        user.setIntegration(1000);
        user.setGrowth(1000);
        user.setStatus(1);
        user.setCreateTime(new Date());

        save(user);
        // 5删除redis中验证码

    }

    @Override
    public UserEntity queryUser(String loginName, String password) {

        List<UserEntity> userEntities = list(new QueryWrapper<UserEntity>()
                .eq("username", loginName)
                .or()
                .eq("email", loginName)
                .or()
                .eq("phone", loginName));

        if (CollectionUtils.isEmpty(userEntities)) {
            return null;
        }
        String pwd = null;
        for (UserEntity userEntity : userEntities) {
            String salt = userEntity.getSalt();
            pwd = DigestUtils.md5Hex(password + salt);

            if (StringUtils.equals(pwd, userEntity.getPassword())) {
                return userEntity;
            }

        }
        return null;
    }

}