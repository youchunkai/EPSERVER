package com.dao.mapper;

import java.util.HashMap;
import java.util.List;

import org.apache.ibatis.annotations.Param;


public interface ThirdMapper {

	/**根据行政区划获取用户信息*/
	List<HashMap<String,Object>> getUserInfoByDistrict(@Param(value = "district") String district);
	
	List<HashMap<String,Object>> getUserInfoByDistrictAndUsName(@Param(value = "district") String district, @Param(value = "lnnm") String lnnm, @Param(value = "pw") String pw);

	/**根据行政区划获取网格信息*/
	List<HashMap<String, Object>> getGridInfo(@Param(value = "district") String district);

	/**根据行政区划 获取一段时间内，用户信息发生变化的 用户信息*/
	List<HashMap<String, Object>> getUpdateUserInfo(HashMap<String, String> params);
	
	/**根据行政区划 获取一段时间内，用户信息发生变化的 用户信息*/
	List<HashMap<String, Object>> queryUpdateUserInfo(HashMap<String, String> params);
	
	/**根据行政区划 获取一段时间内，用户信息发生变化的 网格员信息*/
	List<HashMap<String, Object>> getUpdateGridUserInfo(HashMap<String, String> params);

	/**根据行政区划 获取污染源信息*/
	List<HashMap<String, Object>> getPtsInfo(HashMap<String, String> params);

	/**根据行政区划，获取更新的污染物信息*/
	List<HashMap<String, Object>> getUpdatePtsInfo(HashMap<String, String> params);

	/**获取污染源应巡查 已巡查次数*/
	List<HashMap<String, Object>> getPtsPatrolNum(HashMap<String, String> params);
    /**获取一段时间内 上报案件数量*/
	List<HashMap<String, Object>> getCaseNum(HashMap<String, String> params);

	/**获取时间段内上报的案件的详细信息*/
	List<HashMap<String, Object>> getCaseInfo(HashMap<String, String> params);

}
