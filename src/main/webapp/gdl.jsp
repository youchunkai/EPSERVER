<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ page import="java.net.*, java.util.*,java.io.*, org.json.*,com.util.PropertiesConfigUtil"%>

<%
	Map user = (Map)session.getAttribute("user");  
	if(user != null && !user.toString().equals("") && user.get("US_CODE") != null){
		;
	}else{
		String site = new String((request.getServerName().indexOf("10.16.146") == -1?"http://117.39.29.99:6081":"http://10.16.16.116:8081")+PropertiesConfigUtil.getConfigByKey("loginurl"));
		response.setStatus(response.SC_MOVED_TEMPORARILY);
		response.setHeader("Location", site); 
	}
	System.out.println(PropertiesConfigUtil.getConfigByKey("loginurl"));

	// 跨域用代理
	request.setCharacterEncoding("UTF-8");
	String url = null;
	StringBuffer params = new StringBuffer();
	
	Enumeration enu = request.getParameterNames();
	int total = 0;
	while (enu.hasMoreElements()) {
		String paramName = (String) enu.nextElement();
		if (paramName.equals("url")) {
			url = request.getParameter(paramName);
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
	
	if (url != null) {
		try {
			URL connect = new URL("http://10.16.146.97:9001/ptcPerson/signIn");
			HttpURLConnection connection = (HttpURLConnection) connect.openConnection();
			connection.setConnectTimeout(3000);
			connection.setReadTimeout(3000);
			connection.setDoOutput(true);
			connection.setDoInput(true);
			connection.setUseCaches(false);
			connection.setRequestMethod("POST");
			connection.setRequestProperty("Content-type","application/json;charset=utf-8");
			connection.setRequestMethod(request.getMethod());
			
			OutputStream os = connection.getOutputStream();
			OutputStreamWriter osw = new OutputStreamWriter(os, "UTF-8");    
			osw.write("{\"id\":\""+user.get("US_CODE")+"\",\"username\":\""+user.get("US_NAME")+"\"}");
			//osw.write("{\"id\":\""+"test"+"\",\"username\":\""+"test"+"\"}");
			osw.flush();
			osw.close();
			os.close();  //don't forget to close the OutputStream
			connection.connect();

			BufferedReader rd = new BufferedReader(new InputStreamReader(connection.getInputStream(),"utf-8"));
			ByteArrayOutputStream buf = new ByteArrayOutputStream();
			
			int result2 = rd.read();
			while(result2 != -1) {
			    buf.write((byte) result2);
			    result2 = rd.read();
			}
			String line = buf.toString("utf-8");
			rd.close();
			
			JSONObject returnObj = new JSONObject(line);
			
			if(returnObj != null && returnObj.getString("token") != null){
				url = url + "?sid=" + returnObj.getString("token");
				//System.out.println("打开高德系统："+url);
				String site = new String(url);
			    response.setStatus(response.SC_MOVED_TEMPORARILY);
			    response.setHeader("Location", site);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
%>