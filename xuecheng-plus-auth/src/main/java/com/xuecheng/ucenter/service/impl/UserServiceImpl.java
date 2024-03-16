package com.xuecheng.ucenter.service.impl;

import com.alibaba.fastjson.JSON;
import com.xuecheng.ucenter.model.dto.AuthParamsDto;
import com.xuecheng.ucenter.model.dto.XcUserExt;
import com.xuecheng.ucenter.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.List;


/**
 * @author fantasy
 * @description 查询用户信息
 * @date 2024/3/11 20:23
 */
@Component
public class UserServiceImpl implements UserDetailsService {

    @Autowired
    private ApplicationContext applicationContext;

    @Override
    public UserDetails loadUserByUsername(String s) throws UsernameNotFoundException {

        AuthParamsDto authParamsDto;
        try {
            authParamsDto = JSON.parseObject(s, AuthParamsDto.class);
        } catch (Exception e) {
            throw new RuntimeException("请求认证参数不符合要求");
        }

        // 根据认证类型获取对应类型的bean
        String beanName = authParamsDto.getAuthType() + "_authService";
        AuthService authService = applicationContext.getBean(beanName, AuthService.class);
        // 认证
        XcUserExt xcUserExt = authService.execute(authParamsDto);

        String userString = JSON.toJSONString(xcUserExt);
        List<String> permissionList = xcUserExt.getPermissionList();

        UserDetails userDetails = User.withUsername(userString)
                .password(xcUserExt.getPassword()).authorities(permissionList.toArray(new String[0])).build();
        return userDetails;
    }
}
