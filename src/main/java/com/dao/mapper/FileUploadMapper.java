package com.dao.mapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.springframework.stereotype.Repository;
/***
 * 20180628
 * Y 
 * */
@Repository(value = "FileUploadMapper")
public interface FileUploadMapper {

	public List<HashMap<Object, Object>> test();
	
	public void fileUploadResource(List<HashMap<String, String>> strList);
	
	public HashMap<String,Object> getResourceType(HashMap<String, String> hashMap);
	
	public List<HashMap<String,Object>> getAllResourceType();
	
	public void addResource(HashMap<String, String> hashMap);
	
	public void deleteResource(HashMap<String, Object> hashMap);
	
	public void addEvent(HashMap<String, String> hashMap);
	
	public void addEventResource(HashMap<String, String> hashMap);
	
	public HashMap<String,Integer> searchEventId();
	
	public void addResourceGroup(HashMap<String, Object> map);
	
	public void deleteResourceGroup(HashMap<String, Object> map);
	
	public void addResourceGroupItem(ArrayList<HashMap<String, Object>> list);
	
}
