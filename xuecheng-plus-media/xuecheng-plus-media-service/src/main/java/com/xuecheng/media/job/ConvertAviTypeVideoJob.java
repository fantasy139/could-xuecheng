package com.xuecheng.media.job;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.xuecheng.media.enums.ConvertStatusEnum;
import com.xuecheng.media.model.po.MediaFiles;
import com.xuecheng.media.model.po.MediaProcess;
import com.xuecheng.media.model.po.MediaProcessHistory;
import com.xuecheng.media.service.MediaFileService;
import com.xuecheng.media.service.MediaProcessHistoryService;
import com.xuecheng.media.service.MediaProcessService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author fantasy
 * @description 将avi类型的视频转为mp4的定时任务
 * @date 2023/11/21 21:01
 */
@Component
@Slf4j
public class ConvertAviTypeVideoJob {

    @Resource
    private MediaProcessService mediaProcessService;

    @Resource
    private MediaProcessHistoryService mediaProcessHistoryService;

    @Resource
    private MediaFileService mediaFileService;

    @XxlJob("convertAviTypeVideoJob")
    public void convertAviTypeVideoJob(){
        log.info("============视频转换定时任务开始执行============");
        int shardIndex = XxlJobHelper.getShardIndex();
        int shardTotal = XxlJobHelper.getShardTotal();
        //分布式时需要拿到属于自己的待执行的任务
        List<MediaProcess> mediaProcessList = mediaProcessService.getMediaProcessList(shardIndex,shardTotal);

        //根据fileId分组
        List<String> fileIdList = mediaProcessList.stream().map(MediaProcess::getFileId).collect(Collectors.toList());
        List<MediaFiles> mediaFilesList = mediaFileService.listByIds(fileIdList);
        Map<String, MediaFiles> mediaFilesMap = mediaFilesList.stream().
                collect(Collectors.toMap(MediaFiles::getFileId, Function.identity(), (key1, key2) -> key2));
        // 多线程的方式执行转换任务
        ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        for (MediaProcess mediaProcess : mediaProcessList) {
            executorService.execute(() ->{
                // 用乐观锁来保证分布式部署时定时任务不会执行到同一条记录
                Integer version = mediaProcess.getVersion();
                LambdaUpdateWrapper<MediaProcess> updateWrapper = new LambdaUpdateWrapper<>();
                updateWrapper.eq(MediaProcess::getVersion,version)
                        .eq(MediaProcess::getId,mediaProcess.getId())
                        .set(MediaProcess::getVersion,++version)
                        .set(MediaProcess::getStatus,ConvertStatusEnum.PROCESSING.getConvertFileStatus());
                boolean updateResult = mediaProcessService.update(updateWrapper);
                if (updateResult) {
                    try {
                        // 转换文件，并上传至minio
                        MediaProcess newMediaProcess = mediaFileService.convertAviTypeVideo(mediaProcess);

                        // 转换完成
                        // 将新的数据更新至MediaFiles表对应的记录中
                        MediaFiles mediaFiles = mediaFilesMap.get(newMediaProcess.getFileId());
                        BeanUtils.copyProperties(newMediaProcess, mediaFiles);
                        mediaFiles.setChangeDate(LocalDateTime.now());
                        mediaFileService.updateById(mediaFiles);

                        // 转换完成，在转换历史表中插入记录
                        MediaProcessHistory mediaProcessHistory = new MediaProcessHistory();
                        BeanUtils.copyProperties(newMediaProcess, mediaProcessHistory);
                        mediaProcessHistory.setFinishDate(LocalDateTime.now());
                        mediaProcessHistoryService.save(mediaProcessHistory);

                        // 删除待转换文件表里的数据
                        mediaProcessService.removeById(newMediaProcess.getId());
                    } catch (Exception e) {
                        log.error("视频转换定时任务===>转换失败,fileId：{},原因：{}", mediaProcess.getFileId(), e);
                        // 如果转换失败
                        int failCount = mediaProcess.getFailCount();
                        mediaProcess.setErrormsg(e.getMessage());
                        mediaProcess.setFailCount(++failCount);
                        if (failCount < 5) {
                            mediaProcessService.updateById(mediaProcess);
                        }
                        // 转换失败，在转换历史表中插入记录
                        MediaProcessHistory mediaProcessHistory = new MediaProcessHistory();
                        mediaProcess.setStatus(ConvertStatusEnum.PROCESSING_FAILED.getConvertFileStatus());
                        BeanUtils.copyProperties(mediaProcess, mediaProcessHistory);
                        mediaProcessHistory.setFinishDate(LocalDateTime.now());
                        mediaProcessHistoryService.save(mediaProcessHistory);
                    }
                }
            });
        }
        log.info("============将avi类型的视频转为mp4的定时任务执行结束============");
    }
}
