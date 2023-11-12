package com.xuecheng.content.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xuecheng.content.model.dto.SaveTeachplanDto;
import com.xuecheng.content.model.dto.TeachplanDto;
import com.xuecheng.content.model.po.Teachplan;

import java.util.List;

/**
 * <p>
 * 课程计划 服务类
 * </p>
 *
 * @author itcast
 * @since 2023-11-04
 */
public interface TeachplanService extends IService<Teachplan> {

    /**
     * 查询课程计划树形结构
     * @param courseId
     * @return {@code List<TeachplanDto> }
     * @author fantasy
     * @date 2023-11-07
     * @since version
     */
    List<TeachplanDto> getTreeNodes(Long courseId);

    /**
     * 课程计划创建或者修改
     *
     * @param teachplanDto
     * @author fantasy
     * @date 2023-11-09
     * @since version
     */
    void saveTeachplan(SaveTeachplanDto teachplanDto);

    /**
     * 课程计划上移
     * @param id
     * @author fantasy
     * @date 2023-11-09
     * @since version
     */
    void moveUpById(Long id);

    /**
     * 通过id校验对象是否存在
     * @param id
     * @author fantasy
     * @date 2023-11-09
     * @since version
     */
    void isExist(Long id);

    /**
     * 课程计划下移
     * @param id
     * @author fantasy
     * @date 2023-11-09
     * @since version
     */
    void moveDownById(Long id);
}
