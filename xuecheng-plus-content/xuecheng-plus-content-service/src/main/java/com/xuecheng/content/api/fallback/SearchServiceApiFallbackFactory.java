package com.xuecheng.content.api.fallback;

import com.xuecheng.content.api.SearchServiceApi;
import com.xuecheng.content.model.po.CourseIndex;
import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author fantasy
 * @description 搜索服务降级
 * @date 2024/1/20 16:08
 */

@Slf4j
@Component
public class SearchServiceApiFallbackFactory implements FallbackFactory<SearchServiceApi> {
    @Override
    public SearchServiceApi create(Throwable throwable) {
        return new SearchServiceApi() {
            @Override
            public Boolean add(CourseIndex courseIndex) {
                log.error("远程调用搜索服务异常,索引信息：{},熔断异常：{}", courseIndex, throwable.getMessage(), throwable);
                throw new RuntimeException("系统繁忙，请稍后再试");
            }
        };
    }
}
