package com.xuecheng.content.model.po;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @author fantasy
 * @description TODO
 * @date 2023-11-04
 */
@Data
@TableName("course_category")
public class CourseCategory implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @ApiModelProperty("主键")
    private String id;

    /**
     * 分类名称
     */
    @ApiModelProperty("分类名称")
    private String name;

    /**
     * 分类标签默认和名称一样
     */
    @ApiModelProperty("分类标签,默认和名称一样")
    private String label;

    /**
     * 父结点id（第一级的父节点是0，自关联字段id）
     */
    @ApiModelProperty("父结点id（第一级的父节点是0，自关联字段id）")
    private String parentid;

    /**
     * 是否显示
     */
    @ApiModelProperty("是否显示")
    private Integer isShow;

    /**
     * 排序字段
     */
    @ApiModelProperty("排序字段")
    private Integer orderby;

    /**
     * 是否叶子
     */
    @ApiModelProperty("是否叶子")
    private Integer isLeaf;


}
