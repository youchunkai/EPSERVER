package com.util;

import java.util.Locale;
import java.util.ResourceBundle;

public class PropertiesConfigUtil {

	/**
	 * 根据输入的value值读取配置文件的内容
	 * @param key
	 */
	public static String getConfigByKey(String key){
		ResourceBundle rs = getResourceBundle();// 获取本地化配置
		String result=rs.getString(key);
		return result;
	}
	
	/**
	 * 获取ResourceBundle对象
	 * @return
	 */
	public static ResourceBundle getResourceBundle(){
		Locale currentLocale = Locale.getDefault();//根据 Locale加载资源包
		return ResourceBundle.getBundle("config/config", currentLocale);
	}
}
