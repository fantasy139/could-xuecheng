package com.xuecheng.ucenter.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.xuecheng.ucenter.mapper.XcUserMapper;
import com.xuecheng.ucenter.model.po.SecurityUser;
import com.xuecheng.ucenter.model.po.XcUser;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;

/**
 * @author fantasy
 * @description 查询用户信息
 * @date 2024/3/11 20:23
 */
@Component
public class UserServiceImpl implements UserDetailsService {

    @Resource
    private XcUserMapper xcUserMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 查询数据库
        XcUser user = xcUserMapper.selectOne(Wrappers.<XcUser>lambdaQuery().eq(XcUser::getUsername, username));
        if (null == user){
            return null;
        }
        String password = user.getPassword();
        user.setPassword(null);
        String userString = JSON.toJSONString(user);
        UserDetails userDetails = User.withUsername(userString)
                .password(password).authorities(new String[]{"test"}).build();
        return userDetails;
    }
}
