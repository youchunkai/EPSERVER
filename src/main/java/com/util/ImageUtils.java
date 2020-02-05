package com.util;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
 
import javax.imageio.ImageIO;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;
@SuppressWarnings("all")
public class ImageUtils {
	/**
	* 将网络图片进行Base64位编码
	* 
	* @param imageUrl
	*            图片的url路径，如http://.....xx.jpg
	* @return
	*/
	public static String encodeImgageToBase64(URL imageUrl) {// 将图片文件转化为字节数组字符串，并对其进行Base64编码处理
		ByteArrayOutputStream outputStream = null;
		try {
			BufferedImage bufferedImage = ImageIO.read(imageUrl);
			outputStream = new ByteArrayOutputStream();
			ImageIO.write(bufferedImage, "jpg", outputStream);
		} catch (MalformedURLException e1) {
			e1.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		// 对字节数组Base64编码
		BASE64Encoder encoder = new BASE64Encoder();
		return encoder.encode(outputStream.toByteArray());// 返回Base64编码过的字节数组字符串
	}
	 
	/**
	* 将本地图片进行Base64位编码
	* 
	* @param imageFile
	*            图片的url路径，如http://.....xx.jpg
	* @return
	*/
	public static String encodeImgageToBase64(File imageFile) {// 将图片文件转化为字节数组字符串，并对其进行Base64编码处理
		ByteArrayOutputStream outputStream = null;
		try {
			BufferedImage bufferedImage = ImageIO.read(imageFile);
			outputStream = new ByteArrayOutputStream();
			ImageIO.write(bufferedImage, "jpg", outputStream);
		} catch (MalformedURLException e1) {
				e1.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		// 对字节数组Base64编码
		BASE64Encoder encoder = new BASE64Encoder();
		return encoder.encode(outputStream.toByteArray());// 返回Base64编码过的字节数组字符串
	}
	
	/**
	    * <p>将文件转成base64 字符串</p>
	    * @param path 文件路径
	    * @return
	    * @throws Exception
	    */
	public static String encodeBase64File(String path) throws Exception {
	        File file = new File(path);
	        return encodeBase64File(file);
	}
	
	/**
	    * <p>将文件转成base64 字符串</p>
	    * @param file 文件路径
	    * @return
	    * @throws Exception
	    */
	public static String encodeBase64File(File file) throws Exception {
		FileInputStream inputFile = new FileInputStream(file);
        byte[] buffer = new byte[(int)file.length()];
        inputFile.read(buffer);
        inputFile.close();
        return new BASE64Encoder().encode(buffer);
	}
	 
	/**
	     * 将Base64位编码的图片进行解码，并保存到指定目录
	     * 
	     * @param base64
	     *            base64编码的图片信息
	     * @return
	     */
	public static File decodeBase64ToFile(String base64, String path,String imgName) {
		BASE64Decoder decoder = new BASE64Decoder();
		File uploadResourceFile = new File(path +"\\"+imgName);
		try {
			FileOutputStream write = new FileOutputStream(uploadResourceFile);
			byte[] decoderBytes = decoder.decodeBuffer(base64);
			write.write(decoderBytes);
			write.close();
			return uploadResourceFile;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
}
