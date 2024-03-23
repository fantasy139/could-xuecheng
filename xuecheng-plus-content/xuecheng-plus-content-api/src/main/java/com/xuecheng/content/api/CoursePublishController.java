package com.xuecheng.content.api;

import com.xuecheng.content.service.CoursePublishService;
import com.xuecheng.content.util.SecurityUtil;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;


/**
* @description 
* @date 2024-01-08
* @author fantasy
*/
@Slf4j
@RestController
@RequestMapping("/coursepublish")
public class CoursePublishController {

    @Resource
    private CoursePublishService coursePublishService;

    /**
     * 课程发布
     * @param courseId
     * @author fantasy
     * @date 2024-01-14
     * @since version
     */
    @ApiOperation("课程发布")
    @ResponseBody
    @PostMapping ("/{courseId}")
    public void coursePublish(@PathVariable("courseId") Long courseId){
        String companyId = SecurityUtil.getUser().getCompanyId();
        coursePublishService.coursePublish(Long.valueOf(companyId), courseId);
    }

}
