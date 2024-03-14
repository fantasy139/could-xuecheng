package com.xuecheng.ucenter.service;

import com.xuecheng.ucenter.model.dto.AuthParamsDto;
import com.xuecheng.ucenter.model.dto.XcUserExt;

/**
 * @author fantasy
 * @description 自定义认证接口
 * @date 2024/3/12 21:58
 */
public interface AuthService {
    /**
     * 认证方法
     * @param authParamsDto
     * @return {@code XcUserExt }
     * @author fantasy
     * @date 2024-03-12
     * @since version
     */
    XcUserExt execute(AuthParamsDto authParamsDto);
}
