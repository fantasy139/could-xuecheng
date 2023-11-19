package com.xuecheng.media.util;

import com.beust.jcommander.internal.Lists;
import io.minio.*;
import io.minio.http.Method;
import io.minio.messages.Bucket;
import io.minio.messages.DeleteError;
import io.minio.messages.DeleteObject;
import io.minio.messages.Item;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.utils.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author fantasy
 * @description minio工具类
 * @date 2023/11/15 21:42
 */
@Slf4j
@Component
public class MinioUtil {
    private final int DEFAULT_EXPIRY_TIME = 7 * 24 * 3600;

    @Autowired
    private MinioClient minio;

    /**
     * 检查存储桶是否存在
     *
     * @param bucketName 存储桶名称
     * @return
     */
    @SneakyThrows
    public boolean bucketExists(String bucketName) {
        return minio.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
    }

    /**
     * 创建存储桶
     *
     * @param bucketName 存储桶名称
     */
    @SneakyThrows
    public boolean makeBucket(String bucketName) {
        boolean flag = bucketExists(bucketName);
        if (flag) {
            return true;
        }

        minio.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
        return true;
    }

    /**
     * 列出所有存储桶名称
     *
     * @return
     */
    @SneakyThrows
    public List<String> listBucketNames() {
        List<Bucket> list = listBuckets();
        return list.stream().filter(Objects::nonNull).map(o -> o.name()).collect(Collectors.toList());
    }

    /**
     * 列出所有存储桶
     *
     * @return
     */
    @SneakyThrows
    public List<Bucket> listBuckets() {
        return minio.listBuckets();
    }

    /**
     * 删除存储桶
     *
     * @param bucketName 存储桶名称
     * @return
     */
    @SneakyThrows
    public boolean removeBucket(String bucketName) {
        Iterable<Result<Item>> myObjects = listObjects(bucketName);
        for (Result<Item> result : myObjects) {
            Item item = result.get();
            // 有对象文件，则删除失败
            if (item.size() > 0) {
                return false;
            }
        }
        // 删除存储桶，注意，只有存储桶为空时才能删除成功。
        minio.removeBucket(RemoveBucketArgs.builder().bucket(bucketName).build());
        return !bucketExists(bucketName);
    }

    /**
     * 列出存储桶中的所有对象
     *
     * @param bucketName 存储桶名称
     * @return
     */
    @SneakyThrows
    public Iterable<Result<Item>> listObjects(String bucketName) {
        return minio.listObjects(ListObjectsArgs.builder().bucket(bucketName).build());
    }

    /**
     * 列出存储桶中的所有对象名称
     *
     * @param bucketName 存储桶名称
     * @return
     */
    @SneakyThrows
    public List<String> listObjectNames(String bucketName) {
        List<String> ret = Lists.newArrayList();
        Iterable<Result<Item>> myObjects = listObjects(bucketName);
        for (Result<Item> result : myObjects) {
            Item item = result.get();
            ret.add(item.objectName());
        }

        return ret;
    }

    /**
     * 文件上传
     *
     * @param bucketName
     * @param multipartFile
     */
    @SneakyThrows
    public ObjectWriteResponse putObject(String bucketName, MultipartFile multipartFile) {
        PutObjectArgs args = PutObjectArgs.builder()
                .bucket(bucketName)
                .object(multipartFile.getName())
                .contentType(multipartFile.getContentType())
                .stream(multipartFile.getInputStream(), multipartFile.getSize(), -1)
                .build();

        return minio.putObject(args);
    }


    /**
     * 文件上传
     * @param bucketName
     * @param objectName
     * @param multipartFile
     * @return {@code ObjectWriteResponse }
     * @author fantasy
     * @date 2023-11-17
     * @since version
     */
    @SneakyThrows
    public ObjectWriteResponse putObject(String bucketName, String objectName, MultipartFile multipartFile) {
        PutObjectArgs args = PutObjectArgs.builder()
                .bucket(bucketName)
                .object(objectName)
                .contentType(multipartFile.getContentType())
                .stream(multipartFile.getInputStream(), multipartFile.getSize(), -1)
                .build();

        return minio.putObject(args);
    }

    /**
     * 通过InputStream上传对象
     *
     * @param bucketName  存储桶名称
     * @param objectName  存储桶里的对象名称
     * @param in          要上传的流
     * @param contentType 要上传的文件类型 MimeTypeUtils.IMAGE_JPEG_VALUE
     * @return
     */
    @SneakyThrows
    public ObjectWriteResponse putObject(String bucketName, String objectName, InputStream in, String contentType) {
        PutObjectArgs args = PutObjectArgs.builder()
                .bucket(bucketName)
                .object(objectName)
                .contentType(contentType)
                .stream(in, in.available(), -1)
                .build();

        return minio.putObject(args);
    }

    /**
     * 以流的形式获取一个文件对象
     *
     * @param bucketName 存储桶名称
     * @param objectName 存储桶里的对象名称
     * @return
     */
    @SneakyThrows
    public InputStream getObject(String bucketName, String objectName) {
        boolean flag = bucketExists(bucketName);
        if (!flag) {
            return null;
        }

        StatObjectResponse resp = statObject(bucketName, objectName);
        return resp == null
                ? null
                : minio.getObject(GetObjectArgs.builder().bucket(bucketName).object(objectName).build());
    }

    /**
     * 以file对象的方式拉取minio上的文件
     * @param bucket
     * @param objectName
     * @return {@code File }
     * @author fantasy
     * @date 2023-11-18
     * @since version
     */
    public File getObjectForFile(String bucket,String objectName){
        //临时文件
        File minioFile = null;
        FileOutputStream outputStream = null;
        try{
            InputStream stream = minio.getObject(GetObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectName)
                    .build());
            //创建临时文件
            minioFile= File.createTempFile("minio", ".merge");
            outputStream = new FileOutputStream(minioFile);
            IOUtils.copy(stream,outputStream);
            return minioFile;
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            if(outputStream!=null){
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }


    /**
     * 获取对象的元数据
     *
     * @param bucketName 存储桶名称
     * @param objectName 存储桶里的对象名称
     * @return
     */
    @SneakyThrows
    public StatObjectResponse statObject(String bucketName, String objectName) {
        return !bucketExists(bucketName)
                ? null
                : minio.statObject(StatObjectArgs.builder().bucket(bucketName).object(objectName).build());
    }

    /**
     * 删除文件
     *
     * @param bucketName
     * @param fileName
     * @author fantasy
     * @date 2023-11-17
     * @since version
     */
    @SneakyThrows
    public void removeObject(String bucketName, String fileName) {
        try {
            minio.removeObject(RemoveObjectArgs.builder().bucket(bucketName).object(fileName).build());
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void removeObjects(String bucketName, String chunkFileFolderPath, int chunkTotal){
        List<DeleteObject> deleteObjects = Stream.iterate(0, i -> ++i)
                .limit(chunkTotal)
                .map(i -> new DeleteObject(chunkFileFolderPath.concat(Integer.toString(i))))
                .collect(Collectors.toList());

        RemoveObjectsArgs args = RemoveObjectsArgs.builder().bucket(bucketName).objects(deleteObjects).build();
        Iterable<Result<DeleteError>> results = minio.removeObjects(args);
        // 要想真正的删除，得执行下面的代码
        results.forEach(r->{
            DeleteError deleteError = null;
            try {
                deleteError = r.get();
            } catch (Exception e) {
                e.printStackTrace();
                if (deleteError != null) {
                    log.error("清除分块文件失败,objectName:{}", deleteError.objectName(), e);
                }
            }
        });

    }

    /**
     * 获取文件外链
     *
     * @param bucketName bucket名称
     * @param objectName 文件名称
     * @return url
     */
    @SneakyThrows
    public String getObjectURL(String bucketName, String objectName) {
        GetPresignedObjectUrlArgs build = GetPresignedObjectUrlArgs.builder()
                .method(Method.GET)
                .bucket(bucketName)
                .object(objectName)
//                .expiry(60 * 60 * 24)
                .build();
        return minio.getPresignedObjectUrl(build);
    }

    @SneakyThrows
    public void mergeObject(String bucketName, String mergeObjectName, String chunkFileFolderPath, int chunkTotal) {
        List<ComposeSource> sources = new ArrayList<>();
        for (int i = 0; i < chunkTotal; i++) {
            sources.add(ComposeSource.builder().bucket(bucketName).object(chunkFileFolderPath + i).build());
        }
        ComposeObjectArgs composeObjectArgs = ComposeObjectArgs.builder()
                .sources(sources)
                .bucket(bucketName)
                .object(mergeObjectName)
                .build();
        minio.composeObject(composeObjectArgs);

    }
}
