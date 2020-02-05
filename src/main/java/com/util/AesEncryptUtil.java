package com.util;

/**
 * AES 128bit 加密解密工具类
 *
 */
 
import org.apache.commons.codec.binary.Base64;
 
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
 
 
public class AesEncryptUtil {
    //使用AES-128-CBC加密模式，key需要为16位,key和iv可以相同！
    private static String KEY = PropertiesConfigUtil.getConfigByKey("Securitykey");
    private static String IV = PropertiesConfigUtil.getConfigByKey("Securityiv");
 
    /**
     * 加密方法
     * @param data  要加密的数据
     * @param key 加密key
     * @param iv 加密iv
     * @return 加密的结果
     * @throws Exception
     */
    public static String encrypt(String data, String key, String iv) throws Exception {
        try {
 
            Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");//"算法/模式/补码方式"
            int blockSize = cipher.getBlockSize();
 
            byte[] dataBytes = data.getBytes();
            int plaintextLength = dataBytes.length;
            if (plaintextLength % blockSize != 0) {
                plaintextLength = plaintextLength + (blockSize - (plaintextLength % blockSize));
            }
 
            byte[] plaintext = new byte[plaintextLength];
            System.arraycopy(dataBytes, 0, plaintext, 0, dataBytes.length);
 
            SecretKeySpec keyspec = new SecretKeySpec(key.getBytes(), "AES");
            IvParameterSpec ivspec = new IvParameterSpec(iv.getBytes());
 
            cipher.init(Cipher.ENCRYPT_MODE, keyspec, ivspec);
            byte[] encrypted = cipher.doFinal(plaintext);
            //System.out.println("加密后："+byteToHexString(encrypted));  
            return new Base64().encodeToString(encrypted);
 
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
 
    /**
     * 解密方法
     * @param data 要解密的数据
     * @param key  解密key
     * @param iv 解密iv
     * @return 解密的结果
     * @throws Exception
     */
    public static String desEncrypt(String data, String key, String iv) throws Exception {
        try {
            byte[] encrypted1 = new Base64().decode(data);
 
            Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
            SecretKeySpec keyspec = new SecretKeySpec(key.getBytes(), "AES");
            IvParameterSpec ivspec = new IvParameterSpec(iv.getBytes());
 
            cipher.init(Cipher.DECRYPT_MODE, keyspec, ivspec);
 
            byte[] original = cipher.doFinal(encrypted1);
            String originalString = new String(original);
            return originalString.trim();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
 
    /**
     * 使用默认的key和iv加密
     * @param data
     * @return
     * @throws Exception
     */
    public static String encrypt(String data) throws Exception {
        return encrypt(data, KEY, IV);
    }
 
    /**
     * 使用默认的key和iv解密
     * @param data
     * @return
     * @throws Exception
     */
    public static String desEncrypt(String data) throws Exception {
        return desEncrypt(data, KEY, IV);
    }
 
    public static String byteToHexString(byte[] bytes) {  
        StringBuffer sb = new StringBuffer(bytes.length);  
        String sTemp;  
        for (int i = 0; i < bytes.length; i++) {  
            sTemp = Integer.toHexString(0xFF & bytes[i]);  
            if (sTemp.length() < 2)  
                sb.append(0);  
            sb.append(sTemp.toUpperCase());  
        }  
        return sb.toString();  
    }  
 
//    /**
//    * 测试
//    */
//    public static void main(String args[]) throws Exception {
//        String enmi="9m4tJ1NZw4royjKED+/BRw==";
//        System.out.println("解密串："+enmi);
//        System.out.println("解密后"+desEncrypt(enmi));
//    }
 
}