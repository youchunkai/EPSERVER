package com.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.UUID;

public class PhantomUtil {
    private static String BLANK = " ";
    // 下面内容可以在配置文件中配置
    private static String binPath = "D:/Program Files(N)/phantomjs-2.1.1-windows/bin/phantomjs.exe";

    // 执行cmd命令
    public static String cmd(String js,String imgagePath, String url) {
        return binPath + BLANK + js + BLANK + url + BLANK + imgagePath + BLANK + "1920px*1080px";
    }
    //关闭命令
    public static void close(Process process, BufferedReader bufferedReader) throws IOException {
        if (bufferedReader != null) {
            bufferedReader.close();
        }
        if (process != null) {
            process.destroy();
            process = null;
        }
    }
    public static String printUrlScreen2jpg(String url) throws IOException{
    	System.out.println("开始web截图");
    	String tempPath = System.getProperty("java.io.tmpdir");
        String imgagePath = tempPath+"/"+UUID.randomUUID().toString()+".png";//图片路径
        System.out.println(imgagePath);
        String jsp = PhantomUtil.class.getResource("phantomjs/webclip.js").getPath();
        jsp = jsp.substring(1, jsp.length());
        Process process = Runtime.getRuntime().exec(cmd(jsp,imgagePath,url));
        InputStream inputStream = process.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String tmp = "";
        while ((tmp = reader.readLine()) != null) {
            close(process,reader);
        }
        System.out.println("web截图success");
        return imgagePath;
    }

}
