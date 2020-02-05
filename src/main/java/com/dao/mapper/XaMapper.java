package com.dao.mapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.springframework.stereotype.Repository;
/***
 * 20180110
 * Z 
 * */
@Repository(value = "XaMapper")
public interface XaMapper {	
	
	public List<HashMap<String,Object>> queryDataSources();
	
	public List<HashMap<String,Object>> queryAllService();
	
	public ArrayList<HashMap<String,Object>> queryAllQxCtrl();
	
	public List<HashMap<String,Object>> queryServiceById(HashMap<String, Object> hashMap);

	/**查询历史访问量*/
	public List<HashMap<String, Object>> getHistoryVisit();

	/**查询服务与权限关系*/
	public List<HashMap<String,Object>> queryServerAuthRefs();
	
	/**缓存用户服务授权关系*/
	public List<HashMap<String,Object>> queryAllUserAndService();
}
