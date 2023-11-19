package com.xuecheng.base.utils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Mp4VideoUtil extends VideoUtil {

    /**
     * ffmpeg的安装位置
     */
    String ffmpegPath;
    /**
     * 待转换的文件位置
     */
    String videoPath;
    /**
     * mp4文件的名称
     */
    String mp4Name;
    /**
     * mp4所在的位置
     */
    String mp4folderPath;
    public Mp4VideoUtil(String ffmpegPath, String videoPath, String mp4Name, String mp4folderPath){
        super(ffmpegPath);
        this.ffmpegPath = ffmpegPath;
        this.videoPath = videoPath;
        this.mp4Name = mp4Name;
        this.mp4folderPath = mp4folderPath;
    }

    /**
     * 清除已生成的mp4
     * @param mp4Path
     * @author fantasy
     * @date 2023-11-19
     * @since version
     */
    private void clearMp4(String mp4Path){
        //删除原来已经生成的m3u8及ts文件
        File mp4File = new File(mp4Path);
        if(mp4File.exists() && mp4File.isFile()){
            mp4File.delete();
        }
    }
    /**
     * 视频编码，生成mp4文件
     * @return 成功返回success，失败返回控制台日志
     */
    public String generateMp4(){
        //清除已生成的mp4
        clearMp4(mp4folderPath);
        /*
        ffmpeg.exe -i  lucene.avi -c:v libx264 -s 1280x720 -pix_fmt yuv420p -b:a 63k -b:v 753k -r 18 .\lucene.mp4
         */
        List<String> commend = new ArrayList<String>();
        commend.add(ffmpegPath);
        commend.add("-i");
        commend.add(videoPath);
        commend.add("-c:v");
        commend.add("libx264");
        //覆盖输出文件
        commend.add("-y");
        commend.add("-s");
        commend.add("1280x720");
        commend.add("-pix_fmt");
        commend.add("yuv420p");
        commend.add("-b:a");
        commend.add("63k");
        commend.add("-b:v");
        commend.add("753k");
        commend.add("-r");
        commend.add("18");
        commend.add(mp4folderPath  );
        String outstring = null;
        try {
            ProcessBuilder builder = new ProcessBuilder();
            builder.command(commend);
            //将标准输入流和错误输入流合并，通过标准输入流程读取信息
            builder.redirectErrorStream(true);
            Process p = builder.start();
            outstring = waitFor(p);

        } catch (Exception ex) {

            ex.printStackTrace();

        }
        Boolean checkVideoTime = this.checkVideoTime(videoPath, mp4folderPath);
        if(!checkVideoTime){
            return outstring;
        }else{
            return "success";
        }
    }

    public static void main(String[] args) throws IOException {
        //ffmpeg的路径
        String ffmpegPath = "D:\\ffmpeg\\ffmpeg.exe";
        //源avi视频的路径
        String videoPath = "D:\\Videos\\测试.avi";
        //转换后mp4文件的名称
        String mp4Name = "寄明月.mp4";
        //转换后mp4文件的路径
        String mp4Path = "D:\\" + mp4Name;
        //创建工具类对象
        Mp4VideoUtil videoUtil = new Mp4VideoUtil(ffmpegPath,videoPath,mp4Name,mp4Path);
        //开始视频转换，成功将返回success
        String s = videoUtil.generateMp4();
        System.out.println(s);
    }
}