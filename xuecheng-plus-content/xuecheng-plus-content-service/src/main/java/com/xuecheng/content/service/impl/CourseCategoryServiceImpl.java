package com.xuecheng.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xuecheng.content.mapper.CourseCategoryMapper;
import com.xuecheng.content.model.dto.CourseCategoryTreeDto;
import com.xuecheng.content.model.po.CourseCategory;
import com.xuecheng.content.service.CourseCategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * <p>
 * 课程分类 服务实现类
 * </p>
 *
 * @author itcast
 */
@Slf4j
@Service
public class CourseCategoryServiceImpl extends ServiceImpl<CourseCategoryMapper, CourseCategory> implements CourseCategoryService {

    private final static String ROOT_NODE = "1";

    @Override
    public List<CourseCategoryTreeDto> queryTreeNodes() {
        List<CourseCategory> courseCategoryList = list(new LambdaQueryWrapper<CourseCategory>()
                .eq(CourseCategory::getIsShow,"1")
                .ne(CourseCategory::getId,ROOT_NODE)
                .orderByAsc(CourseCategory::getParentid));
        List<String> idList = courseCategoryList.stream()
                .filter(courseCategory -> ROOT_NODE.equals(courseCategory.getParentid()))
                .map(CourseCategory::getId).collect(Collectors.toList());
        //这个map一定要是CourseCategoryTreeDto的，因为下面的递归插入的是这里面的对象
        Map<String, CourseCategoryTreeDto> categoryMap = new HashMap<>(courseCategoryList.size());
        // 先构建一个以id为键的映射，便于后续查找
        for (CourseCategory category : courseCategoryList) {
            CourseCategoryTreeDto categoryTreeDto = new CourseCategoryTreeDto();
            BeanUtils.copyProperties(category, categoryTreeDto);
            categoryMap.put(category.getId(), categoryTreeDto);
        }
        List<CourseCategoryTreeDto> courseCategoryTreeDtoList = new ArrayList<>();
        courseCategoryList.stream()
                .filter(courseCategory -> !ROOT_NODE.equals(courseCategory.getParentid()))
                .forEach(courseCategory -> {
                    //先查出当前courseCategory的父级
                    CourseCategoryTreeDto courseCategoryTreeDto = categoryMap.get(courseCategory.getParentid());
                    //如果父级id的子集为空，那么先初始化
                    if (courseCategoryTreeDto.getChildrenTreeNodes() == null){
                        courseCategoryTreeDto.setChildrenTreeNodes(new ArrayList<>());
                    }
                    //将当前courseCategory添加到其父级courseCategoryTreeDto的子集中
                    CourseCategoryTreeDto categoryTreeDto = new CourseCategoryTreeDto();
                    BeanUtils.copyProperties(courseCategory,categoryTreeDto);
                    courseCategoryTreeDto.getChildrenTreeNodes().add(categoryTreeDto);
                });
        //将map里的一级节点抽出来返回就行
        idList.forEach(id -> {
            if(categoryMap.containsKey(id)){
                courseCategoryTreeDtoList.add(categoryMap.get(id));
            }
        });
        return courseCategoryTreeDtoList;
    }
}
