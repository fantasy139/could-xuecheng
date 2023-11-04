package com.xuecheng.content.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
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
}
