package com.xuecheng.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.api.R;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xuecheng.base.execption.CommonError;
import com.xuecheng.base.execption.XueChengPlusException;
import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.content.mapper.CourseBaseMapper;
import com.xuecheng.content.model.dto.AddCourseDto;
import com.xuecheng.content.model.dto.CourseBaseInfoDto;
import com.xuecheng.content.model.dto.EditCourseDto;
import com.xuecheng.content.model.dto.QueryCourseParamsDto;
import com.xuecheng.content.model.po.CourseBase;
import com.xuecheng.content.model.po.CourseCategory;
import com.xuecheng.content.model.po.CourseMarket;
import com.xuecheng.content.service.CourseBaseService;
import com.xuecheng.content.service.CourseCategoryService;
import com.xuecheng.content.service.CourseMarketService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
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
    private CourseMarketService courseMarketService;

    @Resource
    private CourseCategoryService courseCategoryService;

    private static final String CHARGE = "201001";

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
        Page<CourseBase> courseBasePage = page(new Page<>(pageNo, pageSize), wrapper);
        List<CourseBase> courseBaseList = courseBasePage.getRecords();
        return new PageResult<>(courseBaseList,courseBasePage.getCurrent(),pageNo,pageSize);
    }

    @Transactional
    @Override
    public CourseBaseInfoDto saveCourse(Long companyId, AddCourseDto addCourseDto) {
        //向课程基本信息表插入数据
        CourseBase courseBase = new CourseBase();
        BeanUtils.copyProperties(addCourseDto,courseBase);
        //TODO 后续要根据companyId查询出companyName一起传入courseBase中
        courseBase.setCompanyId(companyId);
        courseBase.setCreateDate(LocalDateTime.now());
        courseBase.setAuditStatus("202002");
        courseBase.setStatus("203001");
        save(courseBase);
        //课程营销信息表的数据存在就更新，不存在就插入
        CourseMarket courseMarket = new CourseMarket();
        BeanUtils.copyProperties(addCourseDto,courseMarket);
        courseMarket.setId(courseBase.getId());
        saveOrUpdateCourseMarket(courseMarket);
        //从数据库中查询出详细信息并返回
        CourseBaseInfoDto courseBaseInfoDto = getCourseBaseInfo(courseBase.getId());
        if (courseBaseInfoDto == null){
            XueChengPlusException.cast("详细信息" + CommonError.QUERY_NULL);
        }
        return courseBaseInfoDto;
    }
    @Override
    public CourseBaseInfoDto getCourseBaseInfo(Long courseId){
        CourseBaseInfoDto courseBaseInfoDto = new CourseBaseInfoDto();
        CourseBase courseBase = getById(courseId);
        if (courseBase == null){
            return null;
        }
        //查出课程分类的名称
        courseBaseInfoDto.setMtName(courseCategoryService.getById(courseBase.getMt()).getName());
        courseBaseInfoDto.setStName(courseCategoryService.getById(courseBase.getSt()).getName());
        CourseMarket courseMarket = courseMarketService.getById(courseId);
        if (courseMarket == null){
            return null;
        }
        BeanUtils.copyProperties(courseBase,courseBaseInfoDto);
        BeanUtils.copyProperties(courseMarket,courseBaseInfoDto);
        return courseBaseInfoDto;
    }

    @Override
    public CourseBaseInfoDto updateCourse(Long companyId, EditCourseDto editCourseDto) {
        //校验
        CourseBase courseBase = getCourseBase(companyId,editCourseDto.getId());

        CourseMarket courseMarket = new CourseMarket();
        BeanUtils.copyProperties(editCourseDto,courseBase);
        BeanUtils.copyProperties(editCourseDto,courseMarket);
        courseBase.setCreateDate(LocalDateTime.now());
        updateById(courseBase);
        saveOrUpdateCourseMarket(courseMarket);
        //从数据库中查询出详细信息并返回
        CourseBaseInfoDto courseBaseInfoDto = getCourseBaseInfo(courseBase.getId());
        if (courseBaseInfoDto == null){
            XueChengPlusException.cast("详细信息" + CommonError.QUERY_NULL);
        }
        return courseBaseInfoDto;
    }

    private CourseBase getCourseBase(Long companyId, Long courseId) {
        //判断是否存在该课程
        CourseBase courseBase = getById(courseId);
        if (courseBase == null){
            XueChengPlusException.cast("不存在该课程");
        }
        //只能对自己机构的课程进行操作
        if (!companyId.equals(courseBase.getCompanyId())){
            XueChengPlusException.cast("只能对自己机构的课程进行操作");
        }
        return courseBase;
    }

    @Override
    public void deleteCourseBaseInfo(Long companyId, Long courseId) {
        //校验
        CourseBase courseBase = getCourseBase(companyId, courseId);
        //删除课程基本信息和营销信息
        removeById(courseId);
        CourseMarket courseMarket = courseMarketService.getById(courseId);
        if (courseMarket != null) {
            courseMarketService.removeById(courseId);
        }
    }

    /**
     * 保存课程营销信息
     * @param courseMarket
     * @author fantasy
     * @date 2023-11-05
     * @since version
     */
    private void saveOrUpdateCourseMarket(CourseMarket courseMarket){
        //收费规则
        String charge = courseMarket.getCharge();
        if(StringUtils.isBlank(charge)){
            throw new RuntimeException("收费规则没有选择");
        }
        //收费规则为收费
        if(CHARGE.equals(charge)){
            if(courseMarket.getPrice() == null || courseMarket.getPrice().floatValue()<=0){
                throw new RuntimeException("课程为收费价格不能为空且必须大于0");
            }
        }

        if (courseMarketService.getById(courseMarket.getId()) != null){
            courseMarketService.updateById(courseMarket);
        }else {
            courseMarketService.save(courseMarket);
        }
    }
}
