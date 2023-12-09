package com.xuecheng.content.api;

import com.xuecheng.base.model.RestResponse;
import com.xuecheng.content.model.dto.BindTeachplanMediaDto;
import com.xuecheng.content.model.dto.SaveTeachplanDto;
import com.xuecheng.content.model.dto.TeachplanDto;
import com.xuecheng.content.service.TeachplanService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author fantasy
 * @description 课程计划
 * @date 2023/11/7 20:28
 */
@Slf4j
@Api(value = "课程计划编辑接口",tags = "课程计划编辑接口")
@RestController
@RequestMapping("/teachplan")
public class TeachplanController {

    @Resource
    private TeachplanService teachplanService;

    @ApiOperation("查询课程计划树形结构")
    @ApiImplicitParam(value = "courseId",name = "课程Id",required = true,dataType = "Long",paramType = "path")
    @GetMapping("/{courseId}/tree-nodes")
    public List<TeachplanDto> getTreeNodes(@PathVariable Long courseId){
        return teachplanService.getTreeNodes(courseId);
    }

    @ApiOperation("课程计划创建或修改")
    @PostMapping
    public RestResponse saveTeachplan(@RequestBody SaveTeachplanDto teachplan){
        teachplanService.saveTeachplan(teachplan);
        return RestResponse.success();
    }

    @ApiOperation("课程计划删除")
    @DeleteMapping("/{id}")
    public RestResponse saveTeachplan(@PathVariable Long id){
        teachplanService.removeById(id);
        return RestResponse.success();
    }

    @ApiOperation("课程计划上移")
    @PostMapping("/moveup/{id}")
    public RestResponse moveUpById(@PathVariable Long id){
        teachplanService.isExist(id);
        teachplanService.moveUpById(id);
        return RestResponse.success();
    }

    @ApiOperation("课程计划下移")
    @PostMapping("/movedown/{id}")
    public RestResponse moveDownById(@PathVariable Long id){
        teachplanService.isExist(id);
        teachplanService.moveDownById(id);
        return RestResponse.success();
    }

    @ApiOperation(value = "课程计划和媒资信息绑定")
    @PostMapping("/association/media")
    public RestResponse associationMedia(@RequestBody BindTeachplanMediaDto bindTeachplanMediaDto){
        teachplanService.associationMedia(bindTeachplanMediaDto);
        return RestResponse.success();
    }

    @ApiOperation(value = "课程计划和媒资信息解绑")
    @DeleteMapping("/association/media/{teachplanId}/{mediaId}")
    public RestResponse disassociationMedia(@PathVariable Long teachplanId, @PathVariable String mediaId){
        teachplanService.disassociationMedia(teachplanId, mediaId);
        return RestResponse.success();
    }
}
