package com.xuecheng.media.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.media.model.dto.QueryMediaParamsDto;
import com.xuecheng.media.model.dto.UploadFileParamsDto;
import com.xuecheng.media.model.dto.UploadFileResultDto;
import com.xuecheng.media.model.po.MediaFiles;
import org.springframework.web.multipart.MultipartFile;

/**
 * @description 媒资文件管理业务类
 * @author Mr.M
 * @date 2022/9/10 8:55
 * @version 1.0
 */
public interface MediaFileService extends IService<MediaFiles> {

    /**
     * @param companyId
     * @param pageParams          分页参数
     * @param queryMediaParamsDto 查询条件
     * @return {@code PageResult<MediaFiles> }
     * @author fantasy
     * @date 2023-11-15
     * @since version
     */
    public PageResult<MediaFiles> queryMediaFiles(Long companyId,PageParams pageParams, QueryMediaParamsDto queryMediaParamsDto);


    /**
     * 普通文件上传
     *
     * @param companyId
     * @param file
     * @param uploadFileParamsDto
     * @return {@code UploadFileResultDto }
     * @author fantasy
     * @date 2023-11-15
     * @since version
     */
    UploadFileResultDto upload(Long companyId, MultipartFile file, UploadFileParamsDto uploadFileParamsDto);

    /**
     * 判断文件是否存在
     * @param fileMd5
     * @return boolean
     * @author fantasy
     * @date 2023-11-17
     * @since version
     */
    boolean checkFile(String fileMd5);

    /**
     * 判断分块文件是否存在
     * @param fileMd5
     * @param chunk
     * @return boolean
     * @author fantasy
     * @date 2023-11-17
     * @since version
     */
    boolean checkChunk(String fileMd5, int chunk);

    /**
     * 上传分块文件
     *
     * @param file
     * @param fileMd5
     * @param chunk
     * @author fantasy
     * @date 2023-11-17
     * @since version
     */
    void uploadChunk(MultipartFile file, String fileMd5, int chunk);

    /**
     * 合并文件
     *
     * @param companyId
     * @param fileMd5
     * @param fileName
     * @param chunkTotal
     * @param uploadFileParamsDto
     * @author fantasy
     * @date 2023-11-17
     * @since version
     */
    void mergeChunks(Long companyId, String fileMd5, String fileName, int chunkTotal, UploadFileParamsDto uploadFileParamsDto);

    /**
     * 预览视频
     *
     * @param mediaId
     * @return {@code String }
     * @author fantasy
     * @date 2023-11-19
     * @since version
     */
    String previewFileById(String mediaId);

    /**
     * 根据id删除文件
     * @param mediaId
     * @author fantasy
     * @date 2023-11-19
     * @since version
     */
    void removeFileById(String mediaId);
}
