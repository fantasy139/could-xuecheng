package com.xuecheng.media.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xuecheng.media.mapper.MediaProcessMapper;
import com.xuecheng.media.model.po.MediaProcess;
import com.xuecheng.media.service.MediaProcessService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author fantasy
 * @description 待处理文件表
 * @date 2023/11/22 21:23
 */
@Service
public class MediaProcessServiceImpl extends ServiceImpl<MediaProcessMapper, MediaProcess> implements MediaProcessService{
    @Override
    public List<MediaProcess> getMediaProcessList(int shardIndex, int shardTotal) {
        return baseMapper.getMediaProcessList(shardIndex,shardTotal);
    }
}
