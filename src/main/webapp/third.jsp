<%@page import="javax.servlet.http.Cookie"%>
<%@page import="org.apache.commons.lang.StringUtils"%>
<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ page import="java.net.*, java.util.*, org.json.*,com.util.JwtTokenUtils, io.jsonwebtoken.*,com.util.NetAddrUtil"%>

<%
	request.setCharacterEncoding("UTF-8");
	String url = null;
	String token = null;
	String district = null;
	StringBuffer params = new StringBuffer();
	
	Enumeration enu = request.getParameterNames();
	int total = 0;
	while (enu.hasMoreElements()) {
		String paramName = (String) enu.nextElement();
		if (paramName.equals("url")) {
			url = request.getParameter(paramName);
		}else if(paramName.equals("xahbToken")){
			token = request.getParameter(paramName);
		}else if(paramName.equals("district")){
			district = request.getParameter(paramName);
		} else {
			if (total == 0) {
				params.append(paramName).append("=").append(URLEncoder.encode(
								request.getParameter(paramName),"UTF-8"));
			} else {
				params.append("&").append(paramName).append("=").append(URLEncoder.encode(
								request.getParameter(paramName),"UTF-8"));
			}
			++total;
		}
	}
	String param = params.toString();
	
	if(StringUtils.isEmpty(url)||StringUtils.isEmpty(token)||StringUtils.isEmpty(district)){
//		out.print("缺少必要参数！请检查");
	}else{
		//获取url hash
		String urlhash = "";
		if(url.indexOf("#") != -1){
			urlhash = url.substring(url.indexOf("#"));
			url = url.split("#")[0];
		}
		
		String sid = "";
		String districtcode = "";

		Claims claims = JwtTokenUtils.phaseTokenByDistrictWithErrorHandler(district, token);
		if(claims == null){
//			out.print("无效的令牌；");
		}else{
			sid = claims.get("sid",String.class);
			districtcode = claims.get("district",String.class);
			
			if(StringUtils.isEmpty(sid)){
//				out.print("无效的认证信息！");
			}else{
				if(!districtcode.equals(district)){
//					out.print("无效的认证信息！");
				}else{
					Cookie cookie1 = new Cookie("XAHBSSOSID",sid);
					cookie1.setPath("/");
					cookie1.setMaxAge(60*60);
					response.addCookie(cookie1);
					
					String rootpath = NetAddrUtil.getRootPath(url.toString());
					String redirecturl = URLEncoder.encode(url+(url.indexOf("?") == -1?"?":"&")+param + urlhash);
					String path = NetAddrUtil.getIP(url.toString()) + (rootpath.startsWith("/")?"":"/") + rootpath + "/Auth.html?COOKIENAME=XAHBSSOSID&COOKIEVALUE="+sid+"&url="+redirecturl.toString();
					
					//out.print(path);
					String site = new String(path);
				    response.setStatus(response.SC_MOVED_TEMPORARILY);
				    response.setHeader("Location", site);
				}
			}
		}
	}
%>