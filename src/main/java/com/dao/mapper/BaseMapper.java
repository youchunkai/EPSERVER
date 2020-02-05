package com.dao.mapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public interface BaseMapper {

	public List<HashMap<String,Object>> searchUserByUsSubcode(HashMap<String, Object> hashMap);
	
	public List<HashMap<String,Object>> searchUser(HashMap<String, Object> hashMap);
	
	public HashMap<String,Object> getUtCode(HashMap<String, Object> hashMap);
	
	public void addUser(HashMap<String, Object> hashMap);

	public void addLog(HashMap<String, Object> hashMap);
	
	public int addLogBatch(ArrayList<HashMap<String, Object>> list);
}
