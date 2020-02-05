package com.manage.tasks;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.annotation.Resource;

import oracle.sql.CLOB;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.dao.mapper.FileUploadMapper;
import com.dao.mapper.XaMapper;
import com.service.BaseService;
import com.util.ConvertUtil;
import com.util.FtpUtils;
import com.util.PropertiesConfigUtil;

@Component(value = "CacheTask")
public class CacheTask implements InitializingBean{
	private static final Logger logger = Logger.getLogger(BaseService.class);
	
	@Resource(name = "XaMapper")
	private XaMapper xaMapper;
	@Resource(name="FileUploadMapper")
	private FileUploadMapper fileUploadMapper;
	private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	/**资源类型对照关系*/
	public static HashMap<String, String> resTypeMap = new HashMap<String, String>();
	/**缓存服务对象*/
	public static HashMap<String, HashMap<String, Object>> servieMap = new HashMap<String, HashMap<String,Object>>();
	/**缓存服务与数据权限关系信息*/
	public static HashMap<Integer,List<Integer>> serverAuthRefs = new HashMap<Integer, List<Integer>>(); 
	/**缓存数据权限信息*/
	public static HashMap<Integer,HashMap<String,Object>> authInfos = new HashMap<Integer, HashMap<String,Object>>();
	/**缓存用户接口关系授权关系*/
	public static HashMap<String,String> userSvrInfos = new HashMap<String, String>();
	/**
	 * 初始化缓存信息
	 * */
	@Override
	public void afterPropertiesSet() throws Exception {
//		CacheTask t = new CacheTask();
		try{
			//初始化服务列表缓存
			updateServiceMap();
			//获取权限信息
			getAuthInfos();
			//查找服务与权限对应关系
			getServerAuthRefs();
			//
			getUserServiceRefs();

			//资源类型对照关系
			List<HashMap<String, Object>> dataflagArr = fileUploadMapper.getAllResourceType();
			for(int i = 0; i < dataflagArr.size();i++){
				HashMap<String, Object> map = dataflagArr.get(i);
				resTypeMap.put(map.get("RT_NAME").toString(), map.get("RT_CODE").toString());
			}
			
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
	/**
	 * 检查上传失败的缓存文件
	 * */
	@Scheduled(cron = "${job.fileftp.update}")
	public static void checkfaildFtpCacheFile() throws Exception{
		if(PropertiesConfigUtil.getConfigByKey("resourceSaveType").equals("ftp")){
			try {
				String fileDir = PropertiesConfigUtil.getConfigByKey("fileCacheFolder") + "/resources";//文件存放父目录
		    	File toDir = new File(fileDir);
		        if(!toDir.exists()){
		        	return;
		        }
		        
		        File[] files = toDir.listFiles();
		        for(int i = 0;i<files.length;i++){
		        	File itemfile = files[i];
		        	if(itemfile.isDirectory()){
		        		File[] dayfiles = itemfile.listFiles();
		        		if(dayfiles.length <= 0){
		        			itemfile.delete();
		        		}else{
		        			for(int j = 0;j<dayfiles.length;j++){
		        				File aaa = dayfiles[j];
		        				
		        				if(aaa.canWrite()){
			        				String filename = aaa.getName();
			        				try{
				        				FtpUtils ftp = new FtpUtils(
				        		        		PropertiesConfigUtil.getConfigByKey("uploadFilesFtpHost"),
				        		        		Integer.parseInt(PropertiesConfigUtil.getConfigByKey("uploadFilesFtpport")),
				        		        		PropertiesConfigUtil.getConfigByKey("uploadFilesUser"),
				        		        		PropertiesConfigUtil.getConfigByKey("uploadFilesPassWord")); 
				        		        boolean bo = ftp.uploadFile("resources/"+itemfile.getName(), filename, aaa.getAbsolutePath());
				        		        
				        		        if(bo){
				        		        	aaa.delete();
				        		        }
			        				}catch (Exception e) {
										;
									}
		        				}
		        			}
		        		}
		        	}
		        }
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		}
	}
	/**
	 * 定时刷新服务服务列表及数据全新缓存信息
	 * */
	@Scheduled(cron = "${job.service.update}")
	public void updateServiceMapJob() throws Exception{
		try {
			updateServiceMap();
			getAuthInfos();
			getServerAuthRefs();
			getUserServiceRefs();
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}
	/**获取权限信息*/
	private void getAuthInfos() {
        HashMap<Integer,HashMap<String,Object>> result = new HashMap<Integer,HashMap<String,Object>>();
		List<HashMap<String, Object>> authList = xaMapper.queryAllQxCtrl();
		for (HashMap<String, Object> hashMap : authList) {
			result.put(Integer.valueOf(hashMap.get("PSD_CODE").toString()), hashMap);
		}
		authInfos = result;
	}
	/**获取用户接口授权关系**/
	private void getUserServiceRefs(){
		List<HashMap<String, Object>> usersvrs = xaMapper.queryAllUserAndService();
		for (HashMap<String, Object> hashMap : usersvrs) {
			String us_code = hashMap.get("US_CODE").toString();
			CLOB ds_code = (oracle.sql.CLOB)hashMap.get("DS_CODE");
			
			String dsCodeStr = "";
			try{
				dsCodeStr = ConvertUtil.ClobToString(ds_code);
			}catch (Exception e) {
				e.printStackTrace();
			}
			userSvrInfos.put(us_code, dsCodeStr);
		}
	}
	/**
	 * 对服务权限关系做个转换,便于后期查找
	 * */
	private void getServerAuthRefs() {
		HashMap<Integer,List<Integer>> result = new HashMap<Integer, List<Integer>>();
		List<HashMap<String, Object>> serverAuthList = xaMapper.queryServerAuthRefs();
		for (HashMap<String, Object> hashMap : serverAuthList) {
			int dsCode = Integer.valueOf(hashMap.get("DS_CODE").toString());
			int psdCode = Integer.valueOf(hashMap.get("PSD_CODE").toString());
			if(result.containsKey(dsCode)){
				result.get(dsCode).add(psdCode);
			}else{
				List<Integer> psdList = new ArrayList<Integer>();
				psdList.add(psdCode);
				result.put(dsCode,psdList);
			}
		}
		serverAuthRefs = result;		
	}
	/**
	 * 更新服务缓存信息
	 * */
	private void updateServiceMap() throws Exception{
		if(PropertiesConfigUtil.getConfigByKey("isdebuger").equals("0")){
			try {
				List<HashMap<String, Object>> sl = xaMapper.queryAllService();
				for(HashMap<String, Object> map :sl){
					CLOB clob = (oracle.sql.CLOB)map.get("DS_SQLCLOB");
					CLOB logclob = (oracle.sql.CLOB)map.get("DS_LOGSQL");
					map.put("DS_SQLCLOB", ConvertUtil.ClobToString(clob));
					map.put("DS_LOGSQL", ConvertUtil.ClobToString(logclob));
		
					//解析缓存配置
					Object v = map.get("DS_ISCACHE");
					String cahceSwitch = v != null ?v.toString():"";
					if ("1".equalsIgnoreCase(cahceSwitch))
					{
						try 
						{
							v = map.get("DS_CACHECONFIG");
							String cacheconfig =v != null ? v.toString():null;
							if (cacheconfig != null && !"".equalsIgnoreCase(cacheconfig))
							{
								com.alibaba.fastjson.JSONObject cacheconfigobj = com.alibaba.fastjson.JSONObject.parseObject(cacheconfig);
								map.remove("DS_CACHECONFIG");
								map.put("DS_CACHECONFIG", cacheconfigobj);
							}
						}
						catch (Exception e) 
						{
							map.put("DS_CACHECONFIG", null);
						}
					}
					else
					{
						map.put("DS_CACHECONFIG", null);
					}
		
					servieMap.put(map.get("DS_CODE").toString(), map);
				}
			} catch (Exception e1) {
				logger.error(sdf.format(new Date())+":刷新平台服务列表失败!");
				e1.printStackTrace();
				throw e1;
			}
		}
	}

}
