package com.xuecheng.media.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.j256.simplemagic.ContentInfo;
import com.j256.simplemagic.ContentInfoUtil;
import com.xuecheng.base.execption.XueChengPlusException;
import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.media.enums.FileTypeEnum;
import com.xuecheng.media.mapper.MediaFilesMapper;
import com.xuecheng.media.model.dto.QueryMediaParamsDto;
import com.xuecheng.media.model.dto.UploadFileParamsDto;
import com.xuecheng.media.model.dto.UploadFileResultDto;
import com.xuecheng.media.model.po.MediaFiles;
import com.xuecheng.media.service.MediaFileService;
import com.xuecheng.media.util.MinioUtil;
import lombok.SneakyThrows;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.*;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

/**
 * @author Mr.M
 * @version 1.0
 * @description TODO
 * @date 2022/9/10 8:58
 */
@Service
public class MediaFileServiceImpl extends ServiceImpl<MediaFilesMapper, MediaFiles> implements MediaFileService {

    @Value("${minio.bucket.files}")
    private String files;

    @Value("${minio.bucket.videofiles}")
    private String videoFiles;

    @Value("${file.tempPath}")
    private String tempPath;

    @Resource
    MediaFilesMapper mediaFilesMapper;

    @Autowired
    private MinioUtil minioUtil;

    @Override
    public PageResult<MediaFiles> queryMediaFiles(Long companyId, PageParams pageParams, QueryMediaParamsDto queryMediaParamsDto) {

        //构建查询条件对象
        LambdaQueryWrapper<MediaFiles> queryWrapper = new LambdaQueryWrapper<>();

        //分页对象
        Page<MediaFiles> page = new Page<>(pageParams.getPageNo(), pageParams.getPageSize());
        // 查询数据内容获得结果
        Page<MediaFiles> pageResult = mediaFilesMapper.selectPage(page, queryWrapper);
        // 获取数据列表
        List<MediaFiles> list = pageResult.getRecords();
        // 获取数据总数
        long total = pageResult.getTotal();
        // 构建结果集
        PageResult<MediaFiles> mediaListResult = new PageResult<>(list, total, pageParams.getPageNo(), pageParams.getPageSize());
        return mediaListResult;

    }

    @Override
    @SneakyThrows
    public UploadFileResultDto upload(Long companyId, MultipartFile multipartFile, UploadFileParamsDto uploadFileParamsDto) {
        //将文件上传到minio上
        //获取子目录
        String defaultFolderPath = getDefaultFolderPath();
        //获取md5加密后的文件名
        File file = transferToFile(multipartFile);
        String fileMd5 = getFileMd5(file);
        //获取文件后缀名
        String extension = multipartFile.getOriginalFilename().substring(multipartFile.getOriginalFilename().lastIndexOf("."));
        String objectName = defaultFolderPath + fileMd5 + extension;
        try (InputStream inputStream = new FileInputStream(file)) {
            minioUtil.putObject(files, objectName, inputStream, multipartFile.getContentType());
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        //删除临时文件
        FileUtils.deleteQuietly(file);
        //图片
        uploadFileParamsDto.setFileType(FileTypeEnum.PICTURE.getFileType());
        //将文件信息保存至数据库
        MediaFiles mediaFiles = saveMediaFile(companyId, fileMd5, uploadFileParamsDto, objectName, files);
        UploadFileResultDto uploadFileResultDto = new UploadFileResultDto();
        BeanUtils.copyProperties(mediaFiles, uploadFileResultDto);
        return uploadFileResultDto;
    }

    @Override
    public boolean checkFile(String fileMd5) {
        MediaFiles mediaFiles = getById(fileMd5);
        if (mediaFiles != null) {
            try (InputStream inputStream = minioUtil.getObject(mediaFiles.getBucket(), mediaFiles.getFilePath())) {
                if (inputStream != null) {
                    return true;
                }
            } catch (IOException e) {
                return false;
            }
        }
        return false;
    }

    @Override
    public boolean checkChunk(String fileMd5, int chunk) {
        try (InputStream inputStream = minioUtil.getObject(videoFiles, getChunkFileFolderPath(fileMd5) + chunk)) {
            if (inputStream != null) {
                return true;
            }
        } catch (Exception e) {
            return false;
        }
        return false;
    }

    @Override
    public void uploadChunk(MultipartFile file, String fileMd5, int chunk) {
        String chunkFilerPath = getChunkFileFolderPath(fileMd5) + chunk;
        minioUtil.putObject(videoFiles, chunkFilerPath, file);
    }

    @Override
    public void mergeChunks(Long companyId, String fileMd5, String fileName, int chunkTotal, UploadFileParamsDto uploadFileParamsDto) {
        //合并文件
        String extension = fileName.substring(fileName.lastIndexOf("."));
        // 合并后的文件名
        String mergeObjectName = getFilePathByMd5(fileMd5, extension);
        String chunkFileFolderPath = getChunkFileFolderPath(fileMd5);
        minioUtil.mergeObject(videoFiles, mergeObjectName, chunkFileFolderPath, chunkTotal);
        //验证md5
        //从minio上下载文件
        File minioFile = minioUtil.getObjectForFile(videoFiles, mergeObjectName);
        String minioFileMd5 = getFileMd5(minioFile);
        if (!fileMd5.equals(minioFileMd5)) {
            XueChengPlusException.cast("合并后的文件与源文件不符");
        }
        //设置文件大小
        uploadFileParamsDto.setFileSize(minioFile.length());
        uploadFileParamsDto.setFileType(FileTypeEnum.VIDEO.getFileType());
        //删除临时文件
        FileUtils.deleteQuietly(minioFile);
        // 合并成功后保存该文件记录
        saveMediaFile(companyId, fileMd5, uploadFileParamsDto, mergeObjectName, videoFiles);
        //清除分块和minio上下下来的文件
        minioUtil.removeObjects(videoFiles, chunkFileFolderPath, chunkTotal);
    }

    @Override
    public String previewFileById(String mediaId) {
        MediaFiles mediaFiles = getById(mediaId);
        if (mediaFiles == null || StringUtils.isBlank(mediaFiles.getUrl())) {
            throw new RuntimeException("未找到该条文件记录");
        }
        try (InputStream inputStream = minioUtil.getObject("video", mediaFiles.getFilePath())) {
            return mediaFiles.getUrl();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void removeFileById(String mediaId) {
        MediaFiles mediaFiles = getById(mediaId);
        if (mediaFiles == null) {
            throw new RuntimeException("未找到该条文件记录");
        }
        removeById(mediaId);
    }

    /**
     * 保存文件记录
     *
     * @param companyId
     * @param fileMd5
     * @param uploadFileParamsDto
     * @param objectName
     * @author fantasy
     * @date 2023-11-17
     * @since version
     */
    private MediaFiles saveMediaFile(Long companyId, String fileMd5, UploadFileParamsDto uploadFileParamsDto, String objectName, String bucket) {
        MediaFiles mediaFiles = mediaFilesMapper.selectById(fileMd5);
        if (mediaFiles == null) {
            mediaFiles = new MediaFiles();
            BeanUtils.copyProperties(uploadFileParamsDto, mediaFiles);
            mediaFiles.setId(fileMd5);
            mediaFiles.setFileId(fileMd5);
            mediaFiles.setCompanyId(companyId);
            mediaFiles.setUrl("/" + bucket + "/" + objectName);
            mediaFiles.setBucket(bucket);
            mediaFiles.setFilePath(objectName);
            mediaFiles.setCreateDate(LocalDateTime.now());
            mediaFiles.setAuditStatus("002003");
            mediaFiles.setStatus("1");
            int result = mediaFilesMapper.insert(mediaFiles);
            if (result <= 0) {
                String message = String.join("保存文件信息失败，文件id：", fileMd5, "objectName：", objectName);
                XueChengPlusException.cast(message);
            }
        }
        return mediaFiles;
    }

    /**
     * 获取文件的mimeType
     *
     * @param extension
     * @return {@code String }
     * @author fantasy
     * @date 2023-11-15
     * @since version
     */
    private String getMimeType(String extension) {
        if (extension == null) {
            extension = "";
        }
        //根据扩展名取出mimeType
        ContentInfo extensionMatch = ContentInfoUtil.findExtensionMatch(extension);
        //通用mimeType，字节流
        String mimeType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
        if (extensionMatch != null) {
            mimeType = extensionMatch.getMimeType();
        }
        return mimeType;
    }


    /**
     * 获取文件默认存储目录路径 年/月/日
     *
     * @return {@code String }
     * @author fantasy
     * @date 2023-11-15
     * @since version
     */
    private String getDefaultFolderPath() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String folder = sdf.format(new Date()).replace("-", "/") + "/";
        return folder;
    }


    /**
     * 获取文件的md5
     *
     * @param file
     * @return {@code String }
     * @author fantasy
     * @date 2023-11-15
     * @since version
     */
    private String getFileMd5(File file) {
        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            String fileMd5 = DigestUtils.md5Hex(fileInputStream);
            return fileMd5;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public File transferToFile(MultipartFile multipartFile) throws IOException {
        String filePath = tempPath + multipartFile.getOriginalFilename();
        File targetFile = new File(filePath);
        if (!targetFile.exists()) {
            targetFile.mkdirs();
        }
        multipartFile.transferTo(targetFile);

        return targetFile;
    }

    /**
     * 得到分块文件的目录
     *
     * @param fileMd5 文件加密
     * @return {@code String }
     * @author fantasy
     * @date 2023-11-17
     * @since version
     */
    private String getChunkFileFolderPath(String fileMd5) {
        return fileMd5.charAt(0) + "/" + fileMd5.charAt(1) + "/" + fileMd5 + "/" + "chunk" + "/";
    }

    /**
     * 得到合并后的文件的地址
     *
     * @param fileMd5 文件id即md5值
     * @param fileExt 文件扩展名
     * @return
     */
    private String getFilePathByMd5(String fileMd5, String fileExt) {
        return fileMd5.charAt(0) + "/" + fileMd5.charAt(1) + "/" + fileMd5 + "/" + fileMd5 + fileExt;
    }

}
