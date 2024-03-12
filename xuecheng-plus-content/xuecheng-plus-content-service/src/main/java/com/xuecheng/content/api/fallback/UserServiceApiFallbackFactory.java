package com.xuecheng.content.api.fallback;

import com.xuecheng.content.api.UserServiceApi;
import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author fantasy
 * @description auth服务降级
 * @date 2024/3/12 21:07
 */
@Slf4j
@Component
public class UserServiceApiFallbackFactory implements FallbackFactory<UserServiceApi> {
    @Override
    public UserServiceApi create(Throwable throwable) {
        return new UserServiceApi() {
            @Override
            public String getCompanyNameById(String id) {
                log.error("远程调用auth服务异常,id：{},熔断异常：{}", id, throwable.getMessage(), throwable);
                throw new RuntimeException("系统繁忙，请稍后再试");
            }
        };
    }
}
