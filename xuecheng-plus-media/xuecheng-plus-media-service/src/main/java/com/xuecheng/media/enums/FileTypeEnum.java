package com.xuecheng.media.enums;

/**
 * @author fantasy
 * @description 文件类型枚举
 * @date 2023/11/17 23:03
 */
public enum FileTypeEnum {

    /**
     * 图片
     */
    PICTURE("001001"),
    /**
     * 视频
     */
    VIDEO("001002");

    final String fileType;

    FileTypeEnum(String fileType) {
        this.fileType = fileType;
    }

    public String getFileType() {
        return fileType;
    }
}
