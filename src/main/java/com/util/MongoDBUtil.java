package com.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;

import java.util.logging.Level;
import java.util.logging.Logger;
 
//mongodb 连接数据库工具类
public class MongoDBUtil {
	
	private static HashMap<String, MongoClient> clientmap = new HashMap<String, MongoClient>();
	
	public MongoDBUtil() {
		Logger mongoLogger = Logger.getLogger( "org.mongodb.driver" );
		mongoLogger.setLevel(Level.WARNING);
	}
	
    //不通过认证获取连接数据库对象
    public static MongoClient getConnect(String ip,int port){
        //连接到 mongodb 服务
        MongoClient mongoClient = new MongoClient(ip, port);
 
        //连接到数据库
//        MongoDatabase mongoDatabase = mongoClient.getDatabase(db);
 
        //返回连接数据库对象
        return mongoClient;
    }
 
    //需要密码认证方式连接
    public static MongoClient getConnect(String ip,int port,String db,String usname,String pword){
    	String key = ip+port+db+usname+pword;
    	MongoClient client = clientmap.get(key);
    	
    	if(client == null){
	        List<ServerAddress> adds = new ArrayList();
	        //ServerAddress()两个参数分别为 服务器地址 和 端口
	        ServerAddress serverAddress = new ServerAddress(ip, port);
	        adds.add(serverAddress);
	        
	        List<MongoCredential> credentials = new ArrayList();
	        MongoCredential mongoCredential = MongoCredential.createScramSha1Credential(usname, db, pword.toCharArray());
	        credentials.add(mongoCredential);
	        
	        //通过连接认证获取MongoDB连接
	        client = new MongoClient(adds, credentials);
	        clientmap.put(key, client);
	//        //连接到数据库
	//      MongoDatabase mongoDatabase = mongoClient.getDatabase(db);
	 
	        //返回连接数据库对象
    	}
        return client;
    }
}

