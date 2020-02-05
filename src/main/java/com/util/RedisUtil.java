package com.util;

import java.util.HashMap;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class RedisUtil {
	//服务器IP地址
    private static String ADDR = PropertiesConfigUtil.getConfigByKey("redis_ip");
	//端口
    private static int PORT = Integer.parseInt(PropertiesConfigUtil.getConfigByKey("redis_port"));
	//密码
    private static String AUTH = PropertiesConfigUtil.getConfigByKey("redis_author");
    //密码
    private static int DB = Integer.parseInt(PropertiesConfigUtil.getConfigByKey("redis_db"));
	//连接实例的最大连接数
    private static int MAX_ACTIVE = 2000;
	//控制一个pool最多有多少个状态为idle(空闲的)的jedis实例，默认值也是8。
    private static int MAX_IDLE = 200;
    //等待可用连接的最大时间，单位毫秒，默认值为-1，表示永不超时。如果超过等待时间，则直接抛出JedisConnectionException
    private static int MAX_WAIT = 10000;
	//连接超时的时间　　
    private static int TIMEOUT = 10000;

	// 在borrow一个jedis实例时，是否提前进行validate操作；如果为true，则得到的jedis实例均是可用的；
    private static boolean TEST_ON_BORROW = true;
    private static HashMap<Integer, JedisPool> jedispoolMap = new HashMap<Integer, JedisPool>();;

    /**
     * 初始化Redis连接池
     */

    static {
    	try{
    		getJedisPool(0);
    		//15 用来放置 应用数据的缓存数据
    		getJedisPool(15);
    	}catch (Exception e) {
			e.printStackTrace();
		}
    }
    
    private static void getJedisPool(Integer index) throws Exception {
    	try {
            JedisPoolConfig config = new JedisPoolConfig();
            config.setMaxTotal(MAX_ACTIVE);
            config.setMaxIdle(MAX_IDLE);
            config.setMaxWaitMillis(MAX_WAIT);
            config.setTestOnBorrow(TEST_ON_BORROW);

            jedispoolMap.put(index, new JedisPool(config, ADDR, PORT, TIMEOUT,AUTH,index)) ;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * 获取Jedis实例
     */
    public synchronized static Jedis getJedis() {
    	return getJedis(0);
    }
    public synchronized static Jedis getJedis(int index) {
        try {
            if (jedispoolMap.get(index) != null) {
                Jedis resource = jedispoolMap.get(index).getResource();
                return resource;
            } else {
            	try {
            		getJedisPool(index);
            		return getJedis(index);
				} catch (Exception e) {
					 return null;
				}
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /***
     * 
     * 释放资源
     */
    
    public static void returnResource(final Jedis jedis) {
            if(jedis != null) {
            	JedisPool pool  = jedispoolMap.get(Integer.parseInt(jedis.getDB().toString()));
            	pool.returnResource(jedis);
            }
        
    }
    
    public static void returnBrokenResource(Jedis jedis){
    	 if(jedis != null) {
    		 jedispoolMap.get(jedis.getDB()).returnBrokenResource(jedis);
         }
    }
}
