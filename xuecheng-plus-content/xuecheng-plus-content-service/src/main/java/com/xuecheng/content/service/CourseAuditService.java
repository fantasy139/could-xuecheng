package com.xuecheng.content.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xuecheng.content.model.po.CourseAudit;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author fantasy
 * @since 2024-01-08
 */
public interface CourseAuditService extends IService<CourseAudit> {

    /**
     * 课程提交审核
     * @param companyId
     * @param courseId
     * @author fantasy
     * @date 2024-01-08
     * @since version
     */
    void commitAudit(Long companyId, Long courseId);
}
