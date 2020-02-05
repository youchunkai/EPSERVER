package com.controller;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.model.resultDataObject;
import com.service.CacheService;

@Controller
@RequestMapping(value="/cache")
public class CacheController implements InitializingBean{
	@Autowired
	CacheService cacheService;
	
	/**
	 *根据缓存的key移除缓存信息 
	 **/
	@RequestMapping(value="/removeCacheByCacheKey",method=RequestMethod.POST)
	@ResponseBody
	public resultDataObject removeCacheByCacheKey(String key){
		System.out.println("key====="+key);
		return cacheService.removeCacheByCacheKey(key);
	}
	
	/**
	 *根据服务id移除缓存信息 
	 **/
	@RequestMapping(value="/removeCacheByServiceId",method=RequestMethod.POST)
	@ResponseBody
	public resultDataObject removeCacheByServiceId(String serviceId){
		return cacheService.removeCacheByServiceId(serviceId);
	}

	@RequestMapping(value="/test",method = RequestMethod.GET)
	public void test(){
		System.out.println("test");
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		cacheService.autoRemoveCache();
		
	}

}
