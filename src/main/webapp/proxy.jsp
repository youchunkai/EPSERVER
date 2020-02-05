<%@ page language="java" contentType="text/html; charset=utf-8"
	pageEncoding="utf-8"%>
<%@ page import="java.net.*, java.util.*, java.io.*, org.json.*"%>
<%
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
				params.append(paramName)
						.append("=")
						.append(URLEncoder.encode(
								request.getParameter(paramName),
								"UTF-8"));
			} else {
				params.append("&")
						.append(paramName)
						.append("=")
						.append(URLEncoder.encode(
								request.getParameter(paramName),
								"UTF-8"));
			}
			++total;
		}
	}
	String param = params.toString();
	if (url != null) {
		try {
			String u = url;
			if(!param.equals("")){
				u = url + "?" + param;
			}
			URL connect = new URL(u);
			HttpURLConnection connection = (HttpURLConnection) connect.openConnection();
			connection.setConnectTimeout(3000);
			connection.setReadTimeout(3000);
			connection.setDoOutput(true);
			connection.setDoInput(true);
			connection.setUseCaches(false);
			connection.setRequestMethod("POST");
			connection.setRequestProperty("Content-type","application/x-www-form-urlencoded;charset=utf-8");
			connection.setRequestMethod(request.getMethod());
			//connection.connect();
			int clength = request.getContentLength();
			if (clength > 0) {
				byte[] idata = new byte[clength];
				request.getInputStream().read(idata, 0, clength);
				String s = new String(idata);
				
				JSONObject jjj = new JSONObject(s);
				Iterator iterator = jjj.keys();
				String instr = "";
				while(iterator.hasNext()){
				      String key = (String) iterator.next();
				      String value = jjj.getString(key);
				      instr = instr + key + "=" + value + "&";
				}
				instr = instr.substring(0, instr.length()-1);
				
				byte[] ss = instr.getBytes();
				connection.getOutputStream().write(ss, 0, ss.length);
			}
			response.setContentType(connection.getContentType());
			response.setHeader("Access-Control-Allow-Origin", "*");
			response.setHeader("Access-Control-Allow-Methods", "GET,POST");
			//OutputStream out1 = response.getOutputStream();
			BufferedReader rd = new BufferedReader(new InputStreamReader(connection.getInputStream(),"utf-8"));
			String line;
			while ((line = rd.readLine()) != null) {
				out.println(line);
			}
			rd.close();

		} catch (Exception e) {
			e.printStackTrace();
			response.setStatus(500);
		}

	}
%>