package com.xuecheng.content.api;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.xuecheng.content.model.dto.CourseCategoryTreeDto;
import com.xuecheng.content.service.CourseCategoryService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;


/**
 * @author fantasy
 * @description 课程分类
 * @date 2023-11-04
 */
@Api(tags = "课程分类管理")
@Slf4j
@RestController
@RequestMapping("/course-category")
public class CourseCategoryController {

    @Resource
    private CourseCategoryService  courseCategoryService;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private RedissonClient redissonClient;

    /**
     * @return {@code List<CourseCategoryTreeDto> }
     * @author fantasy
     * @date 2023-11-04
     * @since version
     */
    @ApiOperation("查询树形课程分类")
    @GetMapping("/tree-nodes")
    public List<CourseCategoryTreeDto> queryTreeNodes() {
        // 使用redisson实现分布式锁
        ValueOperations valueOperations = redisTemplate.opsForValue();
        Object o = valueOperations.get("tree-nodes");
        List<CourseCategoryTreeDto> courseCategoryTreeDtoList;
        if (null != o){
            if ("null".equals(o)){
                return null;
            }
            courseCategoryTreeDtoList = JSONObject.parseArray(o.toString(), CourseCategoryTreeDto.class);
            return courseCategoryTreeDtoList;
        }
        RLock treeNodesLock = redissonClient.getLock("treeNodesLock");
        // 获取分布式锁
        treeNodesLock.lock();
        try {
            // 因为整个try被分布式锁锁住了，所以一次只能有一个线程进来查数据库
            // 查完之后第二个进来就可以直接查缓存了，所以这里再写一遍查缓存
            Object data = valueOperations.get("tree-nodes");
            if (null != data){
                if ("null".equals(data)){
                    return null;
                }
                courseCategoryTreeDtoList = JSONObject.parseArray(data.toString(), CourseCategoryTreeDto.class);
                return courseCategoryTreeDtoList;
            }
            courseCategoryTreeDtoList = courseCategoryService.queryTreeNodes();
            valueOperations.set("tree-nodes", JSONObject.toJSONString(courseCategoryTreeDtoList), Duration.ofDays(1 + new Random().nextInt(100)));
            return courseCategoryTreeDtoList;
        }finally {
            // 手动释放锁
            treeNodesLock.unlock();
        }
    }
}
