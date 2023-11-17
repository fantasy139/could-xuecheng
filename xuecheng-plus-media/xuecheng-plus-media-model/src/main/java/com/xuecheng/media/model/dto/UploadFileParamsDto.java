package com.xuecheng.media.model.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiOperation;
import lombok.Data;

/**
 * @author fantasy
 * @description 上传普通文件请求参数
 * @date 2023/11/15 21:35
 */
@Data
@ApiModel(value = "UploadFileParamsDto", description = "普通文件上传请求参数")
public class UploadFileParamsDto {

    /**
     * 文件名称
     */
    @ApiModelProperty(value = "文件名称")
    private String filename;
    /**
     * 文件类型（文档，音频，视频）
     */
    @ApiModelProperty(value = "文件类型（文档，音频，视频）")
    private String fileType;
    /**
     * 文件大小
     */
    @ApiModelProperty(value = "文件大小")
    private Long fileSize;
    /**
     * 标签
     */
    @ApiModelProperty(value = "标签")
    private String tags;
    /**
     * 上传人
     */
    @ApiModelProperty(value = "上传人")
    private String username;
    /**
     * 备注
     */
    @ApiModelProperty(value = "备注")
    private String remark;
}