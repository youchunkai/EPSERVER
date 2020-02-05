package com.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import com.model.resultDataObject;

import sun.misc.BASE64Decoder;
 
public class FileUploadUtil {
	
	static SimpleDateFormat sdfday = new SimpleDateFormat("yyyy-MM-dd");
	static SimpleDateFormat sdfday2 = new SimpleDateFormat("yyyyMMdd");
	static String baseStr = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789abcdefghijklmnopqrstuvwxyz";
	
    /**
	 * 解析base64文件字符串，并存储文件
	 * */
    public static HashMap<String,String> transFile(String fileStrs,String subfix,String type,String recode) throws IOException{	
    	HashMap<String, Object> resultfile = decodeBase64ToFile(fileStrs, subfix, recode);//存储视频
    		
    	HashMap<String,String> resMap = new HashMap<String,String>();
        resMap.put("RE_ADDRESS", resultfile.get("RE_ADDRESS").toString());//原视频存放全路径；
        resMap.put("RE_URL", resultfile.get("RE_URL").toString());
        resMap.put("RT_SUBPIC", "");//缩略图路径
        resMap.put("RE_SUBPIC_URL", "");//缩略图URL路径
        resMap.put("RT_NAME", type);
        resMap.put("RE_SUBFIX",subfix);
        resMap.put("RE_SIZE",resultfile.get("SIZE").toString());
        resMap.put("RE_NAME", recode);
        
    	return resMap;
    }
    /**
	     * 将Base64位编码的图片进行解码，并保存到指定目录
	     * @param base64
	     *  base64编码的图片信息
	     * @return
	     */
	public static HashMap<String, Object> decodeBase64ToFile(String base64, String subfix,String recode) {
		BASE64Decoder decoder = new BASE64Decoder();
		File uploadFile = null;

		HashMap<String, Object> result = new HashMap<String, Object>();

		//生成文件名
		String baseName = recode;
		String fileName = baseName+"."+subfix;//原图文件名
		try {
			if(PropertiesConfigUtil.getConfigByKey("resourceSaveType").equals("ftp")){
				//先缓存至本机
				String fileDir = PropertiesConfigUtil.getConfigByKey("fileCacheFolder") + "/resources/"+sdfday2.format(new Date());;//文件存放父目录
		    	File toDir = new File(fileDir);
		        if(!toDir.exists()){
		        	toDir.mkdirs();
		        }
		        
		        uploadFile = new File(fileDir +"\\"+fileName);
				FileOutputStream write = new FileOutputStream(uploadFile);
				byte[] decoderBytes = decoder.decodeBuffer(base64);
				write.write(decoderBytes);
				write.close();
				
				result.put("result", true);
				result.put("RE_ADDRESS", "resources/"+sdfday2.format(new Date())+"/"+fileName);
	        	result.put("RE_URL", "resources/"+sdfday2.format(new Date())+"/"+fileName);
	        	result.put("SIZE", String.valueOf(uploadFile.length()));
	        	result.put("error", null);
	        	
	        	try{
			        FtpUtils ftp = new FtpUtils(
			        		PropertiesConfigUtil.getConfigByKey("uploadFilesFtpHost"),
			        		Integer.parseInt(PropertiesConfigUtil.getConfigByKey("uploadFilesFtpport")),
			        		PropertiesConfigUtil.getConfigByKey("uploadFilesUser"),
			        		PropertiesConfigUtil.getConfigByKey("uploadFilesPassWord")); 
			        boolean bo = ftp.uploadFile("resources/"+sdfday2.format(new Date()), fileName, fileDir+"/"+fileName);
			        
			        if(bo){
			        	uploadFile.delete();
			        }
	        	}catch (Exception e) {
					e.printStackTrace();
				}
			}else{
				String serverAdd = PropertiesConfigUtil.getConfigByKey("uploadFilesRootPath");//文件存放根目录
				
		    	String fileDir = serverAdd+"resources/"+sdfday2.format(new Date());//文件存放父目录
		    	File toDir = new File(fileDir);
		        if(!toDir.exists()){
		        	toDir.mkdirs();
		        }
				
				uploadFile = new File(fileDir +"\\"+fileName);
				FileOutputStream write = new FileOutputStream(uploadFile);
				byte[] decoderBytes = decoder.decodeBuffer(base64);
				write.write(decoderBytes);
				write.close();
				
				result.put("result", true);
	        	result.put("RE_ADDRESS", fileDir+"/"+fileName);
	        	result.put("RE_URL", "resources/"+sdfday2.format(new Date())+"/"+fileName);
	        	result.put("SIZE", String.valueOf(uploadFile.length()));
	        	result.put("error", null);
			}
		} catch (IOException e) {
			result.put("result", false);
    		result.put("error", e.getMessage());
			e.printStackTrace();
			e.printStackTrace();
		}
		return result;
	}
	/**
	 * 文件上传
	 * */
	public static resultDataObject fieUpload(MultipartFile file) {
		resultDataObject result = new resultDataObject();
		if(null == file){
			result.setError("上传文件为空");
			result.setResult(false);
		}else{
			String filePath = PropertiesConfigUtil.getConfigByKey("knowledgeUploadPath");
			Date date = new Date();
			String datePath = sdfday.format(date);
			String localPath = filePath + datePath + "/";
			String fileName = file.getOriginalFilename();
			String subfix = fileName.substring(fileName.lastIndexOf(".")+1);
			String localFileName = date.getTime()+"."+subfix;
			try {
				Map<String, Object> fileMap = new HashMap<String, Object>();
				File fileTo = new File(localPath+localFileName);
				//判断路径是否存在，如果不存在就创建一个
	            if (!fileTo.getParentFile().exists()) { 
	        	   fileTo.getParentFile().mkdirs();
	            }
		        //转存文件
				file.transferTo(fileTo);				
				fileMap.put("re_url", "knowledge/"+datePath+"/"+localFileName);
				fileMap.put("re_name", fileName);
				fileMap.put("re_size", ((file.getSize()/1000)<=1?1:(file.getSize()/1000)));
				fileMap.put("re_address",localPath+localFileName);
				fileMap.put("re_subfix",subfix);
				result.setData(fileMap);
				result.setError("文件上传成功");
				result.setResult(true);
			} catch (Exception e) {
				result.setError("文件解析出错");
				result.setResult(false);
				e.printStackTrace();
		    }
	    }
		return result;
    }


	public static ResponseEntity<byte[]> fileDownload(Map<String, Object> params) {
		//下载文件路径
		try{
			
			String path = params.get("re_address").toString();
			HttpHeaders headers = new HttpHeaders();  
			//下载显示的文件名
			String downloadFielName = params.get("re_name").toString();
			//通知浏览器以attachment（下载方式）打开文件
			headers.setContentDispositionFormData("attachment", downloadFielName); 
			// 二进制流数据（文件下载）。
			headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
			try {
				return new ResponseEntity<byte[]>(FileUtils.readFileToByteArray(new File(path)), headers, HttpStatus.CREATED);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}  
		}catch(Exception e){
			e.printStackTrace();
		}
	       return null;
	}	

}
