package com.xuecheng.content.api;

import com.xuecheng.content.service.CoursePublishService;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
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
        Long companyId = 1232141425L;
        coursePublishService.coursePublish(companyId, courseId);
    }

}
