package com.service;

import io.jsonwebtoken.Claims;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.dao.mapper.BaseMapper;
import com.dao.mapper.ThirdMapper;
import com.model.ResultData;
import com.util.JwtTokenUtils;
import com.util.PropertiesConfigUtil;
/**
 * desc:第三方服务
 * */
@Transactional
@Service
public class ThirdService {
	
	static SimpleDateFormat yyyyMM = new SimpleDateFormat("yyyy-MM");
	
	static SimpleDateFormat yyyyMMdd = new SimpleDateFormat("yyyy-MM-dd");
	
	@Autowired
	private ThirdMapper thirdMapper;

	public ResultData tLogin(String usname,String pw,String district,String token,HttpSession sid){
		ResultData result = new ResultData();
		//解密
		String districtCode = "";
		try{
			Claims claims = JwtTokenUtils.phaseTokenByDistrict(district, token);
			districtCode = claims.get("district",String.class);  
		}catch (Exception e) {
			result.setResult(false);
        	result.setMsg("无效的授权码");
        	result.setData(null);
        	return result;
		}
        if(!district.equals(districtCode)){
        	result.setResult(false);
        	result.setMsg("授权信息不一致！");
        	result.setData(null);
        	return result;
        }
        
        HashMap<String, Object> params = new HashMap<String, Object>();
        params.put("usname",usname);
		params.put("password",pw);
        List<HashMap<String,Object>> userList = thirdMapper.getUserInfoByDistrictAndUsName(district, usname, pw);
        
		//登陆成功
		if(userList.size() > 0){
			sid.setAttribute("user", userList.get(0));
			
			Map map = new HashMap();
	        map.put("district",district);
	        map.put("sid",sid.getId());
	        
			result.setResult(true);
			result.setData(JwtTokenUtils.generatorTokenByDistrict(map));
			
			return result;
		}else{
			result.setResult(false);
        	result.setMsg("认证失败！");
        	result.setData(null);
        	return result;
		}
	}
	public ResultData getUserInfoByDistrict(String district,String token) {
		ResultData datas = new ResultData();
		if(StringUtils.isEmpty(district) || StringUtils.isEmpty(token)){
        	datas.setResult(false);
        	datas.setMsg("参数为空");
        	datas.setData(null);
        	return datas;
        }
		
		//验证权限
		Claims claims = JwtTokenUtils.phaseTokenByDistrict(district, token);
        String districtCode = claims.get("district",String.class);        
        if(!district.equals(districtCode)){
        	datas.setResult(false);
        	datas.setMsg("无效的权限");
        	datas.setData(null);
        	return datas;
        }
        //请求数据
        List<HashMap<String,Object>> userInfos = thirdMapper.getUserInfoByDistrict(district);
        datas.setResult(true);
    	datas.setMsg("获取用户信息成功");
    	datas.setData(userInfos);
    	return datas;
        	
	}
	
	
	/**获取权限下网格信息 ps_org*/
	public ResultData getGridInfo(String district,String token) {
		ResultData datas = new ResultData();
		if(StringUtils.isEmpty(district) || StringUtils.isEmpty(token)){
        	datas.setResult(false);
        	datas.setMsg("参数为空");
        	datas.setData(null);
        	return datas;
        }
		
		//验证权限		      
        if(!validToken(district,token)){
        	datas.setResult(false);
        	datas.setMsg("无效的权限");
        	datas.setData(null);
        	return datas;
        }
        //请求数据
        List<HashMap<String,Object>> grdInfos = thirdMapper.getGridInfo(district);
        datas.setResult(true);
    	datas.setMsg("获取网格信息成功");
    	datas.setData(grdInfos);
    	return datas;
        	
	}
	
	/**根据行政区划，获取时间段内更新的用户信息*/
	public ResultData getUpdateUserInfo(HashMap<String, String> params) {
		ResultData datas = new ResultData();
		try{
			//验证权限		      
			if(!validToken(params.get("district"),params.get("token"))){
				datas.setResult(false);
				datas.setMsg("无效的权限");
				datas.setData(null);
				return datas;
			}
			//请求数据
			List<HashMap<String,Object>> userInfos = thirdMapper.getUpdateUserInfo(params);
			datas.setResult(true);
			datas.setMsg("获取时间段内更新的用户信息成功");
			datas.setData(userInfos);
		}catch(Exception e){
			datas.setResult(false);
			datas.setMsg("程序执行错误，请联系管理员解决！");
			datas.setData(null);
		}
    	return datas;
	}
	
	/**根据行政区划，获取时间段内更新的用户信息*/
	public ResultData queryUpdateUserInfo(HashMap<String, String> params) {
		ResultData datas = new ResultData();
		try{
			//验证权限		      
			if(!validToken(params.get("district"),params.get("token"))){
				datas.setResult(false);
				datas.setMsg("无效的权限");
				datas.setData(null);
				return datas;
			}
			//请求数据
			List<HashMap<String,Object>> userInfos = thirdMapper.queryUpdateUserInfo(params);
			datas.setResult(true);
			datas.setMsg("获取时间段内更新的用户信息成功");
			datas.setData(userInfos);
		}catch(Exception e){
			datas.setResult(false);
			datas.setMsg("程序执行错误，请联系管理员解决！");
			datas.setData(null);
		}
    	return datas;
	}
	
	/**根据行政区划，获取时间段内更新的网格员信息*/
	public ResultData getUpdateGridUserInfo(HashMap<String, String> params) {
		ResultData datas = new ResultData();
		try{
			//验证权限		      
			if(!validToken(params.get("district"),params.get("token"))){
				datas.setResult(false);
				datas.setMsg("无效的权限");
				datas.setData(null);
				return datas;
			}
			//请求数据
			List<HashMap<String,Object>> userInfos = thirdMapper.getUpdateGridUserInfo(params);
			datas.setResult(true);
			datas.setMsg("获取时间段内更新的网格员信息成功");
			datas.setData(userInfos);
		}catch(Exception e){
			datas.setResult(false);
			datas.setMsg("程序执行错误，请联系管理员解决！");
			datas.setData(null);
		}
    	return datas;
	}
	
	/**根据行政区划，获取污染物信息*/
	public ResultData getPtsInfo(HashMap<String, String> params) {
		ResultData datas = new ResultData();
		try{
			//验证权限		      
			if(!validToken(params.get("district"),params.get("token"))){
				datas.setResult(false);
				datas.setMsg("无效的权限");
				datas.setData(null);
				return datas;
			}
			//请求数据
			List<HashMap<String,Object>> ptsInfos = thirdMapper.getPtsInfo(params);
			datas.setResult(true);
			datas.setMsg("获取污染源信息成功");
			datas.setData(ptsInfos);
		}catch(Exception e){
			datas.setResult(false);
			datas.setMsg("程序执行错误，请联系管理员解决！");
			datas.setData(null);
		}
    	return datas;
	}
	
	/**根据行政区划，获取更新的污染物信息*/
	public ResultData getUpdatePtsInfo(HashMap<String, String> params) {
		ResultData datas = new ResultData();
		try{
			//验证权限		      
			if(!validToken(params.get("district"),params.get("token"))){
				datas.setResult(false);
				datas.setMsg("无效的权限");
				datas.setData(null);
				return datas;
			}
			//请求数据
			List<HashMap<String,Object>> ptsInfos = thirdMapper.getUpdatePtsInfo(params);
			datas.setResult(true);
			datas.setMsg("获取时间段内更新的污染源信息成功");
			datas.setData(ptsInfos);
		}catch(Exception e){
			datas.setResult(false);
			datas.setMsg("程序执行错误，请联系管理员解决！");
			datas.setData(null);
		}
    	return datas;
	}
	
	/**污染源应巡查 已巡查 次数 */
	public ResultData getPtsPatrolNum(HashMap<String, String> params) {
		ResultData datas = new ResultData();
		try{
			
			//验证权限		      
			if(!validToken(params.get("district"),params.get("token"))){
				datas.setResult(false);
				datas.setMsg("无效的权限");
				datas.setData(null);
				return datas;
			}
			
			String monthTime = params.get("monthTime");
			Date date = yyyyMM.parse(monthTime);
			String staTime = yyyyMMdd.format(date);			
			Calendar c = Calendar.getInstance();
			c.setTime(date);			
			int lastDay = c.getActualMaximum(Calendar.DATE);			
			c.set(Calendar.DAY_OF_MONTH, lastDay);			
			String endTime = yyyyMMdd.format(c.getTime());
			
			params.put("staTime", staTime);
			params.put("endTime", endTime);			
			
			//请求数据
			List<HashMap<String,Object>> ptsInfos = thirdMapper.getPtsPatrolNum(params);
			datas.setResult(true);
			datas.setMsg("查询污染源巡查次数成功");
			datas.setData(ptsInfos);
		}catch(Exception e){
			e.printStackTrace();
			datas.setResult(false);
			datas.setMsg("程序执行错误，请联系管理员解决！");
			datas.setData(null);
		}
    	return datas;
	}
	
	
	/**获取案件详细信息*/
	public ResultData getCaseInfo(HashMap<String, String> params) {
		ResultData datas = new ResultData();
		try{
			//验证权限		      
			if(!validToken(params.get("district"),params.get("token"))){
				datas.setResult(false);
				datas.setMsg("无效的权限");
				datas.setData(null);
				return datas;
			}
			//请求数据
			List<HashMap<String,Object>> caseInfos = thirdMapper.getCaseInfo(params);
			datas.setResult(true);
			datas.setMsg("获取时间段内上报的案件信息成功");
			datas.setData(caseInfos);
		}catch(Exception e){
			e.printStackTrace();
			datas.setResult(false);
			datas.setMsg("程序执行错误，请联系管理员解决！");
			datas.setData(null);
		}
    	return datas;
	}
	
	/**按天查询案件 有效\无效 */
	public ResultData getDayCaseNum(HashMap<String, String> params) {
		ResultData datas = new ResultData();
		try{
			
			//验证权限		      
			if(!validToken(params.get("district"),params.get("token"))){
				datas.setResult(false);
				datas.setMsg("无效的权限");
				datas.setData(null);
				return datas;
			}
			
			String staTime = params.get("dayTime");
			Date date = yyyyMMdd.parse(staTime);
			Calendar c = Calendar.getInstance();
			c.setTime(date);			
			c.add(Calendar.DATE, 1);		
			String endTime = yyyyMMdd.format(c.getTime());
			
			params.put("staTime", staTime);
			params.put("endTime", endTime);			
			
			//请求数据
			List<HashMap<String,Object>> caseInfos = thirdMapper.getCaseNum(params);
			datas.setResult(true);
			datas.setMsg("查询案件上报次数成功");
			datas.setData(caseInfos);
		}catch(Exception e){
			e.printStackTrace();
			datas.setResult(false);
			datas.setMsg("程序执行错误，请联系管理员解决！");
			datas.setData(null);
		}
    	return datas;
	}
	
	
	/**按月查询案件 有效\无效 */
	public ResultData getMonthCaseNum(HashMap<String, String> params) {
		ResultData datas = new ResultData();
		try{
			
			//验证权限		      
			if(!validToken(params.get("district"),params.get("token"))){
				datas.setResult(false);
				datas.setMsg("无效的权限");
				datas.setData(null);
				return datas;
			}
			
			String monthTime = params.get("monthTime");
			Date date = yyyyMM.parse(monthTime);
			String staTime = yyyyMMdd.format(date);			
			Calendar c = Calendar.getInstance();
			c.setTime(date);
			c.add(Calendar.MONTH, 1);
			String endTime = yyyyMMdd.format(c.getTime());
			
			params.put("staTime", staTime);
			params.put("endTime", endTime);			
			
			//请求数据
			List<HashMap<String,Object>> caseInfos = thirdMapper.getCaseNum(params);
			datas.setResult(true);
			datas.setMsg("查询案件上报次数成功");
			datas.setData(caseInfos);
		}catch(Exception e){
			e.printStackTrace();
			datas.setResult(false);
			datas.setMsg("程序执行错误，请联系管理员解决！");
			datas.setData(null);
		}
    	return datas;
	}
	
	
	/**校验token信息*/
	private boolean validToken(String district,String token){
		Claims claims = JwtTokenUtils.phaseTokenByDistrict(district, token);
        String districtCode = claims.get("district",String.class);        
        return district.equals(districtCode);
	}

}
