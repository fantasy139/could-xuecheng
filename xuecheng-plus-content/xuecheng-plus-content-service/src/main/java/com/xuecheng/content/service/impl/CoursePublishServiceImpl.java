package com.xuecheng.content.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xuecheng.base.execption.CommonError;
import com.xuecheng.base.execption.XueChengPlusException;
import com.xuecheng.content.mapper.CourseBaseMapper;
import com.xuecheng.content.mapper.CoursePublishMapper;
import com.xuecheng.content.mapper.CoursePublishPreMapper;
import com.xuecheng.content.model.po.CourseBase;
import com.xuecheng.content.model.po.CoursePublish;
import com.xuecheng.content.model.po.CoursePublishPre;
import com.xuecheng.content.service.CoursePublishService;
import com.xuecheng.messagesdk.model.po.MqMessage;
import com.xuecheng.messagesdk.service.MqMessageService;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

/**
 * <p>
 * 课程发布 服务实现类
 * </p>
 *
 * @author itcast
 */
@Slf4j
@Service
public class CoursePublishServiceImpl extends ServiceImpl<CoursePublishMapper, CoursePublish> implements CoursePublishService {

    @Resource
    private CoursePublishPreMapper coursePublishPreMapper;

    @Resource
    private CourseBaseMapper courseBaseMapper;

    @Resource
    private MqMessageService mqMessageService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void coursePublish(Long companyId, Long courseId) {
        // 校验有没有这个预发布的课程
        CoursePublishPre coursePublishPre = coursePublishPreMapper.selectById(courseId);
        if (coursePublishPre == null){
            XueChengPlusException.cast("该课程不存在，courseId：" + courseId);
        }

        // 本机构只允许提交本机构的课程
        if(!coursePublishPre.getCompanyId().equals(companyId)){
            XueChengPlusException.cast("不允许提交其它机构的课程。");
        }

        // 审核通过方可发布
        if(!"202004".equals(coursePublishPre.getStatus())){
            XueChengPlusException.cast("操作失败，课程审核通过方可发布。");
        }

        // 插入或更新发布表
        CoursePublish coursePublish = new CoursePublish();
        BeanUtils.copyProperties(coursePublishPre, coursePublish);
        saveOrUpdate(coursePublish);

        // 更新course_base表的课程发布状态为：已发布
        CourseBase courseBase = new CourseBase();
        courseBase.setId(courseId);
        courseBase.setStatus("203002");
        courseBaseMapper.updateById(courseBase);

        //保存消息表
        saveCoursePublishMessage(courseId);

    }

    private void saveCoursePublishMessage(Long courseId) {
        MqMessage mqMessage = mqMessageService.addMessage("course_publish", String.valueOf(courseId), null, null);
        if(mqMessage==null){
            XueChengPlusException.cast(CommonError.UNKNOWN_ERROR);
        }
    }
}
