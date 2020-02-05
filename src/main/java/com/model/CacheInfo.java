package com.model;

import com.alibaba.fastjson.JSONObject;

/**
 *decs:缓存信息 
 **/
public class CacheInfo {
	
	//缓存key
	private String key;
	//缓存value
	private resultDataObject value;
	//是否需要缓存
	private boolean isNeedCache;
	//是否缓存空结果标志位
	private boolean isCacheEmpty;
	//缓存配置条件
	private JSONObject config;
	//缓存过期时间
	private int expireSeconds;
	
	
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	public resultDataObject getValue() {
		return value;
	}
	public void setValue(resultDataObject value) {
		this.value = value;
	}
	public boolean isNeedCache() {
		return isNeedCache;
	}
	public void setNeedCache(boolean isNeedCache) {
		this.isNeedCache = isNeedCache;
	}
	public boolean isCacheEmpty() {
		return isCacheEmpty;
	}
	public void setCacheEmpty(boolean isCacheEmpty) {
		this.isCacheEmpty = isCacheEmpty;
	}
	public JSONObject getConfig() {
		return config;
	}
	public void setConfig(JSONObject config) {
		this.config = config;
	}
	public int getExpireSeconds() {
		return expireSeconds;
	}
	public void setExpireSeconds(int expireSeconds) {
		this.expireSeconds = expireSeconds;
	}
	
	
	

}
