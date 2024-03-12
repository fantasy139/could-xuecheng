package com.xuecheng.ucenter.controller;

import com.xuecheng.ucenter.service.CompanyService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author fantasy
 * @description 机构 controller
 * @date 2024/3/12 21:00
 */
@RestController
@RequestMapping("/company")
public class CompanyController {

    @Resource
    private CompanyService companyService;

    /**
     * 根据id获取机构名称
     * @param id
     * @return {@code String }
     * @author fantasy
     * @date 2024-03-12
     * @since version
     */
    @GetMapping("/getCompanyNameById")
    public String getCompanyNameById(String id){
        return companyService.getCompanyNameById(id);
    }
}
