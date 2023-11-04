package com.xuecheng.content.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xuecheng.content.model.dto.CourseCategoryTreeDto;
import com.xuecheng.content.model.po.CourseCategory;

import java.util.List;

/**
 * <p>
 * 课程分类 服务类
 * </p>
 *
 * @author itcast
 * @since 2023-11-04
 */
public interface CourseCategoryService extends IService<CourseCategory> {

    /**
     * 查询树形课程分类
     * @return {@code List<CourseCategoryTreeDto> }
     * @author fantasy
     * @date 2023-11-04
     * @since version
     */
    List<CourseCategoryTreeDto> queryTreeNodes();
}
