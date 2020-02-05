package com.util;

import com.alibaba.fastjson.JSON;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;

import java.security.Key;
import java.util.HashMap;
import java.util.Map;

/**
 * Desc:
 * Author:Kevin
 * Date:2019/6/28
 **/
public class JwtTokenUtils {
	
    private static final String COMMONKEY = "f3973b6491dfedtrd85acea1b6cbec5";

    public static HashMap<String,String> secretKeys = new HashMap<String, String>();

    static {
        secretKeys.put("610141", "f3973b64918e4324ad85acea1b6cbec5");
        secretKeys.put("610140", "f394ad85acea1b6cbec573b64918e4rt");        
    }

    /**
     * @desc 生成一个密钥 用于签名
    */
    private static Key generatorKey(){
        SignatureAlgorithm saa=SignatureAlgorithm.HS256;
        byte[] bin=DatatypeConverter.parseBase64Binary(COMMONKEY);
        Key key=new SecretKeySpec(bin,saa.getJcaName());
        return key;
    }

    /**
     * @desc 生成token
    */
    public static String generatorToken(Map<String,Object> payLoad){
        return Jwts.builder().setPayload(JSON.toJSONString(payLoad))
                    .signWith(SignatureAlgorithm.HS256,generatorKey()).compact();
    }


    /**
     * @desc 解析token
    */
    public static Claims phaseToken(String token){
        Jws<Claims> claimsJwt=Jwts.parser().setSigningKey(generatorKey()).parseClaimsJws(token);
        return claimsJwt.getBody();
    }

    /**
     * @desc 根据行政区划 生成token密码串
     */
    public static String generatorTokenByDistrict(Map<String,Object> payLoad){
        String districtCode = payLoad.get("district").toString();
        return Jwts.builder().setPayload(JSON.toJSONString(payLoad))
                .signWith(SignatureAlgorithm.HS256, generatorKeyByDistrict(districtCode)).compact();
    }

    /**
     * @desc 生成一个密钥 用于签名
     */
    private static Key generatorKeyByDistrict(String districtCode){
        String secretKey = secretKeys.get(districtCode);
        SignatureAlgorithm saa=SignatureAlgorithm.HS256;
        byte[] bin=DatatypeConverter.parseBase64Binary(secretKey);
        Key key=new SecretKeySpec(bin,saa.getJcaName());
        return key;
    }

    /**
     * @desc 解析token
     */
    public static Claims phaseTokenByDistrict(String districtCode,String token){
        Jws<Claims> claimsJwt=Jwts.parser().setSigningKey(generatorKeyByDistrict(districtCode)).parseClaimsJws(token);
        return claimsJwt.getBody();
    }
    
    /**
     * @desc 解析token
     */
    public static Claims phaseTokenByDistrictWithErrorHandler(String districtCode,String token){
    	Jws<Claims> claimsJwt = null;
    	try{
    		claimsJwt=Jwts.parser().setSigningKey(generatorKeyByDistrict(districtCode)).parseClaimsJws(token);
    	}catch (Exception e) {
			;
		}
    	if(claimsJwt != null)
    		return claimsJwt.getBody();
    	else
    		return null;
    }


//    public static void main(String[] args) {
////data=eyJhbGciOiJIUzI1NiJ9.eyJkaXN0cmljdCI6IjYxMDE0MCIsInNpZCI6IjIwNjE1RTdDODNEM0JFOTE4QTkxQ0Y2N0ZDRDk3NzlDIn0.XkI_KYAXuokPI-_hS6reJ7ELzi1NhlafGjG2ss1gmX8
//        Claims claims = JwtTokenUtils.phaseTokenByDistrictWithErrorHandler("610140", "eyJhbGciOiJIUzI1NiJ9.eyJkaXN0cmljdCI6IjYxMDE0MCIsInNpZCI6IjIwNjE1RTdDODNEM0JFOTE4QTkxQ0Y2N0ZDRDk3NzlDIn0.XkI_KYAXuokPI-_hS6reJ7ELzi1NhlafGjG2ss1gmX8");
//        String district = claims.get("sid",String.class);
//        String aaa = claims.get("district",String.class);
//        System.out.println(district + "   "+ aaa);
//    }
    
    
}
