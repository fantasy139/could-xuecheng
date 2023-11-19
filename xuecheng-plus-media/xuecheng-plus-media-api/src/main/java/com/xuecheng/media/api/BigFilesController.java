package com.xuecheng.media.api;

import com.xuecheng.base.utils.RestResponse;
import com.xuecheng.media.model.dto.UploadFileParamsDto;
import com.xuecheng.media.model.po.MediaFiles;
import com.xuecheng.media.service.MediaFileService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author fantasy
 * @description 大文件上传
 * @date 2023/11/17 21:34
 */
@Api(value = "大文件上传接口", tags = "大文件上传接口")
@RestController
@RequestMapping("/upload")
public class BigFilesController {

    @Autowired
    private MediaFileService mediaFileService;

    @ApiOperation(value = "文件上传前检查文件")
    @PostMapping("/checkfile")
    public RestResponse<Boolean> checkFile(@RequestParam("fileMd5") String fileMd5) {
        if (!mediaFileService.checkFile(fileMd5)) {
            return RestResponse.success(true);
        }
        return RestResponse.success(false, "已存在该文件");
    }


    @ApiOperation(value = "分块文件上传前的检测")
    @PostMapping("/checkchunk")
    public RestResponse<Boolean> checkChunk(@RequestParam("fileMd5") String fileMd5,
                                            @RequestParam("chunk") int chunk) {
        if (mediaFileService.checkChunk(fileMd5, chunk)) {
            return RestResponse.success(true);
        }
        return RestResponse.success(false);
    }

    @ApiOperation(value = "上传分块文件")
    @PostMapping("/uploadchunk")
    public RestResponse uploadChunk(@RequestParam("file") MultipartFile file,
                                    @RequestParam("fileMd5") String fileMd5,
                                    @RequestParam("chunk") int chunk) {
        mediaFileService.uploadChunk(file, fileMd5, chunk);
        return RestResponse.success();
    }

    @ApiOperation(value = "合并文件")
    @PostMapping("/mergechunks")
    public RestResponse mergeChunks(@RequestParam("fileMd5") String fileMd5,
                                    @RequestParam("fileName") String fileName,
                                    @RequestParam("chunkTotal") int chunkTotal) {
        Long companyId = 1232141425L;
        UploadFileParamsDto uploadFileParamsDto = new UploadFileParamsDto();
        uploadFileParamsDto.setTags("课程视频");
        uploadFileParamsDto.setRemark("");
        uploadFileParamsDto.setFilename(fileName);

        mediaFileService.mergeChunks(companyId, fileMd5, fileName, chunkTotal, uploadFileParamsDto);
        return RestResponse.success();
    }
}
