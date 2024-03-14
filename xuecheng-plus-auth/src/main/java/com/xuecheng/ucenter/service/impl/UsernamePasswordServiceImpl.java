package com.xuecheng.ucenter.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.xuecheng.ucenter.mapper.XcUserMapper;
import com.xuecheng.ucenter.model.dto.AuthParamsDto;
import com.xuecheng.ucenter.model.dto.XcUserExt;
import com.xuecheng.ucenter.model.po.XcUser;
import com.xuecheng.ucenter.service.AuthService;
import org.springframework.beans.BeanUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import javax.annotation.Resource;

/**
 * @author fantasy
 * @description 账号密码认证实现类
 * @date 2024/3/12 21:58
 */
@Service("password_authService")
public class UsernamePasswordServiceImpl implements AuthService {

    @Resource
    private XcUserMapper xcUserMapper;

    @Resource
    private PasswordEncoder passwordEncoder;

    @Override
    public XcUserExt execute(AuthParamsDto authParamsDto) {
        // 查询数据库
        XcUser user = xcUserMapper.selectOne(Wrappers.<XcUser>lambdaQuery().eq(XcUser::getUsername, authParamsDto.getUsername()));
        Assert.notNull(user, "用户不存在");

        boolean matches = passwordEncoder.matches(authParamsDto.getPassword(), user.getPassword());
        Assert.isTrue(matches, "密码错误");

        XcUserExt xcUserExt = new XcUserExt();
        BeanUtils.copyProperties(user, xcUserExt);
        return xcUserExt;
    }
}
