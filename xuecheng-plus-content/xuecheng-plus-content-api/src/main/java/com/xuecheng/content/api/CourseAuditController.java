package com.xuecheng.content.api;

import com.xuecheng.content.service.CourseAuditService;
import io.swagger.annotations.ApiImplicitParam;
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
@RequestMapping("/courseaudit")
public class CourseAuditController {

    @Resource
    private CourseAuditService  courseAuditService;

    /**
     * 课程提交审核
     * @param courseId
     * @author fantasy
     * @date 2024-01-08
     * @since version
     */
    @ResponseBody
    @ApiOperation("课程提交审核")
    @ApiImplicitParam(name = "课程id", value = "courseId", required = true, dataType = "Long")
    @PostMapping("/commit/{courseId}")
    public void commitAudit(@PathVariable("courseId") Long courseId){
        Long companyId = 1232141425L;
        courseAuditService.commitAudit(companyId, courseId);
    }

}
