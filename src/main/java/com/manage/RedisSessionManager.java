package com.manage;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import com.util.RedisUtil;

import redis.clients.jedis.Jedis;

public class RedisSessionManager {
	private static Jedis jedis = null;
	public enum SessionEnum {
	    SESSIONMAIN,USCODEMAIN
	}
	
	public static HashMap<String, Object> getSessionMap(SessionEnum type){
		HashMap result = new HashMap<String, Object>();
		
		Jedis jedis = RedisUtil.getJedis();        
        Set<String> keySet = jedis.keys("*");   
        String[] keyArr = keySet.toArray(new String[keySet.size()]);
        
        for (int i = 0; i < keyArr.length; ++i) {
        	String key = keyArr[i];
        	String uscode = "NULLUSER";
        	HashMap<String, Object> sessionobj = null;
        	try{
        		byte[] data = jedis.get(key.getBytes());       		     		
        		ObjectInputStream ois = null;  //对象输入流		
        		ByteArrayInputStream bis = null;   //内存缓冲流		
        		bis = new ByteArrayInputStream(data);		
        		ois = new ObjectInputStream(bis);		
        		Object obj = ois.readObject();	
        		
        		long creationTime = ((Long)ois.readObject()).longValue();
        		long lastAccessedTime = ((Long)ois.readObject()).longValue();
        		int maxInactiveInterval = ((Integer)ois.readObject()).intValue();
        		boolean isNew = ((Boolean)ois.readObject()).booleanValue();
        	    boolean isValid = ((Boolean)ois.readObject()).booleanValue();
        	    long thisAccessedTime = ((Long)ois.readObject()).longValue();
        		String id = (String)ois.readObject();
        		
        		int attrcount = ((Integer)ois.readObject()).intValue();
        		HashMap<String, Object> attrs = new HashMap<String, Object>();
        		for(int m = 0; m < attrcount; ++m) {
                    String name = (String)ois.readObject();
                    
                    Object value = ois.readObject();
                    attrs.put(name, value);
                    
                    if(name.equals("user")){
                    	HashMap<String, Object> user = (HashMap<String, Object>)value;
                    	if(user != null){
                    		uscode = user.get("US_CODE").toString();
                    	}
                    }
        		}
        		bis.close();		
        		ois.close();
        		
        		sessionobj = new HashMap<String, Object>();
        		sessionobj.put("creationTime", creationTime);
        		sessionobj.put("lastAccessedTime", lastAccessedTime);
        		sessionobj.put("maxInactiveInterval", maxInactiveInterval);
        		sessionobj.put("isNew", isNew);
        		sessionobj.put("isValid", isValid);
        		sessionobj.put("thisAccessedTime", thisAccessedTime);
        		sessionobj.put("id", id);
        		sessionobj.put("attr", attrs);
        	}catch (Exception e) {
				e.printStackTrace();
			}
        	
        	if(sessionobj != null){
	        	if(type == SessionEnum.SESSIONMAIN)
	        		result.put(key, sessionobj);
	        	else{
	        		if(result.containsKey(uscode)){
	        			ArrayList<Object> arr = (ArrayList<Object>)result.get(uscode);
	        			arr.add(sessionobj);
	        		}else{
	        			ArrayList<Object> arr = new ArrayList<Object>();
	        			arr.add(sessionobj);
	        			result.put(uscode, arr);
	        		}
	        	}
        	}
        }
		return result;
	}
	
	public static HashMap<String, Object> getSession(String jid){		
		Jedis jedis = RedisUtil.getJedis();        
        String key = jid;
        String uscode = "NULLUSER";
        HashMap<String, Object> sessionobj = null;
        try{
        	byte[] data = jedis.get(key.getBytes());       		     		
        	ObjectInputStream ois = null;  //对象输入流		
        	ByteArrayInputStream bis = null;   //内存缓冲流		
        	bis = new ByteArrayInputStream(data);		
        	ois = new ObjectInputStream(bis);		
        	Object obj = ois.readObject();	
        		
        	long creationTime = ((Long)ois.readObject()).longValue();
        	long lastAccessedTime = ((Long)ois.readObject()).longValue();
        	int maxInactiveInterval = ((Integer)ois.readObject()).intValue();
        		boolean isNew = ((Boolean)ois.readObject()).booleanValue();
        	    boolean isValid = ((Boolean)ois.readObject()).booleanValue();
        	    long thisAccessedTime = ((Long)ois.readObject()).longValue();
        		String id = (String)ois.readObject();
        		
        		int attrcount = ((Integer)ois.readObject()).intValue();
        		HashMap<String, Object> attrs = new HashMap<String, Object>();
        		for(int m = 0; m < attrcount; ++m) {
                    String name = (String)ois.readObject();
                    
                    Object value = ois.readObject();
                    attrs.put(name, value);
                    
                    if(name.equals("user")){
                    	HashMap<String, Object> user = (HashMap<String, Object>)value;
                    	if(user != null){
                    		uscode = user.get("US_CODE").toString();
                    	}
                    }
        		}
        		bis.close();		
        		ois.close();
        		
        		sessionobj = new HashMap<String, Object>();
        		sessionobj.put("creationTime", creationTime);
        		sessionobj.put("lastAccessedTime", lastAccessedTime);
        		sessionobj.put("maxInactiveInterval", maxInactiveInterval);
        		sessionobj.put("isNew", isNew);
        		sessionobj.put("isValid", isValid);
        		sessionobj.put("thisAccessedTime", thisAccessedTime);
        		sessionobj.put("id", id);
        		sessionobj.put("attr", attrs);
        	}catch (Exception e) {
				e.printStackTrace();
			}
        	
		return sessionobj;
	}
	
	/**获取当前在线人数*/
	public static int getSessionCount(){		
		Jedis jedis = RedisUtil.getJedis();        
        Set<String> keySet = jedis.keys("*"); 
        return keySet.size();
	}
	
	/**
	 * 通过用户ID获取session信息
	 * */
	public static String getSessionIdByUserId(String uscode){
		
		return "";
	}
	/***
	 * 通过sessionid移除会话
	 * 
	 * */
	public static boolean removeSessionBySessionID(String sessionid){
		
		return true;
	}
	/***
	 * 通过用户ID移除该用户全部会话
	 * */
	public static boolean removeAllSessionByUsCode(String uscode){
		
		return true;
	}

}
