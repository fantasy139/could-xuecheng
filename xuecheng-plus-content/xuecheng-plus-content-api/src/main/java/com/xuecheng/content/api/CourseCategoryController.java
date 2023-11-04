package com.xuecheng.content.api;

import com.xuecheng.content.model.dto.CourseCategoryTreeDto;
import com.xuecheng.content.service.CourseCategoryService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;


/**
 * @author fantasy
 * @description 课程分类
 * @date 2023-11-04
 */
@Api(tags = "课程分类管理")
@Slf4j
@RestController
@RequestMapping("/course-category")
public class CourseCategoryController {

    @Resource
    private CourseCategoryService  courseCategoryService;

    /**
     * @return {@code List<CourseCategoryTreeDto> }
     * @author fantasy
     * @date 2023-11-04
     * @since version
     */
    @ApiOperation("查询树形课程分类")
    @GetMapping("/tree-nodes")
    public List<CourseCategoryTreeDto> queryTreeNodes() {
        return courseCategoryService.queryTreeNodes();
    }
}
