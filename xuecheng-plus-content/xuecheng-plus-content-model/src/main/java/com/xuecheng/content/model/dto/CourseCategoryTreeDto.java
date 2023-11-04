package com.xuecheng.content.model.dto;

import com.xuecheng.content.model.po.CourseCategory;
import io.swagger.annotations.ApiModel;
import javafx.scene.control.Tab;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author fantasy
 * @description 课程分类树型结点dto
 * @date 2023/11/4 18:57
 */
@ApiModel("课程分类树型结点")
@Data
public class CourseCategoryTreeDto extends CourseCategory implements Serializable {

    List<CourseCategoryTreeDto> childrenTreeNodes;
}

