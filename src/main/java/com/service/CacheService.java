package com.service;

import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Service;

import redis.clients.jedis.Jedis;

import com.model.CacheInfo;
import com.model.resultDataObject;
import com.util.ConvertUtil;
import com.util.PropertiesConfigUtil;
import com.util.RedisUtil;

import consumer.MsgConsumer;
import consumer.MsgRecord;

@Service(value = "CacheService")
public class CacheService {
	
	
	/**
	 *根据缓存key移除缓存 
	 **/
	public resultDataObject removeCacheByCacheKey(String key) {
		Jedis jedis = null;
		resultDataObject result = new resultDataObject();
		try{
			jedis = RedisUtil.getJedis(15);
			jedis.del(key);
			result.setResult(true);
			result.setError(key+"缓存已删除。");
		}catch(Exception e){
			result.setResult(false);
			result.setError(key+"缓存删除失败！");
		}finally{
			RedisUtil.returnResource(jedis);
		}		
		return result;
	}
	
	/**
	 *根据服务id移除缓存 
	 **/
	public static resultDataObject removeCacheByServiceId(String serviceId) {
		Jedis jedis = null;
		resultDataObject result = new resultDataObject();
		try{
			jedis = RedisUtil.getJedis(15);
			Set<String> keys = jedis.keys(serviceId+"_*");
			for (String key : keys) {
				jedis.del(key);
			}			
			result.setResult(true);
			result.setError("服务id为："+serviceId+"的缓存已删除。");
		}catch(Exception e){
			result.setResult(false);
			result.setError("服务id为："+serviceId+"的缓存删除失败！");
		}finally{
			RedisUtil.returnResource(jedis);
		}		
		return result;
	}
	
	
	/**创建缓存的key
	 * @param serviceid */
	public String createCacheKeyByParams(int serviceid, List<HashMap<String, Object>> params,
			HashMap<String, Object> commonParams) {
		String serviceId = String.valueOf(serviceid);
		StringBuilder cacheKey = new StringBuilder(serviceId);

		for(int m = 0;m < params.size();m++){
			HashMap<String, Object> param = params.get(m);
			String value = param.get("value").toString();
			cacheKey.append("_").append(value);
		}
		
		if(commonParams != null){
			if(commonParams.get("ispages")!=null){
				int cts = 100;
				if(commonParams.get("pagecount")!=null)
					cts = Integer.parseInt(commonParams.get("pagecount").toString());
				int cp = 1;
				if(commonParams.get("currentpage")!=null)
					cp = Integer.parseInt(commonParams.get("currentpage").toString());
				cacheKey.append("_").append(cts).append("_").append(cp);
			}
			if(commonParams.get("orderfield")!=null){
				if(commonParams.get("order")!=null){
					cacheKey.append("_").append(commonParams.get("orderfield").toString())
					        .append("_").append(commonParams.get("order").toString());
				}else{
					cacheKey.append("_").append(commonParams.get("orderfield").toString())
					        .append("_asc nulls first");
				}
			}
			if(commonParams.get("advaceorder")!=null){
				cacheKey.append("_").append(commonParams.get("advaceorder").toString());
			}
		}
		return serviceId+"_"+ConvertUtil.stringToMD5(cacheKey.toString());
	}
	
	
	/**
	 * 设置缓存信息
	 * */
	public void setCache(CacheInfo cacheInfo, resultDataObject result) {
		if(result.isResult()&&cacheInfo.isNeedCache()){
			Jedis jedis = null;
			try{
				jedis = RedisUtil.getJedis(15);
				result.setCacheKey(cacheInfo.getKey());
				String value = com.alibaba.fastjson.JSONObject.toJSONString(result);
				jedis.setex(cacheInfo.getKey(),cacheInfo.getExpireSeconds(),value);
			}finally{
				RedisUtil.returnResource(jedis);
			}		
		}
	}

	/**
	 *自动移除缓存 
	 **/
	public void autoRemoveCache() {
		new Thread(
				new Runnable() {
					
					@Override
					public void run() {
						MsgConsumer consumer = new MsgConsumer();
						String topic = "xasync";
						while(true){
							try {
								List<MsgRecord> msgRecords = consumer.subscribe(topic);
								for (MsgRecord msg : msgRecords) {
									System.out.println(msg);
									String serviceIds = PropertiesConfigUtil.getConfigByKey(msg.getValue());
									System.out.println(serviceIds);
									for(String serviceId : serviceIds.split(",")){
										removeCacheByServiceId(serviceId.trim());
									}
								}							
								TimeUnit.SECONDS.sleep(10);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						}						
					}
				},"removeXasyncCacheThread"
				).start();		
	}

}
