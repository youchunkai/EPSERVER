package com.util;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.jsoup.Connection;
import org.jsoup.Connection.Method;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
/**
 *
 * 从第三方网页、链接上抓取数据
 * @author MEI
 *
 */
public class LoadDataFromUrl {
	private static final Logger logger=Logger.getLogger(LoadDataFromUrl.class);

	/**
	 * 模拟登录，获取cookie
	 * @return
	 */
	public Map<String, String> getCookies() {
		try {
			Connection.Response res = Jsoup.connect(PropertiesConfigUtil.getConfigByKey("config.sz.logon.url")).data(
					"ReturnUrl","%2f",
					"userName",PropertiesConfigUtil.getConfigByKey("config.sz.logon.usnm")
	                ,"password",PropertiesConfigUtil.getConfigByKey("config.sz.logon.uspw"))
					.method(Method.POST).execute();
			return res.cookies();
		} catch (IOException e) {
			logger.error(e);
		}
		return null;
	}

	/**
	 * 获取指定链接的json对象
	 * @param listUrl
	 * @param args
	 * @return
	 */
	public String postJSONObj(String listUrl,String... args) {
		Connection con = Jsoup.connect(listUrl);
		try {
			Response response = con.data(args).method(Method.POST).execute();
			String result = response.body();
			return result;
		} catch (IOException e) {
			e.printStackTrace();
			logger.error(e);
		}
		return null;
	}
	
	/**
	 * 获取指定链接的json对象
	 * @param listUrl
	 * @param cookieMap
	 * @param args
	 * @return
	 */
	public String getJSONObjHasCookie(String listUrl, Map<String, String> cookieMap,String... args) {
		Connection con = Jsoup.connect(listUrl);
		// 把登录信息的cookies保存如map对象里面
		Iterator<Entry<String, String>> it = cookieMap.entrySet().iterator();
		while (it.hasNext()) {
			Entry<String, String> en = it.next();
			// 把登录的信息放入请求里面
			con = con.cookie(en.getKey(), en.getValue());
		}
		try {
			Response response = con.data(args).method(Method.POST).execute();
			String result = response.body();
			return result;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			logger.error(e);
		}
		return null;
	}
	
	 /**
      * 
      * @param requestType 请求类型
      * @param urlStr 请求地址
      * @param body 请求发送内容
      * @return 返回内容
      */
     public static byte[] requestMethod(String requestType, String urlStr, String body,HashMap<String,String> resqustpropmap) {
         // 是否有http正文提交
         boolean isDoInput = false;
         if (body != null && body.length() > 0)
             isDoInput = true;
         OutputStream outputStream = null;
         OutputStreamWriter outputStreamWriter = null;
         InputStream inputStream = null;
         InputStreamReader inputStreamReader = null;
         BufferedReader reader = null;
         StringBuffer resultBuffer = new StringBuffer();
         ByteArrayOutputStream outStream = new ByteArrayOutputStream(); 
         byte[] data = new byte[]{};
         try {
             // 统一资源
             URL url = new URL(urlStr);
             // 连接类的父类，抽象类
             URLConnection urlConnection = url.openConnection();
             // http的连接类
             HttpURLConnection httpURLConnection = (HttpURLConnection) urlConnection;
 
             if (isDoInput) {
                 httpURLConnection.setDoOutput(true);
                 httpURLConnection.setRequestProperty("Content-Length", String.valueOf(body.length()));
             }else{
            	 httpURLConnection.setRequestProperty("Content-Length", "0");
             }
             httpURLConnection.setDoInput(true);
             httpURLConnection.setConnectTimeout(50000);
             httpURLConnection.setReadTimeout(50000);
             httpURLConnection.setUseCaches(false);
             
             if(resqustpropmap != null){
            	 for (Entry<String, String> entry : resqustpropmap.entrySet()) {
             		httpURLConnection.setRequestProperty(entry.getKey(), entry.getValue());
            	 }
             }
             
             // 设定请求的方法，默认是GET
             httpURLConnection.setRequestMethod(requestType);
 
             // 打开到此 URL 引用的资源的通信链接（如果尚未建立这样的连接）。
             // 如果在已打开连接（此时 connected 字段的值为 true）的情况下调用 connect 方法，则忽略该调用。
             httpURLConnection.connect();
 
             if (isDoInput) {
                 outputStream = httpURLConnection.getOutputStream();
                 outputStreamWriter = new OutputStreamWriter(outputStream,"ISO8859-1");
                 outputStreamWriter.write(body);
                 outputStreamWriter.flush();// 刷新
             }
             if (httpURLConnection.getResponseCode() >= 300) {
                 throw new Exception(
                         "HTTP Request is not success, Response code is " + httpURLConnection.getResponseCode());
             }
 
             if (httpURLConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                 inputStream = httpURLConnection.getInputStream();
                 
                 byte[] buffer = new byte[1024]; 
                 int len = 0; 
                 while ((len = inputStream.read(buffer)) != -1) { 
                	 outStream.write(buffer, 0, len); 
                 } 
                 
                 data = outStream.toByteArray(); 
                 
//                 inputStreamReader = new InputStreamReader(inputStream,"utf-8");
//                 reader = new BufferedReader(inputStreamReader);
// 
//                 while ((tempLine = reader.readLine()) != null) {
//                     resultBuffer.append(tempLine);
//                     resultBuffer.append("\n");
//                 }
             }
 
         } catch (MalformedURLException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         } catch (IOException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         } catch (Exception e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         } finally {// 关闭流
 
             try {
                 if (outputStreamWriter != null) {
                     outputStreamWriter.close();
                 }
             } catch (Exception e) {
                 // TODO Auto-generated catch block
                 e.printStackTrace();
             }
             try {
                 if (outputStream != null) {
                     outputStream.close();
                 }
             } catch (Exception e) {
                 // TODO Auto-generated catch block
                 e.printStackTrace();
             }
             try {
                 if (reader != null) {
                     reader.close();
                 }
             } catch (Exception e) {
                 // TODO Auto-generated catch block
                 e.printStackTrace();
             }
             try {
                 if (inputStreamReader != null) {
                     inputStreamReader.close();
                 }
             } catch (Exception e) {
                 // TODO Auto-generated catch block
                 e.printStackTrace();
             }
             try {
                 if (inputStream != null) {
                     inputStream.close();
                 }
             } catch (Exception e) {
                 // TODO Auto-generated catch block
                 e.printStackTrace();
             }
             try {
                 if (outStream != null) {
                	 outStream.close();
                 }
             } catch (Exception e) {
                 // TODO Auto-generated catch block
                 e.printStackTrace();
             }
             
         }
         return data;
     }
     
}
