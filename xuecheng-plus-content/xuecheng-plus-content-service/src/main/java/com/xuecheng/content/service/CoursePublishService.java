package com.xuecheng.content.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xuecheng.content.model.po.CoursePublish;

/**
 * <p>
 * 课程发布 服务类
 * </p>
 *
 * @author itcast
 * @since 2023-11-04
 */
public interface CoursePublishService extends IService<CoursePublish> {


    /**
     * 课程发布
     *
     * @param companyId
     * @param courseId
     * @author fantasy
     * @date 2024-01-14
     * @since version
     */
    void coursePublish(Long companyId, Long courseId);
}
