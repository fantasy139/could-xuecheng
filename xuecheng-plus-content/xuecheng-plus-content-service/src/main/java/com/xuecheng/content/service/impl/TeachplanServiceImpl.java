package com.xuecheng.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xuecheng.base.execption.XueChengPlusException;
import com.xuecheng.base.utils.SecurityUtil;
import com.xuecheng.content.mapper.TeachplanMapper;
import com.xuecheng.content.model.dto.BindTeachplanMediaDto;
import com.xuecheng.content.model.dto.CourseCategoryTreeDto;
import com.xuecheng.content.model.dto.SaveTeachplanDto;
import com.xuecheng.content.model.dto.TeachplanDto;
import com.xuecheng.content.model.po.CourseCategory;
import com.xuecheng.content.model.po.Teachplan;
import com.xuecheng.content.model.po.TeachplanMedia;
import com.xuecheng.content.service.TeachplanMediaService;
import com.xuecheng.content.service.TeachplanService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author fantasy
 * @description 课程计划service
 * @date 2023-11-09
 */
@Slf4j
@Service
public class TeachplanServiceImpl extends ServiceImpl<TeachplanMapper, Teachplan> implements TeachplanService {

    @Resource
    private TeachplanMediaService teachplanMediaService;
    private final static Long ROOT_NODE = 0L;
    /**
     * 二级课程计划
     */
    private final static Integer SECONDARY_TEACHER_PLAN = 2;
    @Override
    public List<TeachplanDto> getTreeNodes(Long courseId) {
        LambdaQueryWrapper<Teachplan> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(courseId!= null, Teachplan::getCourseId, courseId)
                .orderByAsc(Teachplan::getParentid)
                .orderByAsc(Teachplan::getOrderby);
        List<Teachplan> teachplanList = list(wrapper);

        //拿到第一级的id
        List<Long> idList = teachplanList.stream()
                .filter(teachplan -> ROOT_NODE.equals(teachplan.getParentid()))
                .map(Teachplan::getId).collect(Collectors.toList());
        //这个map一定要是CourseCategoryTreeDto的，因为下面的递归插入的是这里面的对象
        Map<Long, TeachplanDto> teachplanDtoMap = new HashMap<>(teachplanList.size());
        // 先构建一个以id为键的映射，便于后续查找
        for (Teachplan teachplan : teachplanList) {
            TeachplanDto teachplanDto = new TeachplanDto();
            BeanUtils.copyProperties(teachplan, teachplanDto);
            teachplanDtoMap.put(teachplan.getId(), teachplanDto);
        }
        List<TeachplanDto> teachplanDtoList = new ArrayList<>();
        teachplanList.stream().filter(teachplan -> !ROOT_NODE.equals(teachplan.getParentid())).forEach(teachplan -> {
            //先查出当前teachplan的父级
            TeachplanDto teachplanParentDto = teachplanDtoMap.get(teachplan.getParentid());
            //如果父级id的子集为空，那么先初始化
            if (teachplanParentDto.getTeachPlanTreeNodes() == null){
                teachplanParentDto.setTeachPlanTreeNodes(new ArrayList<>());
            }
            //将当前teachplan添加到其父级teachplanParentDto的子集中
            TeachplanDto teachplanDto = new TeachplanDto();
            BeanUtils.copyProperties(teachplan,teachplanDto);
            //查出当前teachplanDto的媒资信息并赋值给teachplanMedia
            List<TeachplanMedia> teachplanMediaList = teachplanMediaService.list(Wrappers.<TeachplanMedia>lambdaQuery()
                    .eq(TeachplanMedia::getTeachplanId, teachplan.getId())
                    .orderByDesc(TeachplanMedia::getCreateDate)
                    .last("limit 1"));
            if (!CollectionUtils.isEmpty(teachplanMediaList)){
                //不为空时，才赋值
                teachplanDto.setTeachplanMedia(teachplanMediaList.get(0));
            }
            teachplanParentDto.getTeachPlanTreeNodes().add(teachplanDto);
        });
        //将map里的一级节点抽出来返回就行
        idList.forEach(id -> {
            if(teachplanDtoMap.containsKey(id)){
                teachplanDtoList.add(teachplanDtoMap.get(id));
            }
        });
        return teachplanDtoList;
    }

    @Override
    public void saveTeachplan(SaveTeachplanDto teachplanDto) {
        if(teachplanDto.getId() == null){
            Teachplan teachplan = new Teachplan();
            BeanUtils.copyProperties(teachplanDto,teachplan);
            teachplan.setOrderby(getCount(teachplanDto));
            save(teachplan);
        }else {
            Teachplan teachplan = new Teachplan();
            BeanUtils.copyProperties(teachplanDto,teachplan);
            updateById(teachplan);
        }
    }

    @Override
    public void moveUpById(Long id) {
        //判断是否存在
        Teachplan teachplan = isExist(id);
        //排好序，查出当前对象的上一个对象
        Integer orderby = teachplan.getOrderby();
        LambdaQueryWrapper<Teachplan> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Teachplan::getCourseId, teachplan.getCourseId())
                .eq(Teachplan::getParentid, teachplan.getParentid())
                .ne(Teachplan::getId, id)
                .lt(Teachplan::getOrderby, orderby)
                .orderByDesc(Teachplan::getOrderby)
                .last("limit 1");
        Teachplan newTeachplan = getOne(wrapper);
        teachplan.setOrderby(newTeachplan.getOrderby());
        newTeachplan.setOrderby(orderby);
        updateById(teachplan);
        updateById(newTeachplan);
    }

    private int getCount(SaveTeachplanDto teachplanDto) {
        LambdaQueryWrapper<Teachplan> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Teachplan::getParentid, teachplanDto.getParentid())
                .eq(Teachplan::getCourseId, teachplanDto.getCourseId());
        return count(wrapper) + 1;
    }

    public Teachplan isExist(Long id){
        Teachplan teachplan = getById(id);
        if (teachplan == null){
            log.error("该对象不存在，id：{}", id);
            XueChengPlusException.cast("该对象不存在，id：" + id);
        }
        return teachplan;
    }

    @Override
    public void moveDownById(Long id) {
        //判断是否存在
        Teachplan teachplan = isExist(id);
        //排好序，查出当前对象的下一个对象
        Integer orderby = teachplan.getOrderby();
        LambdaQueryWrapper<Teachplan> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Teachplan::getCourseId, teachplan.getCourseId())
                .eq(Teachplan::getParentid, teachplan.getParentid())
                .ne(Teachplan::getId, id)
                .ge(Teachplan::getOrderby, orderby)
                .orderByDesc(Teachplan::getOrderby)
                .last("limit 1");
        Teachplan newTeachplan = getOne(wrapper);
        teachplan.setOrderby(newTeachplan.getOrderby());
        newTeachplan.setOrderby(orderby);
        updateById(teachplan);
        updateById(newTeachplan);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void associationMedia(BindTeachplanMediaDto bindTeachplanMediaDto) {
        Long teachplanId = bindTeachplanMediaDto.getTeachplanId();
        Teachplan teachplan = isExist(teachplanId);
        if (!SECONDARY_TEACHER_PLAN.equals(teachplan.getGrade())){
            XueChengPlusException.cast("只允许第二级教学计划绑定媒资");
        }
        // 先将这条课程计划原有的媒资信息删除
        teachplanMediaService.remove(Wrappers.<TeachplanMedia>lambdaQuery()
                .eq(TeachplanMedia::getTeachplanId, teachplanId));
        TeachplanMedia teachplanMedia = new TeachplanMedia();
        BeanUtils.copyProperties(bindTeachplanMediaDto, teachplanMedia);
        teachplanMedia.setMediaFilename(bindTeachplanMediaDto.getFileName());
        teachplanMedia.setCourseId(teachplan.getCourseId());
        teachplanMedia.setCreateDate(LocalDateTime.now());
        teachplanMedia.setCreatePeople(SecurityUtil.getUser().getId());
        teachplanMediaService.save(teachplanMedia);
    }

    @Override
    public void disassociationMedia(Long teachplanId, String mediaId) {
        TeachplanMedia teachplanMedia = teachplanMediaService.getOne(Wrappers.<TeachplanMedia>lambdaQuery()
                .eq(TeachplanMedia::getTeachplanId, teachplanId)
                .eq(TeachplanMedia::getMediaId, mediaId));
        if (null == teachplanMedia){
            XueChengPlusException.cast("删除失败，不存在该关联关系");
        }
        teachplanMediaService.removeById(teachplanMedia.getId());
    }
}
