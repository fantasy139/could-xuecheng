package com.xuecheng.content.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.content.model.dto.AddCourseDto;
import com.xuecheng.content.model.dto.CourseBaseInfoDto;
import com.xuecheng.content.model.dto.EditCourseDto;
import com.xuecheng.content.model.dto.QueryCourseParamsDto;
import com.xuecheng.content.model.po.CourseBase;

/**
 * <p>
 * 课程基本信息 服务类
 * </p>
 *
 * @author itcast
 * @since 2023-11-04
 */
public interface CourseBaseService extends IService<CourseBase> {

    /**
     * 分页查询
     *
     * @param pageParams
     * @param queryCourseParams
     * @return {@code PageResult<CourseBase> }
     * @author fantasy
     * @date 2023-11-04
     * @since version
     */
    PageResult<CourseBase> queryCourseBaseList(PageParams pageParams, QueryCourseParamsDto queryCourseParams);

    /**
     * 新增课程
     *
     * @param companyId
     * @param addCourseDto
     * @return {@code CourseBaseInfoDto }
     * @author fantasy
     * @date 2023-11-05
     * @since version
     */
    CourseBaseInfoDto saveCourse(Long companyId, AddCourseDto addCourseDto);

    /**
     * 根据id查询课程信息
     * @param courseId
     * @return {@code CourseBaseInfoDto }
     * @author fantasy
     * @date 2023-11-07
     * @since version
     */
    CourseBaseInfoDto getCourseBaseInfo(Long courseId);

    /**
     * 修改课程信息
     *
     * @param companyId
     * @param editCourseDto
     * @return {@code CourseBaseInfoDto }
     * @author fantasy
     * @date 2023-11-07
     * @since version
     */
    CourseBaseInfoDto updateCourse(Long companyId, EditCourseDto editCourseDto);

    /**
     * 删除课程
     *
     * @param companyId
     * @param courseId
     * @author fantasy
     * @date 2023-11-07
     * @since version
     */
    void deleteCourseBaseInfo(Long companyId, Long courseId);
}
