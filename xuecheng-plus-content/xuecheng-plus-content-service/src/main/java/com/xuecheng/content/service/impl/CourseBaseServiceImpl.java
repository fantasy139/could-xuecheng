package com.xuecheng.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.content.mapper.CourseBaseMapper;
import com.xuecheng.content.model.dto.QueryCourseParamsDto;
import com.xuecheng.content.model.po.CourseBase;
import com.xuecheng.content.service.CourseBaseService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * <p>
 * 课程基本信息 服务实现类
 * </p>
 *
 * @author itcast
 */
@Slf4j
@Service
public class CourseBaseServiceImpl extends ServiceImpl<CourseBaseMapper, CourseBase> implements CourseBaseService {

    @Resource
    private CourseBaseMapper courseBaseMapper;

    @Override
    public PageResult<CourseBase> queryCourseBaseList(PageParams pageParams, QueryCourseParamsDto queryCourseParams) {
        String courseName = queryCourseParams.getCourseName();
        String auditStatus = queryCourseParams.getAuditStatus();
        String publishStatus = queryCourseParams.getPublishStatus();
        Long pageNo = pageParams.getPageNo();
        Long pageSize = pageParams.getPageSize();
        LambdaQueryWrapper<CourseBase> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.isNotBlank(courseName),CourseBase::getName,courseName)
                .eq(StringUtils.isNotBlank(auditStatus),CourseBase::getAuditStatus,auditStatus)
                .eq(StringUtils.isNotBlank(publishStatus),CourseBase::getStatus,publishStatus);
        Page<CourseBase> courseBasePage = courseBaseMapper.selectPage(new Page<>(pageNo, pageSize), wrapper);
        List<CourseBase> courseBaseList = courseBasePage.getRecords();
        return new PageResult<>(courseBaseList,courseBasePage.getCurrent(),pageNo,pageSize);
    }
}
