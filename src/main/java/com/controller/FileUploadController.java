package com.controller;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.model.resultDataObject;
import com.service.FileUploadService;
import com.util.FileUploadUtil;


/**
 * @author admin
 */
@Controller
@SuppressWarnings("all")
public class FileUploadController {
	
	@Resource
	FileUploadService fileUploadService;
	
	@RequestMapping(value="/test")
	public @ResponseBody List<HashMap<Object, Object>>  test(){
		return fileUploadService.test();
	}
	
	/**
	 * 通用的文件上传接口
	 * */
	@RequestMapping(value="/fileUpload",method=RequestMethod.POST)
	@ResponseBody
	public resultDataObject fileUpload(MultipartFile file,HttpServletRequest request, HttpServletResponse response){
		//根据参数 转存文件
		resultDataObject result = FileUploadUtil.fieUpload(file);
		return result;
	} 
	
	/**
	 * 通用的文件下载接口,该方法有待完善
	 * */
	@RequestMapping(value="/fileDownload",method={RequestMethod.POST,RequestMethod.GET})
	public ResponseEntity<byte[]> fileDownload(@RequestBody Map<String,Object> params,HttpServletRequest request, HttpServletResponse response) throws IOException{
		//根据参数下载文件
		String path = params.get("re_address").toString();//要下载的文件地址
		String downloadFielName = params.get("re_name").toString();//以原文件名下载
		response.setHeader("Content-disposition","attachment;filename=aaa.png");
		HttpHeaders headers = new HttpHeaders();  
		//headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);//设置浏览器需要下载
		//headers.setContentDispositionFormData("attachment", downloadFielName); //设置浏览器打开下载功能
		return new ResponseEntity<byte[]>(FileUtils.readFileToByteArray(new File(path)), headers, HttpStatus.CREATED);
			
	} 
	
	
}
