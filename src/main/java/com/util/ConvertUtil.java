package com.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.Map;

import oracle.sql.CLOB;

public class ConvertUtil {
	public static String ClobToString(CLOB clob) throws SQLException, IOException { 
		if(clob ==null)
			return null;
		
        String reString = ""; 
        Reader is = clob.getCharacterStream();// 得到流 
        BufferedReader br = new BufferedReader(is); 
        String s = br.readLine(); 
        StringBuffer sb = new StringBuffer(); 
        while (s != null) {// 执行循环将字符串全部取出付值给StringBuffer由StringBuffer转成STRING 
            sb.append(s + "\n"); 
            s = br.readLine(); 
        } 
        reString = sb.toString(); 
        return reString; 
    }
	
	public static String stringToMD5(String plainText) {
        byte[] secretBytes = null;
        try {
            secretBytes = MessageDigest.getInstance("md5").digest(
                    plainText.getBytes());
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("没有这个md5算法！");
        }
        String md5code = new BigInteger(1, secretBytes).toString(16);
        for (int i = 0; i < 32 - md5code.length(); i++) {
            md5code = "0" + md5code;
        }
        return md5code;
    }
	
	public static Object getMapValue(Map obj,String key){
		if(obj == null){
			return null;
		}else{
			return obj.get(key);
		}
	}
}
