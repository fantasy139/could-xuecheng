package com.xuecheng.content.api;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.xuecheng.content.model.po.CourseTeacher;
import com.xuecheng.content.service.CourseTeacherService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author fantasy
 * @description TODO
 * @date 2024/1/8 21:32
 */
@Api(tags = "教师管理")
@Slf4j
@RestController
@RequestMapping("/courseTeacher")
public class CourseTeacherController {

    @Resource
    private CourseTeacherService courseTeacherService;

    @ApiOperation("查询教师列表")
    @GetMapping("/list/{courseId}")
    public List<CourseTeacher> getTeacherList(@PathVariable("courseId") Long courseId){
        return courseTeacherService.list(Wrappers.<CourseTeacher>lambdaQuery().eq(CourseTeacher::getCourseId, courseId));
    }
}
