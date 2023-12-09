package com.xuecheng.media.enums;

/**
 * @author fantasy
 * @description 转换状态枚举类
 * @date 2023/11/21 21:57
 */
public enum ConvertStatusEnum {
    /**
     * 待处理
     */
    UNTREATED("1"),
    /**
     * 处理成功
     */
    SUCCESSFULLY_PROCESSED("2"),
    /**
     * 处理失败
     */
    PROCESSING_FAILED("3"),
    /**
     * 处理中
     */
    PROCESSING("4");

    final String convertFileStatus;

    ConvertStatusEnum(String convertFileStatus) {
        this.convertFileStatus = convertFileStatus;
    }

    public String getConvertFileStatus() {
        return convertFileStatus;
    }
}
