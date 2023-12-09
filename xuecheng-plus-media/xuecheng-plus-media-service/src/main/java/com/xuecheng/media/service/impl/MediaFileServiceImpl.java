package com.xuecheng.media.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.j256.simplemagic.ContentInfo;
import com.j256.simplemagic.ContentInfoUtil;
import com.xuecheng.base.execption.XueChengPlusException;
import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.base.utils.Mp4VideoUtil;
import com.xuecheng.media.enums.ConvertStatusEnum;
import com.xuecheng.media.enums.FileTypeEnum;
import com.xuecheng.media.mapper.MediaFilesMapper;
import com.xuecheng.media.model.dto.QueryMediaParamsDto;
import com.xuecheng.media.model.dto.UploadFileParamsDto;
import com.xuecheng.media.model.dto.UploadFileResultDto;
import com.xuecheng.media.model.po.MediaFiles;
import com.xuecheng.media.model.po.MediaProcess;
import com.xuecheng.media.service.MediaFileService;
import com.xuecheng.media.service.MediaProcessService;
import com.xuecheng.media.util.MinioUtil;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
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
@Slf4j
@Service
public class MediaFileServiceImpl extends ServiceImpl<MediaFilesMapper, MediaFiles> implements MediaFileService {

    @Value("${minio.bucket.files}")
    private String files;

    @Value("${minio.bucket.videofiles}")
    private String videoFiles;

    @Value("${file.tempPath}")
    private String tempPath;

    @Value("${file.ffmpegPath}")
    private String ffmpegPath;

    @Resource
    private MediaFilesMapper mediaFilesMapper;

    @Resource
    private MinioUtil minioUtil;

    @Resource
    private MediaProcessService mediaProcessService;

    /**
     * 本类的代理对象，如果不注入的话调用方法默认是this.方法名，非代理对象调用事务方法会导致事务失效
     */
    @Resource
    private MediaFileServiceImpl mediaFileServiceProxy;

    /**
     * 待处理文件的格式
     */
    private static final String PENDING_FILE_MIMETYPE = "video/x-msvideo";

    /**
     * 文件转换结果
     */
    private static final String CONVERT_RESULT = "success";

    private static final String MP4_EXTENSION = ".mp4";

    @Override
    public PageResult<MediaFiles> queryMediaFiles(Long companyId, PageParams pageParams, QueryMediaParamsDto queryMediaParamsDto) {
        String fileType = queryMediaParamsDto.getFileType();
        String auditStatus = queryMediaParamsDto.getAuditStatus();
        String filename = queryMediaParamsDto.getFilename();
        //构建查询条件对象
        LambdaQueryWrapper<MediaFiles> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(StringUtils.isNotBlank(filename), MediaFiles::getFilename, filename)
                .eq(StringUtils.isNotBlank(fileType), MediaFiles::getFileType, fileType)
                .eq(StringUtils.isNotBlank(auditStatus), MediaFiles::getAuditStatus, auditStatus);
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
        String extension = getExtension(multipartFile.getOriginalFilename());
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
        MediaFiles mediaFiles = mediaFileServiceProxy.saveMediaFile(companyId, fileMd5, uploadFileParamsDto, objectName, files);
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
            } catch (Exception e) {
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
        mediaFileServiceProxy.saveMediaFile(companyId, fileMd5, uploadFileParamsDto, mergeObjectName, videoFiles);
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

    @Override
    public MediaProcess convertAviTypeVideo(MediaProcess mediaProcess){
        String aviFilePath = tempPath + mediaProcess.getFilename();
        String bucket = mediaProcess.getBucket();
        String mp4FileName = mediaProcess.getFileId().concat(MP4_EXTENSION);
        String mp4FilePath = tempPath + mp4FileName;
        try (InputStream inputStream =minioUtil.getObject(bucket, mediaProcess.getFilePath());
             OutputStream outputStream = Files.newOutputStream(Paths.get(aviFilePath))) {
            // 将minio上的文件流保存到本地
            byte[] buffer = new byte[1024];
            int len;
            while ((len = inputStream.read(buffer)) != -1){
                outputStream.write(buffer, 0, len);
            }
            // 创建工具类对象
            Mp4VideoUtil videoUtil = new Mp4VideoUtil(ffmpegPath, aviFilePath, mp4FileName, mp4FilePath);
            // 开始视频转换，成功将返回success
            String result = videoUtil.generateMp4();
            if (!CONVERT_RESULT.equals(result)){
                log.error("视频转换定时任务===>视频转码失败,fileId：{},原因：{}", bucket, mediaProcess.getFileId());
                throw new RuntimeException("视频转码失败");
            }
            String objectName = mediaProcess.getFilePath().replace(getExtension(aviFilePath),  MP4_EXTENSION);
            try (InputStream fileInputStream = Files.newInputStream(Paths.get(mp4FilePath))) {
                minioUtil.putObject(bucket, objectName, fileInputStream, getMimeType(MP4_EXTENSION));
            }
            mediaProcess.setFilename(mp4FileName);
            mediaProcess.setFilePath(objectName);
            mediaProcess.setUrl(mediaProcess.getUrl().replace(getExtension(aviFilePath),  MP4_EXTENSION));
            mediaProcess.setStatus(ConvertStatusEnum.SUCCESSFULLY_PROCESSED.getConvertFileStatus());
            return mediaProcess;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 保存文件记录
     * 如果是非事务方法调用事务方法，必须是代理对象来调用，否则事务不会生效，并且被调用的事务方法需要是public的
     *
     * @param companyId
     * @param fileMd5
     * @param uploadFileParamsDto
     * @param objectName
     * @author fantasy
     * @date 2023-11-17
     * @since version
     */
    @Transactional(rollbackFor = Exception.class)
    public MediaFiles saveMediaFile(Long companyId, String fileMd5, UploadFileParamsDto uploadFileParamsDto, String objectName, String bucket) {
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
        // 如果上传的文件是avi格式的视频，要将他添加到待处理文件表中，让定时任务去将他转为mp4格式
        String mimeType = getMimeType(getExtension(mediaFiles.getFilename()));
        if (PENDING_FILE_MIMETYPE.equals(mimeType)){
            //将文件信息写入到待处理文件表中
            MediaProcess mediaProcess = new MediaProcess();
            BeanUtils.copyProperties(mediaFiles,mediaProcess);
            mediaProcess.setStatus("1");
            mediaProcess.setFailCount(0);
            mediaProcess.setCreateDate(LocalDateTime.now());
            mediaProcessService.save(mediaProcess);
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
    private static String getMimeType(String extension) {
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

    private String getExtension(String fileName){
        if (StringUtils.isBlank(fileName)){
            XueChengPlusException.cast("文件名为空，无法获取后缀名！");
        }
        return fileName.substring(fileName.lastIndexOf("."));
    }
}
