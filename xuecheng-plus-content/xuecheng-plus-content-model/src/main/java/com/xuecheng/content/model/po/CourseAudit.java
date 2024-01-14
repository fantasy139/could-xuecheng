package com.xuecheng.content.model.po;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @description 
 * @date 2024-01-08
 * @author fantasy
 */
@Data
@ApiModel(value="CourseAudit", description="")
public class CourseAudit implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    @ApiModelProperty(value = "课程id")
    private Long courseId;

    @ApiModelProperty(value = "审核意见")
    private String auditMind;

    @ApiModelProperty(value = "审核状态")
    private String auditStatus;

    @ApiModelProperty(value = "审核人")
    private String auditPeople;

    @ApiModelProperty(value = "审核时间")
    private LocalDateTime auditDate;


}
