package com.xuecheng.content.api;

import com.xuecheng.content.api.fallback.SearchServiceApiFallbackFactory;
import com.xuecheng.content.api.fallback.UserServiceApiFallbackFactory;
import com.xuecheng.content.model.po.CourseIndex;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author fantasy
 * @description 远程调用搜索服务api
 * @date 2024/1/20 16:02
 */

@FeignClient(value = "auth-service", fallbackFactory = UserServiceApiFallbackFactory.class)
public interface UserServiceApi {

    /**
     * 根据id获取机构名称
     * @param id
     * @return {@code String }
     * @author fantasy
     * @date 2024-03-12
     * @since version
     */
    @PostMapping("/auth/company/getCompanyNameById")
    String getCompanyNameById(@RequestParam("id") String id);

}
