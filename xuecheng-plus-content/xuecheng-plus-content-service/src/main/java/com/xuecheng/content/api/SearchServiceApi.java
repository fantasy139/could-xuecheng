package com.xuecheng.content.api;

import com.xuecheng.content.api.fallback.SearchServiceApiFallbackFactory;
import com.xuecheng.content.model.po.CourseIndex;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * @author fantasy
 * @description 远程调用搜索服务api
 * @date 2024/1/20 16:02
 */

@FeignClient(value = "search", fallbackFactory = SearchServiceApiFallbackFactory.class)
public interface SearchServiceApi {

    @PostMapping("/search/index/course")
    Boolean add(@RequestBody CourseIndex courseIndex);

}
