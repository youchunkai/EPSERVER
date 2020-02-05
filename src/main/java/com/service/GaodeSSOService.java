package com.service;

import java.util.HashMap;

import com.alibaba.fastjson.JSONObject;
import com.util.LoadDataFromUrl;

/**
 * 高德单点登陆接口服务
 * 
 * */
public class GaodeSSOService {
	
	/**
	 * 高德登陆服务
	 * */
	public JSONObject gaodeLogin(String usname,String password){
		LoadDataFromUrl ld = new LoadDataFromUrl();
		HashMap<String, String> map = new HashMap<String, String>();
		map.put("Content-Type", "application/json");
		JSONObject jobj = new JSONObject();
		jobj.put("loginname", usname);
		jobj.put("password", password);
		byte[] s = ld.requestMethod("POST","http://113.137.32.162:8083/PtcPerson/login",jobj.toJSONString(),map);
		JSONObject result = JSONObject.parseObject(new String(s));
		return result;
	}
	/***
	 * 高德check
	 * */
	public JSONObject gaodeCheck(String token){
		LoadDataFromUrl ld = new LoadDataFromUrl();
		HashMap<String, String> map = new HashMap<String, String>();
		map.put("Content-Type", "application/json");
		JSONObject jobj = new JSONObject();
		jobj.put("token", token);
		byte[] s = ld.requestMethod("POST","http://113.137.32.162:8083/PtcPerson/person",jobj.toJSONString(),map);
		JSONObject result = JSONObject.parseObject(new String(s));
		return result;
	}

//	/**
//	 * @param args
//	 */
//	public static void main(String[] args) {
//		// TODO Auto-generated method stub
//		LoadDataFromUrl ld = new LoadDataFromUrl();
//		HashMap<String, String> map = new HashMap<String, String>();
//		map.put("Content-Type", "application/json");
//		JSONObject jobj = new JSONObject();
//		jobj.put("loginname", "test");
//		jobj.put("password", "123");
//		byte[] s = ld.requestMethod("POST","http://113.137.32.162:8083/PtcPerson/login",jobj.toJSONString(),map);
//		System.out.println(new String(s));
//	}

}
