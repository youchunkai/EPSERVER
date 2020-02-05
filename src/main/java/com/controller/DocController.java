package com.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.util.FreeMarkerUtils;

@Controller
@RequestMapping(value="/DOC")
@SuppressWarnings("all")
public class DocController {
	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	SimpleDateFormat sdf2 = new SimpleDateFormat("yyyyMMddHH24mmss");
	
	@RequestMapping(value="/WORD",method=RequestMethod.GET)
	public @ResponseBody void test3(HttpServletRequest request,HttpServletResponse response) throws Exception {		
		//获得数据  
	   	 Map<String, Object> map = new HashMap<String, Object>(); 
	   	 map.put("tablenale", "biao名");
	   	 map.put("val", "按时发生巨大的繁华过后的方济各会的附近");
	   	 
	   	 List<Map<String, Object>> newsList=new ArrayList<Map<String,Object>>();
	   	 for(int i=1;i<=10;i++){
	          	Map<String, Object> map1=new HashMap<String, Object>();
	          	map1.put("val1", "标题"+i);
	          	map1.put("val2", "内容"+(i*2));
	          	map1.put("val3", "作者"+(i*3));
	          	map1.put("val4", "作者"+(i*4));
	          	newsList.add(map1);
	         }
	   	 
	   	 map.put("datalist", newsList);
	   	 map.put("img", FreeMarkerUtils.getImageBase("D://11.png"));
	   	 
	   	InputStream fin = null;  
        ServletOutputStream out = null; 
	   	 try {
	   		 File file = FreeMarkerUtils.renderWord(map,"D:/templates/1111.ftl");
	   		 
	   		 try{
		         fin = new FileInputStream(file);  
		         response.setCharacterEncoding("utf-8");  
		         response.setContentType("application/msword"); 
		         
		         String fileName = "方案" + sdf2.format(new Date()) + ".doc";  
		         response.setHeader("Content-Disposition", "attachment;filename=".concat(String.valueOf(URLEncoder.encode(fileName, "UTF-8"))));  
		  
		         out = response.getOutputStream();  
		         byte[] buffer = new byte[512];  // 缓冲区  
		         int bytesToRead = -1;  
		         // 通过循环将读入的Word文件的内容输出到浏览器中  
		         while((bytesToRead = fin.read(buffer)) != -1) {  
		             out.write(buffer, 0, bytesToRead);  
		         }
	         }finally{
	        	 if(fin != null) fin.close();  
		         if(out != null) out.close();
		         if(file != null) file.delete();
	         }
	     } catch (IOException e) {
	         // TODO Auto-generated catch block
	         e.printStackTrace();
	         throw e;
	     }
	}
}
