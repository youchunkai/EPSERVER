package com.util;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NetAddrUtil {
	public static String getIP(String url) {
		if(url == null)
			return "";
        //使用正则表达式过滤，
        String re = "((http|ftp|https)://)(([a-zA-Z0-9._-]+)|([0-9]{1,3}.[0-9]{1,3}.[0-9]{1,3}.[0-9]{1,3}))(([a-zA-Z]{2,6})|(:[0-9]{1,5})?)";
        String str = "";
        // 编译正则表达式
        Pattern pattern = Pattern.compile(re);
        // 忽略大小写的写法
        // Pattern pat = Pattern.compile(regEx, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(url);
        //若url==http://127.0.0.1:9040或www.baidu.com的，正则表达式表示匹配
        if (matcher.matches()) {
            str = url;
        } else {
            String[] split2 = url.split(re);
            if (split2.length > 1) {
                String substring = url.substring(0, url.length() - split2[1].length());
                str = substring;
            } else {
                str = split2[0];
            }
        }
        return str;
    }
	
	public static String getRootPath(String url) {
        String str = "";
        
        String host = getIP(url);
        url = url.replace(host, "");
        int endIndex = url.indexOf("/",2);
        if(endIndex == -1)
        	str = "";
        else{
        	str = url.substring(0, endIndex);
        }
        
        return str;
    }
	
	/**
     * 解析出url参数中的键值对
     * 如 "index.jsp?Action=del&id=123"，解析出Action:del,id:123存入map中
     * @param URL  url地址
     * @return  url请求参数部分
     */
    public static Map<String, String> getURLParamMap(String URL)
    {
	    Map<String, String> mapRequest = new HashMap<String, String>();
	    String[] arrSplit=null;
	    String strUrlParam=getTruncateUrlPage(URL);
	    if(strUrlParam==null){
	        return mapRequest;
	    }
	    //每个键值为一组
	    arrSplit=strUrlParam.split("[&]");
	    for(String strSplit:arrSplit){
	          String[] arrSplitEqual=null;          
	          arrSplitEqual= strSplit.split("[=]"); 
	          //解析出键值
	          if(arrSplitEqual.length>1){
	              mapRequest.put(arrSplitEqual[0], arrSplitEqual[1]);
	              
	          }
	          else{
	              if(arrSplitEqual[0]!=""){
	            	  mapRequest.put(arrSplitEqual[0], "");        
	              }
	          }
	    }    
	    return mapRequest;    
    }
    
    /**
     * 去掉url中的路径，留下请求参数部分
     * @param strURL url地址
     * @return url请求参数部分
     */
    private static String getTruncateUrlPage(String strURL)
    {
	    String strAllParam=null;
	      String[] arrSplit=null;
	      strURL=strURL.trim();
	      arrSplit=strURL.split("[?]");
	      if(strURL.length()>1){
	          if(arrSplit.length>1){
	                  if(arrSplit[1]!=null){
	                	  strAllParam=arrSplit[1];
	                  }
	          }
	      }
	    return strAllParam;    
    }
}

