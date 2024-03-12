package com.xuecheng.ucenter.service.impl;

import com.xuecheng.ucenter.mapper.XcCompanyMapper;
import com.xuecheng.ucenter.model.po.XcCompany;
import com.xuecheng.ucenter.service.CompanyService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author fantasy
 * @description 机构service实现类
 * @date 2024/3/12 21:04
 */
@Service
public class CompanyServiceImpl implements CompanyService {

    @Resource
    private XcCompanyMapper companyMapper;
    @Override
    public String getCompanyNameById(String id) {
        XcCompany xcCompany = companyMapper.selectById(id);
        if (null == xcCompany){
            return null;
        }
        return xcCompany.getName();
    }
}
