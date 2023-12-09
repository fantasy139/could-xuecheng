package com.xuecheng.media.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xuecheng.media.model.po.MediaProcess;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author fantasy
 * @description 待处理文件 service
 * @date 2023/11/22 21:22
 */
public interface MediaProcessService extends IService<MediaProcess> {
    /**
     * 查询当前定时任务节点的待处理任务
     * @param shardIndex
     * @param shardTotal
     * @return {@code List<MediaProcess> }
     * @author fantasy
     * @date 2023-11-22
     * @since version
     */
    List<MediaProcess> getMediaProcessList(@Param("shardIndex") int shardIndex, @Param("shardTotal") int shardTotal);
}
