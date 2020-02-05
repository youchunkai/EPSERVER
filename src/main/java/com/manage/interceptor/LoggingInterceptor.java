package com.manage.interceptor;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

public class LoggingInterceptor implements HandlerInterceptor {
	private static ThreadLocal<Long> threadLocal = new ThreadLocal<Long>();
	
	@Override
	public void afterCompletion(HttpServletRequest req,HttpServletResponse res, Object arg2, Exception arg3)throws Exception {
//		System.out.println( req.getRequestURL() + "->请求用时：" + (System.currentTimeMillis() - threadLocal.get()) );
		//logging
//		System.out.println("111");
	}

	@Override
	public void postHandle(HttpServletRequest arg0, HttpServletResponse arg1,
			Object arg2, ModelAndView arg3) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean preHandle(HttpServletRequest req, HttpServletResponse res,Object arg2) throws Exception {
		// TODO Auto-generated method stub
		threadLocal.set(System.currentTimeMillis());
		
		HttpSession session = req.getSession();
		String jsessionid = session.getId();
		Cookie cookie1=new Cookie("XAHBSSOSID",jsessionid);
		cookie1.setPath("/");
		cookie1.setMaxAge(60*60);
		res.addCookie(cookie1);
		
		return true;
	}

}
