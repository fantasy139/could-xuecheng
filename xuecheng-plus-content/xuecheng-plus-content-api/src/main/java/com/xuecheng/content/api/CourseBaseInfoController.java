package com.xuecheng.content.api;

import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.base.model.RestResponse;
import com.xuecheng.content.model.dto.AddCourseDto;
import com.xuecheng.content.model.dto.CourseBaseInfoDto;
import com.xuecheng.content.model.dto.EditCourseDto;
import com.xuecheng.content.model.dto.QueryCourseParamsDto;
import com.xuecheng.content.model.po.CourseBase;
import com.xuecheng.content.service.CourseBaseService;
import com.xuecheng.base.utils.SecurityUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;

/**
 * @author fantasy
 * @description 课程基本信息 Controller
 * @date 2023-11-03
 */
@Api(tags = "课程管理")
@RestController
@RequestMapping("/course")
public class CourseBaseInfoController {

    @Resource
    private CourseBaseService courseBaseService;

    /**
     * 课程分页
     * @param pageParams
     * @param queryCourseParams
     * @return {@code PageResult<CourseBase> }
     * @author fantasy
     * @date 2023-11-07
     * @since version
     */
    @ApiOperation("课程分页")
    @PostMapping("/list")
    public PageResult<CourseBase> list(PageParams pageParams, @RequestBody QueryCourseParamsDto queryCourseParams){
        return courseBaseService.queryCourseBaseList(pageParams,queryCourseParams);
    }

    /**
     * 新增课程
     * @param addCourseDto
     * @return {@code CourseBaseInfoDto }
     * @author fantasy
     * @date 2023-11-07
     * @since version
     */
    @PostMapping
    @ApiOperation("新增课程")
    public CourseBaseInfoDto saveCourse(@RequestBody @Valid AddCourseDto addCourseDto){
        String companyId = SecurityUtil.getUser().getCompanyId();
        return courseBaseService.saveCourse(Long.valueOf(companyId),addCourseDto);
    }

    /**
     * 根据id查询课程信息
     * @param courseId
     * @return {@code CourseBaseInfoDto }
     * @author fantasy
     * @date 2023-11-07
     * @since version
     */
    @ApiOperation("新增课程")
    @GetMapping("/{courseId}")
    public CourseBaseInfoDto getCourseBaseInfo(@PathVariable Long courseId){
        return courseBaseService.getCourseBaseInfo(courseId);
    }

    /**
     * 修改课程
     * @param editCourseDto
     * @return {@code CourseBaseInfoDto }
     * @author fantasy
     * @date 2023-11-07
     * @since version
     */
    @PutMapping
    @ApiOperation("修改课程")
    public CourseBaseInfoDto updateCourse(@RequestBody @Valid EditCourseDto editCourseDto){
        String companyId = SecurityUtil.getUser().getCompanyId();
        return courseBaseService.updateCourse(Long.valueOf(companyId), editCourseDto);
    }

    /**
     * 删除课程
     * @param courseId
     * @return {@code RestResponse }
     * @author fantasy
     * @date 2023-11-07
     * @since version
     */
    @ApiOperation("删除课程")
    @DeleteMapping("/{courseId}")
    public RestResponse deleteCourseBaseInfo(@PathVariable Long courseId){
        String companyId = SecurityUtil.getUser().getCompanyId();
        courseBaseService.deleteCourseBaseInfo(Long.valueOf(companyId),courseId);
        return RestResponse.success();
    }
}
