package com.dao;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.bson.Document;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.util.MongoDBUtil;
import com.util.PropertiesConfigUtil;

public class MongoDao {
	private static String ip = PropertiesConfigUtil.getConfigByKey("AppendMongoDBIP");
	private static int port = Integer.parseInt(PropertiesConfigUtil.getConfigByKey("AppendMongoDBPORT"));
	private static String usname = PropertiesConfigUtil.getConfigByKey("AppendMongoLogUser");
	private static String pword = PropertiesConfigUtil.getConfigByKey("AppendMongoLogPw");
	private static SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	public static List<Document> SaveLogData(ArrayList<HashMap<String, Object>> list){
		List<Document> resultlist = new ArrayList<Document>();
		
		MongoClient mongoc = null;
		try{
			mongoc = MongoDBUtil.getConnect(ip, port, "log", usname, pword);
			MongoDatabase mdb = mongoc.getDatabase("log");
	        MongoCollection mongoCollection = mdb.getCollection("ServiceLog");
			if(mongoCollection == null){
				mdb.createCollection("ServiceLog");
				mongoCollection = mdb.getCollection("ServiceLog");
			}
			
			List<Document> doclist = new ArrayList<Document>();
			for(int i = 0;i < list.size();i++){
				Document document = new Document();
				HashMap<String, Object> datamap = list.get(i);
				Iterator iter = datamap.entrySet().iterator();
				while (iter.hasNext()) {
					Map.Entry entry = (Map.Entry) iter.next();
					String key = entry.getKey().toString();
					Object value = (Object)entry.getValue();
					
					if(value instanceof Float){
						document.put(key, Double.valueOf(value.toString()));
					}else{
						document.put(key, value);
					}
				}
				doclist.add(document);
			}

			mongoCollection.insertMany(doclist);

		}catch (Exception e) {
			e.printStackTrace();
		}
		
		return resultlist;
	}

}
