package com.controller;

import java.net.URLEncoder;
import java.util.HashMap;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.model.ResultData;
import com.service.AlipayLoginService;
import com.service.ThirdService;
import com.util.PropertiesConfigUtil;

@Controller
@RequestMapping("/third")
public class ThrdLonginController {
	@Autowired
    private AlipayLoginService alipayLoginService;
	
	@Autowired
	private ThirdService thirdService;
	/**
	 * 支付宝第三方登录服务
	 *
	 **/
    @RequestMapping("/aliLogin")
    public @ResponseBody String getAuthCode(HttpServletRequest request, HttpServletResponse response) throws Exception {
        //从request中获取授权信息
        String authCode = request.getParameter("auth_code");
        String appID = request.getParameter("app_id");
        String scope = request.getParameter("scope");

        if (StringUtils.isNotEmpty(authCode)) {
            //获取access_token
            String accessToken = alipayLoginService.getAccessToken(authCode);
            //获取用户信息
            if (StringUtils.isNotEmpty(accessToken)) {
                //获取用户信息
                HashMap<String, Object> alipayUser = alipayLoginService.getUserInfoByToken(accessToken);

                //存储到cookie中
                String name = "";
                if(alipayUser.get("NickName") != null){
                	name = alipayUser.get("NickName").toString();
                }else{
                	name = "Alic"+alipayUser.get("UserId").toString().subSequence(0, 5).toString();
                }
                Cookie cookieName = new Cookie("account", URLEncoder.encode(name, "UTF-8"));
                Cookie cookieRole = new Cookie("roleName", "alicount");
                cookieName.setMaxAge(3600);
                cookieRole.setMaxAge(3600);
                cookieName.setPath("/");
                cookieRole.setPath("/");
                response.addCookie(cookieName);
                response.addCookie(cookieRole);
                //跳转至主界面
                if(PropertiesConfigUtil.getConfigByKey(appID) != null){
                	String uri = PropertiesConfigUtil.getConfigByKey(appID) + "?" + "usname="+name;
                	response.sendRedirect(URLEncoder.encode(uri, "UTF-8"));
                }else
                	response.sendRedirect("http://xaepb.xa.gov.cn/ptl/index.html");
            }
        }

        return "hello alipay!";
    }

    //根据行政区划 获取 行政区划下 在用的用户信息
    @RequestMapping(value="getUserInfoByDistrict",method={RequestMethod.POST})
    @ResponseBody
    public ResultData getUserInfoByDistrict(@RequestBody HashMap<String,String> params,HttpServletRequest request,HttpServletResponse response){
    	if(request.getHeader("Referer") != null){
    		return new ResultData(false,"该接口不支持浏览器直接调用!");
    	}
    	String district = params.get("district");
    	String xahbToken = params.get("xahbToken"); 
    	return thirdService.getUserInfoByDistrict(district,xahbToken);
    }
   
	/***
	 * 第三方登陆
	 * */
	@RequestMapping(value="/tLogin",method={RequestMethod.POST})
	@ResponseBody
	public ResultData tlogin(@RequestBody HashMap<String,Object> params,HttpServletRequest request,HttpServletResponse response){
		if(request.getHeader("Referer") != null){
    		return new ResultData(false,"该接口不支持浏览器直接调用!");
    	}
		HttpSession session = request.getSession();

		ResultData result = new ResultData();
		Object token = params.get("xahbToken");
		Object usname = params.get("usName");
		Object password = params.get("passWord");
		Object districtcode = params.get("districtCode");
		
		if(usname!=null&&password!=null&&token!= null&&districtcode!= null
				&&!usname.toString().equals("")&&!password.toString().equals("")&&!token.toString().equals("")&&!districtcode.toString().equals("")){
				result = thirdService.tLogin(usname.toString(), password.toString(), districtcode.toString(), token.toString(), session);
		}else{
				result.setResult(false);
	        	result.setMsg("登陆信息不完整！");
	        	result.setData(null);
		}

		return result;
	}
	
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
	
	//获取网格信息
	@RequestMapping(value="getGridInfo",method={RequestMethod.POST})
    @ResponseBody
    public ResultData getGridInfo(@RequestBody HashMap<String,String> params,HttpServletRequest request,HttpServletResponse response){
    	if(request.getHeader("Referer") != null){
    		return new ResultData(false,"该接口不支持浏览器直接调用!");
    	}
    	String district = params.get("district");
    	String xahbToken = params.get("xahbToken"); 
    	return thirdService.getGridInfo(district,xahbToken);
    }
	
	//获取一段时间内 用户信息发生变化的 用户信息  用于同步数据用
	@RequestMapping(value="getUpdateUserInfo",method={RequestMethod.POST})
    @ResponseBody
    public ResultData getUpdateUserInfo(@RequestBody HashMap<String,String> requestParams,HttpServletRequest request,HttpServletResponse response){
    	if(request.getHeader("Referer") != null){
    		return new ResultData(false,"该接口不支持浏览器直接调用!");
    	}
		HashMap<String,String> params = new HashMap<String,String>();
		params.put("district",requestParams.get("district"));
		params.put("token",requestParams.get("xahbToken"));
		params.put("staTime",requestParams.get("staTime"));
		params.put("endTime",requestParams.get("endTime"));
		
		if(!validParams(params)){
			return new ResultData(false,"参数为空,请检查参数！");
		}

    	return thirdService.getUpdateUserInfo(params);
    }
	
	//获取一段时间内 用户信息发生变化的 用户信息  用于同步数据用
	@RequestMapping(value="queryUpdateUserInfo",method={RequestMethod.POST})
    @ResponseBody
    public ResultData queryUpdateUserInfo(@RequestBody HashMap<String,String> requestParams,HttpServletRequest request,HttpServletResponse response){
    	if(request.getHeader("Referer") != null){
    		return new ResultData(false,"该接口不支持浏览器直接调用!");
    	}
		HashMap<String,String> params = new HashMap<String,String>();
		params.put("district",requestParams.get("district"));
		params.put("token",requestParams.get("xahbToken"));
		params.put("staTime",requestParams.get("staTime"));
		params.put("endTime",requestParams.get("endTime"));
		
		if(!validParams(params)){
			return new ResultData(false,"参数为空,请检查参数！");
		}

    	return thirdService.queryUpdateUserInfo(params);
    }
	
	
	//获取一段时间内 用户信息发生变化的 用户信息  用于同步数据用
	@RequestMapping(value="getUpdateGridUserInfo",method={RequestMethod.POST})
    @ResponseBody
    public ResultData getUpdateGridUserInfo(@RequestBody HashMap<String,String> requestParams,HttpServletRequest request,HttpServletResponse response){
    	if(request.getHeader("Referer") != null){
    		return new ResultData(false,"该接口不支持浏览器直接调用!");
    	}
		HashMap<String,String> params = new HashMap<String,String>();
		params.put("district",requestParams.get("district"));
		params.put("token",requestParams.get("xahbToken"));
		params.put("staTime",requestParams.get("staTime"));
		params.put("endTime",requestParams.get("endTime"));
		
		if(!validParams(params)){
			return new ResultData(false,"参数为空,请检查参数！");
		}

    	return thirdService.getUpdateGridUserInfo(params);
    }	
	
	//获取污染源信息
	@RequestMapping(value="getPtsInfo",method={RequestMethod.POST})
    @ResponseBody
    public ResultData getPtsInfo(@RequestBody HashMap<String,String> requestParams,HttpServletRequest request,HttpServletResponse response){
    	if(request.getHeader("Referer") != null){
    		return new ResultData(false,"该接口不支持浏览器直接调用!");
    	}
		HashMap<String,String> params = new HashMap<String,String>();
		params.put("district",requestParams.get("district"));
		params.put("token",requestParams.get("xahbToken"));
		
		if(!validParams(params)){
			return new ResultData(false,"参数为空,请检查参数！");
		}

    	return thirdService.getPtsInfo(params);
    }
	
	//获取更新的污染源信息
	@RequestMapping(value="getUpdatePtsInfo",method={RequestMethod.POST})
    @ResponseBody
    public ResultData getUpdatePtsInfo(@RequestBody HashMap<String,String> requestParams,HttpServletRequest request,HttpServletResponse response){
	    	if(request.getHeader("Referer") != null){
	    		return new ResultData(false,"该接口不支持浏览器直接调用!");
	    	}
		HashMap<String,String> params = new HashMap<String,String>();
		params.put("district",requestParams.get("district"));
		params.put("token",requestParams.get("xahbToken"));
		params.put("staTime",requestParams.get("staTime"));
		params.put("endTime",requestParams.get("endTime"));
		
		if(!validParams(params)){
			return new ResultData(false,"参数为空,请检查参数！");
		}

    	return thirdService.getUpdatePtsInfo(params);
    }
	
	//获取二级网格下 所有污染源巡已巡查次数
	@RequestMapping(value="getPtsPatrolNum",method={RequestMethod.POST})
    @ResponseBody
    public ResultData getPtsPatrolNum(@RequestBody HashMap<String,String> requestParams,HttpServletRequest request,HttpServletResponse response){
    	if(request.getHeader("Referer") != null){
    		return new ResultData(false,"该接口不支持浏览器直接调用!");
    	}
		HashMap<String,String> params = new HashMap<String,String>();
		params.put("district",requestParams.get("district"));
		params.put("token",requestParams.get("xahbToken"));
		params.put("monthTime", requestParams.get("monthTime"));
		
		if(!validParams(params)){
			return new ResultData(false,"参数为空,请检查参数！");
		}

    	return thirdService.getPtsPatrolNum(params);
    }
	
	
	//获取一段时间内 用户信息发生变化的 用户信息  用于同步数据用
	@RequestMapping(value="getCaseInfo",method={RequestMethod.POST})
    @ResponseBody
    public ResultData getCaseInfo(@RequestBody HashMap<String,String> requestParams,HttpServletRequest request,HttpServletResponse response){
    	if(request.getHeader("Referer") != null){
    		return new ResultData(false,"该接口不支持浏览器直接调用!");
    	}
		HashMap<String,String> params = new HashMap<String,String>();
		params.put("district",requestParams.get("district"));
		params.put("token",requestParams.get("xahbToken"));
		params.put("staTime",requestParams.get("staTime"));
		params.put("endTime",requestParams.get("endTime"));
		
		if(!validParams(params)){
			return new ResultData(false,"参数为空,请检查参数！");
		}

    	return thirdService.getCaseInfo(params);
    }
	
	//获取二级网格下 日上报案件数量
	@RequestMapping(value="getDayCaseNum",method={RequestMethod.POST})
    @ResponseBody
    public ResultData getDayCaseNum(@RequestBody HashMap<String,String> requestParams,HttpServletRequest request,HttpServletResponse response){
    	if(request.getHeader("Referer") != null){
    		return new ResultData(false,"该接口不支持浏览器直接调用!");
    	}
		HashMap<String,String> params = new HashMap<String,String>();
		params.put("district",requestParams.get("district"));
		params.put("token",requestParams.get("xahbToken"));
		params.put("dayTime", requestParams.get("dayTime"));
		params.put("isValid", requestParams.get("isValid"));
		
		if(!validParams(params)){
			return new ResultData(false,"参数为空,请检查参数！");
		}

    	return thirdService.getDayCaseNum(params);
    }
	
	//获取二级网格下 月上报案件数量
	@RequestMapping(value="getMonthCaseNum",method={RequestMethod.POST})
    @ResponseBody
    public ResultData getMonthCaseNum(@RequestBody HashMap<String,String> requestParams,HttpServletRequest request,HttpServletResponse response){
    	if(request.getHeader("Referer") != null){
    		return new ResultData(false,"该接口不支持浏览器直接调用!");
    	}
		HashMap<String,String> params = new HashMap<String,String>();
		params.put("district",requestParams.get("district"));
		params.put("token",requestParams.get("xahbToken"));
		params.put("monthTime", requestParams.get("monthTime"));
		params.put("isValid", requestParams.get("isValid"));
		
		if(!validParams(params)){
			return new ResultData(false,"参数为空,请检查参数！");
		}

    	return thirdService.getMonthCaseNum(params);
    }
	
	
	@RequestMapping(value="/mytest",method={RequestMethod.POST})
	@ResponseBody
	public ResultData test(ResultData data){
		data.setData(6);
		return data;
	}

	//验证参数是否为空
	private boolean validParams(HashMap<String, String> params) {
		for(String v : params.values()){
			if(StringUtils.isEmpty(v)){
				return false;
			}
		}
		return true;
	}
}
