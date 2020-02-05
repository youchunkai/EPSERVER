package com.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Map;
import java.util.UUID;
import sun.misc.BASE64Encoder;

import freemarker.template.Configuration;
import freemarker.template.Template;

public class FreeMarkerUtils {
     //配置信息,代码本身写的还是很可读的,就不过多注解了  
    private static Configuration configuration = null;
  
    private FreeMarkerUtils() {  
        throw new AssertionError();  
    }  
  
    public static File renderWord(Map map,String ftlFile) throws IOException {
    	File f = new File(ftlFile);
    	String path = f.getAbsolutePath();
    	String tplpath = path.substring(0, path.lastIndexOf('\\'));
    	String tplname = path.substring(path.lastIndexOf('\\')+1,path.length());
    	
    	configuration = new Configuration();  
        configuration.setDefaultEncoding("utf-8");
        configuration.setDirectoryForTemplateLoading(new File(tplpath)); 
        
        Template freemarkerTemplate = configuration.getTemplate(tplname);  
        
        File file = File.createTempFile(UUID.randomUUID().toString(),".doc");  
        try {  
            // 这个地方不能使用FileWriter因为需要指定编码类型否则生成的Word文档会因为有无法识别的编码而无法打开  
            Writer w = new OutputStreamWriter(new FileOutputStream(file), "utf-8");  
            freemarkerTemplate.process(map, w);  
            w.close();  
        } catch (Exception ex) {  
            ex.printStackTrace();  
            throw new RuntimeException(ex);  
        }  
        
        return file;
    }  
    
    //获得图片的base64码
    @SuppressWarnings("deprecation")
    public static String getImageBase(String src) {
        if(src==null||src==""){
            return "";
        }
        File file = new File(src);
        if(!file.exists()) {
            return "";
        }
        InputStream in = null;
        byte[] data = null;  
        try {
            in = new FileInputStream(file);
        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
        }
        try {  
            data = new byte[in.available()];  
            in.read(data);  
            in.close();  
        } catch (IOException e) {  
          e.printStackTrace();  
        } 
        BASE64Encoder encoder = new BASE64Encoder();
        return encoder.encode(data);
    }
}
