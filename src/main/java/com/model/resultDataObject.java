package com.model;

import java.util.HashMap;

public class resultDataObject {
	private HashMap<String, Object> serviceinfo;
	private boolean result;
	private Object data;
	private Object updatecount;
	private Object pageinfo;
	private String redirect;
	private String error;
	private Object sql;
	private String setCookieUrl;
	private Object sqlparam;
	private String Authinfo;
	private boolean isCache = false;
	private HashMap<String,Object> commonparam;
	private String cacheKey;
	
	public boolean isResult() {
		return result;
	}
	public void setResult(boolean result) {
		this.result = result;
	}
	public Object getData() {
		return data;
	}
	public void setData(Object data) {
		this.data = data;
	}
	public String getError() {
		return error;
	}
	public void setError(String error) {
		this.error = error;
	}
	public Object getPageinfo() {
		return pageinfo;
	}
	public void setPageinfo(Object pageinfo) {
		this.pageinfo = pageinfo;
	}
	public Object getUpdatecount() {
		return updatecount;
	}
	public void setUpdatecount(Object updatecount) {
		this.updatecount = updatecount;
	}
	public String getRedirect() {
		return redirect;
	}
	public void setRedirect(String redirect) {
		this.redirect = redirect;
	}
	public Object getSql() {
		return sql;
	}
	public void setSql(Object sql) {
		this.sql = sql;
	}
	public String getSetCookieUrl() {
		return setCookieUrl;
	}
	public void setSetCookieUrl(String setCookieUrl) {
		this.setCookieUrl = setCookieUrl;
	}
	public HashMap<String, Object> getServiceinfo() {
		return serviceinfo;
	}
	public void setServiceinfo(HashMap<String, Object> serviceinfo) {
		this.serviceinfo = serviceinfo;
	}
	public HashMap<String, Object> getCommonparam() {
		return commonparam;
	}
	public void setCommonparam(HashMap<String, Object> commonparam) {
		this.commonparam = commonparam;
	}
	public Object getSqlparam() {
		return sqlparam;
	}
	public void setSqlparam(Object sqlparam) {
		this.sqlparam = sqlparam;
	}
	public String getAuthinfo() {
		return Authinfo;
	}
	public void setAuthinfo(String authinfo) {
		Authinfo = authinfo;
	}
	public boolean isCache() {
		return isCache;
	}
	public void setCache(boolean isCache) {
		this.isCache = isCache;
	}
	public String getCacheKey() {
		return cacheKey;
	}
	public void setCacheKey(String cacheKey) {
		this.cacheKey = cacheKey;
	}
	
}
