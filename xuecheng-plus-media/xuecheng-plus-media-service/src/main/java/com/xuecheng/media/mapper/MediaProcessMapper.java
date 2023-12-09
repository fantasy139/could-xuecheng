package com.xuecheng.media.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xuecheng.media.model.po.MediaProcess;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author itcast
 */
public interface MediaProcessMapper extends BaseMapper<MediaProcess> {
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
