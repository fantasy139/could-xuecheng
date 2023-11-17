package com.xuecheng.media.service;

import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.media.model.dto.QueryMediaParamsDto;
import com.xuecheng.media.model.dto.UploadFileParamsDto;
import com.xuecheng.media.model.dto.UploadFileResultDto;
import com.xuecheng.media.model.po.MediaFiles;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * @description 媒资文件管理业务类
 * @author Mr.M
 * @date 2022/9/10 8:55
 * @version 1.0
 */
public interface MediaFileService {

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
}
