package com.xuecheng.ucenter.service;

/**
 * @author fantasy
 * @description 机构 service
 * @date 2024/3/12 21:03
 */
public interface CompanyService {

    /**
     * 根据机构id获取机构名称
     * @param id
     * @return {@code String }
     * @author fantasy
     * @date 2024-03-12
     * @since version
     */
    String getCompanyNameById(String id);
}
