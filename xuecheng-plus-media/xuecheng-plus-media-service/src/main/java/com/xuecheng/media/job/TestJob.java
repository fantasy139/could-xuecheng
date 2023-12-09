package com.xuecheng.media.job;

import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author fantasy
 * @description 将avi类型的视频转为mp4的定时任务
 * @date 2023/11/21 21:01
 */
@Component
@Slf4j
public class TestJob {

    @XxlJob("testJob")
    public void testJob(){
        log.info("============将avi类型的视频转为mp4的定时任务开始执行============");
        System.out.println("1111111111111111111111111111");
        log.info("============将avi类型的视频转为mp4的定时任务执行结束============");
    }
}
