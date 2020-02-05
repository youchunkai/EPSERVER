package com.controller;

import com.alibaba.fastjson.JSON;
import com.manage.RedisSessionManager;
import com.manage.RedisSessionManager.SessionEnum;
import com.model.resultDataObject;
import com.service.BaseService;
import com.util.*;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import sun.misc.BASE64Encoder;

import javax.annotation.Resource;
import javax.imageio.ImageIO;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping(value="/HB")
@SuppressWarnings("all")
public class BaseController {
	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	SimpleDateFormat sdf2 = new SimpleDateFormat("yyyyMMddHH24mmss");
	
	private static final Logger logger = Logger.getLogger(BaseController.class);
	
	@Resource(name = "BaseService")
	private BaseService baseService;
	private static final String OPECODE = "d4107dea-5d6f-3ebf-df6b-8cfe77425965";
	
	private static boolean isDebuger = PropertiesConfigUtil.getConfigByKey("isdebuger").equals("1");

	/***
	 * 登陆
	 * */
	@RequestMapping(value="/LOGIN",method=RequestMethod.POST)
	public @ResponseBody resultDataObject login(@RequestBody HashMap<String,Object> params,HttpServletRequest request,HttpServletResponse response){
		Date sdate = new Date();
		
		HttpSession session = request.getSession();
		String jsessionid = session.getId();
		resultDataObject result = new resultDataObject();
		
		result.setAuthinfo("XAHBSSOSID="+jsessionid);
		Object subcode = params.get("usSubCode");
		Object usname = params.get("usname");
		Object password = params.get("password");
		Object verifyCode = params.get("verifyCode");
		int errorcount = (session.getAttribute("errorcount") == null ? 0:(Integer)session.getAttribute("errorcount"));
		
		HashMap<String, Object> commonParam = null;
		if(params.get("commonParam") != null && !(params.get("commonParam").toString()).equals("")){
			commonParam = (HashMap<String, Object>)params.get("commonParam"); 
		}else{
			commonParam = new HashMap<String, Object>();
		}
		
		//登陆参数
		String urlp = request.getHeader("Referer");
		Map<String,String> param = null;
		if(urlp != null){
			param = NetAddrUtil.getURLParamMap(urlp);
		}
		//成功以后回调地址
		Object url = "";
		//1. 检查用户名密码
		HashMap<String,Object> userMap = null;
		try{
			if(usname!=null&&password!=null&&!usname.toString().equals("")&&!password.toString().equals("")){
				if(errorcount >= 3){
					if(verifyCode == null||verifyCode.toString().equals(""))
						throw new Exception("请输入验证码！");
					else if(!verifyCode.toString().equalsIgnoreCase(session.getAttribute("VerifyCode").toString())){
						throw new Exception("验证码错误！");
					}	
				}
				
				//解密
				try{
					password = AesEncryptUtil.desEncrypt(password.toString());
				}catch (Exception e) {
					password = "";
				}
				params.put("password", password);
				userMap = new HashMap<String, Object>();
				userMap = baseService.login(params);
				
				//登陆成功
				if(userMap != null){
					//登陆完成后回跳
					if(url == null || url.toString().equals("")){
						url = (param != null ? param.get("url"):"");
						if(url == null ||url.toString().equals(""))
							url = NetAddrUtil.getIP(urlp) + "/" + PropertiesConfigUtil.getConfigByKey("indexurl");
					}
					url = URLDecoder.decode(url.toString());
					
					String rootpath = NetAddrUtil.getRootPath(url.toString());
					result.setResult(true);
					result.setRedirect(NetAddrUtil.getIP(url.toString()) + (rootpath.startsWith("/")?"":"/") + rootpath + "/Auth.html?COOKIENAME=XAHBSSOSID&COOKIEVALUE="+jsessionid+"&url="+url.toString());
					result.setData(userMap);
					session.setAttribute("user", result.getData());
					
					session.removeAttribute("errorcount");
					session.removeAttribute("VerifyCode");

				}else{
					throw new Exception("账号或密码错误！");
				}
			}else{
				throw new Exception("登陆信息不完整，请完善登陆信息!");
			}
		}catch (Exception e) {
			session.setAttribute("errorcount", errorcount + 1);
			result.setResult(false);
			result.setError(e.getMessage());
			
			e.printStackTrace();
			
			//生成验证码
			if(errorcount >= 2 && !result.isResult()){
				int width=200;
	            int height=69;
	            BufferedImage verifyImg=new BufferedImage(width,height,BufferedImage.TYPE_INT_RGB);
	            String randomText = VerifyCode.drawRandomText(width,height,verifyImg);
	            session.setAttribute("VerifyCode", randomText);
	            
	            try{
		            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		            ImageIO.write(verifyImg, "jpg", outputStream);
		            BASE64Encoder encoder = new BASE64Encoder();
		            String png_base64 =  encoder.encodeBuffer(outputStream.toByteArray()).trim();
		            png_base64 = png_base64.replaceAll("\n", "").replaceAll("\r", "");
		            String oo = "data:image/jpg;base64," + png_base64;
		            outputStream.close();
		            
		            result.setData(oo);
	            }catch (Exception e1) {
	    			result.setData("生成验证码市发生错误！");
				}
			}
		}
		
		try{
			HashMap<String, Object> map = new HashMap<String, Object>();
			map.put("LG_SESSION", jsessionid == null ? "" : jsessionid);
			map.put("SYS_CODE", ConvertUtil.getMapValue(param,"sysCode"));
			map.put("FUNC_CODE", ConvertUtil.getMapValue(param,"funcCode"));
			map.put("OPE_CODE", ConvertUtil.getMapValue(param,"opeCode"));
			map.put("US_CODE", ConvertUtil.getMapValue(userMap,"US_CODE"));
			map.put("LG_STM", sdate);
			map.put("LG_COST", new Date().getTime()  - sdate.getTime());
			map.put("LG_TM", new Date());
			map.put("LG_ERR", result.getError());
			map.put("LG_CLIENTTYPE", ConvertUtil.getMapValue(commonParam,"clientInfo"));
			String rip = getRemortIP(request) ;
			map.put("LG_CLIENTIP", rip);
			String rht = getRemortHost(request) ;
			map.put("DSLG_CLIENTNAME", rht);
			map.put("LG_CLIENTPORT", "");
			map.put("LG_ISSUCESS", result.isResult());
			map.put("LG_SQLCLOB", "");
			map.put("LG_SQLPARAM", JSON.toJSONString(params));
			map.put("LG_TYPE", "登录日志");
			map.put("LG_DESC", usname+(userMap == null?"":"["+userMap.get("US_NAME")+"]")+"登陆系统"+(result.isResult()?"成功":"失败")+",系统地址"+url);//描述信息
			map.put("LG_REMARKS", "接口服务");
			map.put("LG_URL", urlp);
			map.put("LG_SVRCODE", "/LOGIN");
			
			map.put("LG_SYSVERSION", ConvertUtil.getMapValue(commonParam,"sysVersion"));
			map.put("LG_CLIENTINFO", request.getHeader("User-Agent"));
			baseService.addLog(map);
		}catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}

		return result;
	}
	
	
	/***
	 * checkLogin
	 * */
	@RequestMapping(value="/CHECKLOGIN",method=RequestMethod.POST)
	public @ResponseBody resultDataObject checklogin(HttpServletRequest request,HttpServletResponse response){
		resultDataObject result = new resultDataObject();
		
		ServletRequestAttributes attrs =(ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
		HttpServletRequest resuest = attrs.getRequest();
		HttpSession session = resuest.getSession();
		String jsessionid = session.getId();

		HashMap<String,Object> user = null;
		if(session != null){
			user = (HashMap<String,Object>)session.getAttribute("user");
		}
		Cookie NewCookie=new Cookie("XAHBSSOSID",null);
		NewCookie.setMaxAge(0);
		response.addCookie(NewCookie);
		
		result.setResult(true);
		if(user != null && !user.toString().equals("") && user.get("US_CODE") != null){
			String referurl = request.getHeader("Referer") == null?"":request.getHeader("Referer");
			
			Map<String, String> ma = NetAddrUtil.getURLParamMap(referurl);
			String callbackurl = "";
			if(ma != null)
				callbackurl = URLDecoder.decode(ma.get("url"));
			
			if(callbackurl == null || callbackurl.equals("")){
				callbackurl = NetAddrUtil.getIP(referurl) + "/" + PropertiesConfigUtil.getConfigByKey("indexurl");
			}
			
			String rootpath = NetAddrUtil.getRootPath(callbackurl);
			result.setRedirect(NetAddrUtil.getIP(callbackurl) + (rootpath.startsWith("/")?"":"/") + rootpath + "/Auth.html?COOKIENAME=XAHBSSOSID&COOKIEVALUE="+jsessionid+"&url="+callbackurl);
			result.setData(true);
		}else{
			result.setData(false);
		}

		return result;
	}
	/**
	 * 验证码
	 **/
	@RequestMapping(value="/VERIFYCODE",method=RequestMethod.POST)
	public 	@ResponseBody resultDataObject verifyCode(HttpServletRequest request,HttpServletResponse response){
		ServletRequestAttributes attrs =(ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
		HttpServletRequest resuest = attrs.getRequest();
		HttpSession session = resuest.getSession();
		
		int errorcount = (session.getAttribute("errorcount") == null ? 0:(Integer)session.getAttribute("errorcount"));
		
		resultDataObject result = new resultDataObject();
		//生成验证码
		if(errorcount >= 3){
			int width=200;
            int height=69;
            BufferedImage verifyImg=new BufferedImage(width,height,BufferedImage.TYPE_INT_RGB);
            String randomText = VerifyCode.drawRandomText(width,height,verifyImg);
            session.setAttribute("VerifyCode", randomText);
            
            try{
	            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
	            ImageIO.write(verifyImg, "jpg", outputStream);
	            BASE64Encoder encoder = new BASE64Encoder();
	            String png_base64 =  encoder.encodeBuffer(outputStream.toByteArray()).trim();
	            png_base64 = png_base64.replaceAll("\n", "").replaceAll("\r", "");
	            String oo = "data:image/jpg;base64," + png_base64;
	            outputStream.close();
	            
	            result.setData(oo);
            }catch (Exception e1) {
    			result.setData("生成验证码市发生错误！");
			}
		}
		
		return result;
	}
	/***
	 * 登出
	 * */
	@RequestMapping(value="/LOGOUT",method=RequestMethod.POST)
	public 	@ResponseBody resultDataObject logout(HttpServletRequest request,HttpServletResponse response){
		Date sdate = new Date();
		
		ServletRequestAttributes attrs =(ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
		HttpServletRequest resuest = attrs.getRequest();
		HttpSession session = resuest.getSession();
		String jsessionid = session.getId();

		HashMap<String,Object> user = null;
		if(session != null){
			user = (HashMap<String,Object>)session.getAttribute("user");
			session.setAttribute("user", null);
			session.invalidate();
		}
		Cookie NewCookie=new Cookie("XAHBSSOSID",null);
		NewCookie.setMaxAge(0);
		response.addCookie(NewCookie);
		
		resultDataObject result = new resultDataObject();
		result.setResult(true);
		
		String callbackurl = URLEncoder.encode(request.getHeader("Referer") == null?"":request.getHeader("Referer"));
		result.setRedirect(NetAddrUtil.getIP(URLDecoder.decode(callbackurl)) + "/" + PropertiesConfigUtil.getConfigByKey("loginurl")+"?url="+callbackurl);
				
		String urlp = request.getHeader("Referer");
		Map<String,String> param = null;
		if(urlp != null){
			param = NetAddrUtil.getURLParamMap(urlp);
		}
		
		try{
			HashMap<String, Object> map = new HashMap<String, Object>();
			map.put("LG_SESSION", jsessionid == null ? "" : jsessionid);
			map.put("SYS_CODE", ConvertUtil.getMapValue(param,"sysCode"));
			map.put("FUNC_CODE", ConvertUtil.getMapValue(param,"funcCode"));
			map.put("OPE_CODE", ConvertUtil.getMapValue(param,"opeCode"));
			map.put("US_CODE", ConvertUtil.getMapValue(user,"US_CODE"));
			map.put("LG_STM", sdate);
			map.put("LG_COST", new Date().getTime() - sdate.getTime());
			map.put("LG_TM", new Date());
			map.put("LG_ERR", result.getError());
			map.put("LG_CLIENTTYPE", "");
			String rip = getRemortIP(request) ;
			map.put("LG_CLIENTIP", rip);
			String rht = getRemortHost(request) ;
			map.put("DSLG_CLIENTNAME", rht);
			map.put("LG_CLIENTPORT", "");
			map.put("LG_ISSUCESS", result.isResult());
			map.put("LG_SQLCLOB", "");
			map.put("LG_SQLPARAM", "");
			map.put("LG_TYPE", "登出日志");
			map.put("LG_DESC", jsessionid+(user == null?"":"["+user.get("US_NAME")+"]")+"登出系统,系统地址"+(urlp==null?"":urlp.toString()));//描述信息
			map.put("LG_REMARKS", "接口服务");
			map.put("LG_URL", urlp);
			map.put("LG_SVRCODE", "/LOGOUT");
			
			map.put("LG_SYSVERSION", "");
			map.put("LG_CLIENTINFO", request.getHeader("User-Agent"));
			baseService.addLog(map);
		}catch (Exception e) {
			e.printStackTrace();
		}
		
		return result;
	}
	/***
	 * 提供通用服务
	 * */
	@RequestMapping(value="/SERVICES",method=RequestMethod.POST)
	public 	@ResponseBody resultDataObject test(@RequestBody Map<String,Object> params,HttpServletRequest request,HttpServletResponse response){
		Date d1 = new Date();
		Long c1 = System.currentTimeMillis();//创建计时起点
		
		HttpSession session = request.getSession();//获取session
		String jsessionid = session.getId();//获取sessionId
		
		resultDataObject result = new resultDataObject();//创建返回对象
		String user = "";//声明用户赋值为""
		String ip = "";//声明ip赋值为""
		String clientname = "";//声明客户端名赋值为""
		String url = "";//声明路径赋值为""
		int port = 0;//声明端口号赋值为0
		
		if(params.get("id")==null){//判断获取当前服务ip是否为空
			result.setError("未声明调用服务id");//如果为空设置异常信息
			result.setResult(false);//设置返回结果为false
			return result;//返回当前对象
		}
		
		Integer id = Integer.parseInt(params.get("id").toString());//获取当前服务ID
		HashMap<String, Object> sqlParam = null;//声明存储SQL语句集合参数
		HashMap<String, Object> commonParam = null;//声明通用集合参数
		// 判断请求sql参数是否为空与""
		if(params.get("sqlParam") != null && !(params.get("sqlParam").toString()).equals("")){
			// 如不为空获取获取sql参数值赋值给sqlParam
			sqlParam = (HashMap<String, Object>)params.get("sqlParam"); 
			if(sqlParam == null) //验证sqlParam是否为空
				sqlParam = new HashMap<String, Object>();//如果为空创建存储SQL语句集合
		}
		// 判断通用参数是否为空
		if(params.get("commonParam") != null && !(params.get("commonParam").toString()).equals("")){
			commonParam = (HashMap<String, Object>)params.get("commonParam"); //获取请求参数值
		}else{
			commonParam = new HashMap<String, Object>();// 否则创建集合用于存储通用参数
		}
		
		try{
			ip = getRemortIP(request);// 调用封装的获取客户端真实ip方法，获取ip地址并赋值给ip
			clientname = getRemortHost(request);// 调用封装的获取客户端名方法，获取客户端名并赋值给clientname
			port = request.getRemotePort();//获取客户端端口号
			url = request.getHeader("Referer");//获取url全路径
			
			commonParam.put("DS_CODE",id);//设置通用参数：服务ID
			commonParam.put("user", session.getAttribute("user"));//设置用户
			
			//通过commonParam传参 再强制验证 是否修改权限 解决需要登录的问题
			if(null != commonParam.get("isauth")){
        		HashMap<String, Object> u = new HashMap<String,Object>();
				u.put("US_LNNM","admin");
				u.put("US_CODE","1");
				commonParam.put("user",u);
			}
			
			commonParam.put("ip", ip);//设置ip
			commonParam.put("clientname", clientname);//设置客户端名
			commonParam.put("port", port);//设置端口号
			commonParam.put("begintime", d1);//设置计时开始时间
			commonParam.put("referer", url);//设置请求全路径
			commonParam.put("jsessionid", jsessionid);//设置sessionId
		}catch (Exception e) {
			e.printStackTrace();
		}
		// 调用业务层执行服务数据项目方法（参数1：服务id,参数2：sql语句参数，参数3：通用参数）
		// 获取返回结果对象
		result = baseService.excuteDataServerItem(id, sqlParam, commonParam);
		//处理跳转，拼接参数
		if(result.getRedirect()!=null){//判断是否需要跳转，不为空，需要跳转，进入判断处理跳转
			if(request.getHeader("Referer")!=null&&!request.getHeader("Referer").equals("")){
				String currenturl = request.getHeader("Referer");//获取请求路径url
				String loginurl = "";//声明登录路径并赋值为：""
				if(currenturl.indexOf("10.16.146.") != -1)//判断请求路径是否包含"10.16.146."
					//调用网络地址工具类中获取IP方法获取IP地址+/+调用配置文件工具类获取配置文件中key为loginurl的值
					//将拼接的路径赋值给loginurl
					loginurl = NetAddrUtil.getIP(currenturl) + "/" + PropertiesConfigUtil.getConfigByKey("loginurl");
				else{
					// 调用配置文件工具类获取配置文件中key为publicurl+/+loginurl的值
					// 将拼接的登录路径赋值给loginurl
					loginurl = PropertiesConfigUtil.getConfigByKey("publicurl") + "/" + PropertiesConfigUtil.getConfigByKey("loginurl");
				}
				// 路径拼接并保证编码格式一致
				String rurl = URLEncoder.encode(loginurl+"?url="+URLEncoder.encode(currenturl));
				// 判断通用参数中sysCode是否为空
				if(commonParam.get("sysCode") != null && !commonParam.get("sysCode").toString().equals(""))
					rurl += "&sysCode="+commonParam.get("sysCode").toString();//将当前系统码拼接到rurl路径后
				// 判断当前服务码是否为空看那个
				if(commonParam.get("funcCode") != null && !commonParam.get("funcCode").toString().equals(""))
					rurl += "&funcCode="+commonParam.get("funcCode").toString();//将当前服务码拼接到rurl路径后
				
				String rootpath = NetAddrUtil.getRootPath(loginurl.toString());
				String uurl = NetAddrUtil.getIP(loginurl.toString()) + (rootpath.startsWith("/")?"":"/") + rootpath + "/Auth.html?COOKIENAME=XAHBSSOID&COOKIEVALUE="+jsessionid+"&url="+rurl;

				result.setRedirect(uurl);
			}
		}
		
		Date d2 = new Date();
		Long c2 = System.currentTimeMillis();//设置结束时间
		commonParam.put("endtime", d2);//存入通用参数中
		//记录日志
		boolean islog = baseService.excuteDataServerLogItem(sqlParam, commonParam, result);
		//判断是否为调试模式：1调试模式  0生产模式
		if(!isDebuger){
			result.setServiceinfo(null);
			result.setSqlparam(null);
			result.setSql(null);
		}
		System.out.println(jsessionid+ "调用服务："+ params.get("id") + ", 耗时："+(c2 - c1)+"ms" + (result.isCache()?"【缓存】":"")) ;
		return result;
	}

	/**
	 * 获取真实IP方法
	 * @param request
	 * @return
	 */
	public String getRemortIP(HttpServletRequest request) {
		if (request.getHeader("X-Real-IP") == null) { 
			return request.getRemoteAddr();  
		}   
		return request.getHeader("X-Real-IP");
	}
	
	public String getRemortHost(HttpServletRequest request) {  
		if (request.getHeader("Host") == null) { 
			return request.getRemoteHost();  
		}   
		return request.getHeader("Host");
	}
	
	@RequestMapping(value="/getOnlineNumInf",method=RequestMethod.GET)
	public @ResponseBody HashMap<String, Object> getOnlineNumInf(HttpServletRequest request,HttpServletResponse response){		
		return baseService.getOnlineNumInf();
	}
	
	@RequestMapping(value="/TEST",method=RequestMethod.GET)
	public @ResponseBody HashMap<String, Object> test2(HttpServletRequest request,HttpServletResponse response){		
		return RedisSessionManager.getSessionMap(SessionEnum.USCODEMAIN);
	}
}
