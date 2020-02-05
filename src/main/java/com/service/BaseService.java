package com.service;

import com.alibaba.fastjson.JSON;
import com.dao.BaseDao;
import com.dao.MongoDao;
import com.dao.mapper.BaseMapper;
import com.dao.mapper.FileUploadMapper;
import com.dao.mapper.XaMapper;
import com.manage.RedisSessionManager;
import com.manage.tasks.CacheTask;
import com.model.CacheInfo;
import com.model.MySqlException;
import com.model.RequireLoginException;
import com.model.resultDataObject;
import com.util.*;
import com.util.RedisQueue.Action;
import com.util.RedisQueue.ISerializer;

import oracle.sql.CLOB;

import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import redis.clients.jedis.Jedis;

import javax.annotation.Resource;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
/***
 * 2018
 * */
@Service(value = "BaseService")
public class BaseService{
	@Resource(name = "BaseDao")
	private BaseDao baseDao;
	@Resource(name = "XaMapper")
	private XaMapper xaMapper;
	@Resource(name="FileUploadMapper")
	private FileUploadMapper fileUploadMapper;
	@Autowired
	BaseMapper baseMapper;
	@Autowired
	CacheService cacheService;
	
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(BaseService.class);
	private static final String OPECODE = "d4107dea-5d6f-3ebf-df6b-8cfe77425965";

	private static boolean isDebuger = PropertiesConfigUtil.getConfigByKey("isdebuger").equals("1");

	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	static SimpleDateFormat sdfday2 = new SimpleDateFormat("yyyyMMdd");
	ConcurrentHashMap<String,LRUHashMap<String, resultDataObject>> LRU_MAP  = new ConcurrentHashMap<String, LRUHashMap<String, resultDataObject>>();
	private RedisQueue<HashMap<String, Object>> SYSLOG_QUEUE = new RedisQueue<HashMap<String, Object>>
	(
			"SYSLOG_QUEUE", 
			200, 
			10000, 
			1, 
			new ISerializer<HashMap<String, Object>>() 
			{

				@Override
				public byte[] encode(HashMap<String, Object> e) 
				{
					return com.alibaba.fastjson.JSONObject.toJSONString(e).getBytes();
				}
		
				@Override
				public HashMap<String, Object> decode(byte[] buffer)
				{
					String temp = new String(buffer);
					
					HashMap<String,Object> clazz = new HashMap<String, Object>();
					HashMap<String, Object> params = com.alibaba.fastjson.JSONObject.parseObject(temp,clazz.getClass());
					return params;
				}
			},
			
			new Action<List<HashMap<String, Object>>>()
			{
				@Override
				public void run(List<HashMap<String, Object>> list) 
				{
					try{

						if(list.size()>0){
							logger.info("********setLogBatch to DB:" + list.size()+"**********************");
							saveSysLog((ArrayList<HashMap<String, Object>>)list);
						}
						
					}catch(Exception e){
						e.printStackTrace();
					}
				}
			}
	); 
	

	/**
	 * 执行数据服务项目语句
	 * @param serviceid String 服务ID
	 * @param Sqlparam HashMap<String,Object> SQL参数
	 * @param commonParam HashMap<String,Object> 通用参数
	 * */
	public resultDataObject excuteDataServerItem(int serviceid,HashMap<String,Object> Sqlparam,HashMap<String,Object> commonParam){
		
		resultDataObject result = new resultDataObject();
		//1.根据服务ID获取服务信息
		HashMap<String,Object> serviceinfo = null;
		
		//缓存
//		Comb<LRUHashMap<String, resultDataObject>,String,Boolean> cache;
		CacheInfo cacheInfo = new CacheInfo();
		com.alibaba.fastjson.JSONObject cacheconfigobj = null;
		
		String cahceSwitch = null;
		try{
			//如果不是调试模式 从服务缓存中获取服务信息
			if(!isDebuger){
				serviceinfo = CacheTask.servieMap.get(serviceid+"");
			}
			//如果获取失败则从数据库查询服务信息
			if(serviceinfo == null){
				HashMap<String, Object> param = new HashMap<String, Object>();
				param.put("id", serviceid);//服务id
				List<HashMap<String, Object>> serviceListInfo = xaMapper.queryServiceById(param);//获取当前服务信息
				if(serviceListInfo == null || serviceListInfo.size() <=0){
					result.setError("请检查服务"+serviceid+"是否存在，并确保其处于已审核的可用状态");
					result.setResult(false);
					return result;
				}
				serviceinfo = serviceListInfo.get(0);
//				if(serviceinfo != null){
//					CLOB clob = (oracle.sql.CLOB)serviceinfo.get("DS_SQLCLOB");
//					CLOB logclob = (oracle.sql.CLOB)serviceinfo.get("DS_LOGSQL");
//					serviceinfo.put("DS_SQLCLOB", ConvertUtil.ClobToString(clob));
//					serviceinfo.put("DS_LOGSQL", ConvertUtil.ClobToString(logclob));
//					
//					Object v = serviceinfo.get("DS_ISCACHE");
//					cahceSwitch = v != null ?v.toString():"";
//					if ("1".equalsIgnoreCase(cahceSwitch)){
//						try {
//							v = serviceinfo.get("DS_CACHECONFIG");
//							String cacheconfig =v != null ? v.toString():null;
//							if (cacheconfig != null && !"".equalsIgnoreCase(cacheconfig))
//							{
//								cacheconfigobj = com.alibaba.fastjson.JSONObject.parseObject(cacheconfig);
//								serviceinfo.remove("DS_CACHECONFIG");
//								serviceinfo.put("DS_CACHECONFIG", cacheconfigobj);
//							}
//						}
//						catch (Exception e) 
//						{
//							serviceinfo.put("DS_CACHECONFIG", null);
//						}
//					}
//					else
//					{
//						serviceinfo.put("DS_CACHECONFIG", null);
//					}
//				}
			}
			//不存在的服务或者停用，未审核的服务
			if(serviceinfo == null){
				result.setError("请检查服务"+serviceid+"是否存在，并确保其处于已审核的可用状态");
				result.setResult(false);
				return result;
			}
		}catch (Exception e) {
			e.printStackTrace();
			result.setError("查询服务"+serviceid+"信息失败，失败原因："+e.getMessage());
			result.setResult(false);
			return result;
		}
		
		Object v = serviceinfo.get("DS_ISCACHE");
		cahceSwitch = v != null ?v.toString():"";
		if ("1".equalsIgnoreCase(cahceSwitch)){
			cacheInfo.setNeedCache(true);
			try {
				v = serviceinfo.get("DS_CACHECONFIG");
				String cacheconfig =v != null ? v.toString():null;
				if (cacheconfig != null && !"".equalsIgnoreCase(cacheconfig)){
					cacheconfigobj = com.alibaba.fastjson.JSONObject.parseObject(cacheconfig);
					cacheInfo.setConfig(cacheconfigobj);
				}
			}catch (Exception e) {
				serviceinfo.put("DS_CACHECONFIG", null);
			}
		}else{
			serviceinfo.put("DS_CACHECONFIG", null);
		}

		
		//如果是调试模式  给返回参数多设置一些字段
		try{
			if(isDebuger){
	        	CLOB clob = (oracle.sql.CLOB)serviceinfo.get("DS_SQLCLOB");
	    		CLOB logclob = (oracle.sql.CLOB)serviceinfo.get("DS_LOGSQL");
	    		serviceinfo.put("DS_SQLCLOB", ConvertUtil.ClobToString(clob));
	    		serviceinfo.put("DS_LOGSQL", ConvertUtil.ClobToString(logclob));
				serviceinfo.put("DS_CACHECONFIG", cacheconfigobj);
	        }
		}catch(Exception e){
			
		}
		
		//返回服务信息
		result.setServiceinfo(serviceinfo);
		
		HashMap<String, Object> user = (HashMap<String, Object>)commonParam.get("user");
		//2.服务是否需要登陆
		if(serviceinfo.get("DS_ISCHECKLOGIN") != null && serviceinfo.get("DS_ISCHECKLOGIN").toString().equals("1")){
			if(user != null && !user.toString().equals("") && user.get("US_CODE") != null){
				;
			}else{
				result.setError("服务"+serviceid+"需要登陆，请先登陆后在进行该操作!");
				result.setResult(false);
				result.setRedirect("redirect");
				return result;
			}
		}
		
		//2.5. 验证服务用户权限
		//&& user != null && !user.toString().equals("") && user.get("US_CODE") != null && !"admin".equals(user.get("US_LNNM").toString().trim())
		if(serviceinfo.get("DS_ISCHECKUSER") != null && serviceinfo.get("DS_ISCHECKUSER").toString().equals("1")){
			if(user != null && !user.toString().equals("") && user.get("US_CODE") != null){
				if("admin".equals(user.get("US_LNNM").toString().trim())){
					;
				}else{
					if(CacheTask.userSvrInfos != null){
						String dscodes = CacheTask.userSvrInfos.get(user.get("US_CODE").toString());
						if(dscodes == null || dscodes.indexOf(serviceid + ",") == -1){
							result.setError("服务"+serviceid+"未对您授权，请联系管理员解决！");
							result.setResult(false);
							return result;
						}
					}else{
						result.setError("服务正在重启中，请稍后重试！！");
						result.setResult(false);
						return result;
					}
				}
			}else{
				result.setError("服务"+serviceid+"需要登陆，请先登陆后在进行该操作!");
				result.setResult(false);
				result.setRedirect("redirect");
				return result;
			}
		}
		
		//3.执行服务
		if(serviceinfo.get("DS_TYPE") != null && serviceinfo.get("DS_TYPE").toString().equals("2")){
			result.setError("暂时不支持的操作");
			result.setResult(true);
			return result;
		}else if(serviceinfo.get("DS_TYPE") != null && serviceinfo.get("DS_TYPE").toString().equals("3")){
			//空操作
			result.setResult(true);
			return result;
		}else if(//通用表服务
				serviceinfo.get("DS_TYPE") != null && serviceinfo.get("DS_TYPE").toString().equals("1")
			  &&serviceinfo.get("DS_SQLCLOB") != null && serviceinfo.get("DS_SQLCLOB").toString().length() != 0
			  &&serviceinfo.get("SQL_TYPE") != null && serviceinfo.get("SQL_TYPE").toString().length() != 0
			  &&serviceinfo.get("DS_PRAPTYPE") != null && serviceinfo.get("DS_PRAPTYPE").toString().length() != 0){
			String sqlstr = serviceinfo.get("DS_SQLCLOB").toString();                 //SQL语句
			
			//查询语句是否包含权限控制的表
			if(serviceinfo.get("DS_ISAUTHOR") != null && serviceinfo.get("DS_ISAUTHOR").equals("1")){
				//admin 和 不需要登录的操作 不执行权限控制 
				if(user == null || user.get("US_LNNM")==null ){
					result.setError("登录失效，请重新登录!");
					result.setResult(false);
					result.setRedirect("redirect");
					return result;
				}				
				
				if(!"admin".equals(user.get("US_LNNM").toString().trim())){ 
					//获取服务对应的权限code,根据权限code 获取权限完整信息
					List<Integer> authList = CacheTask.serverAuthRefs.get(Integer.valueOf(serviceinfo.get("DS_CODE").toString()));
					if(authList == null)
						authList = new ArrayList<Integer>();
					for (Integer integer : authList) {
						HashMap<String,Object> qxitem = CacheTask.authInfos.get(integer);
						Pattern p = Pattern.compile( "(\\s|,)"+"auth."+"(" + qxitem.get("PSD_DUSNM") + ".){0,1}" + qxitem.get("PSD_TNAME").toString()+"(\\s|,)",Pattern.CASE_INSENSITIVE);
						Matcher m = p.matcher(sqlstr);
						if( m.find())
						{
							String fstr = m.group();
							if(fstr.startsWith(",")||fstr.startsWith(" "))
								fstr = fstr.substring(1);
							if(fstr.endsWith(",")||fstr.endsWith(" "))
								fstr = fstr.substring(0,fstr.length() - 1);
							String qxsql = qxitem.get("PSD_VALUESQL").toString();

							if(user != null && !user.toString().equals("") && user.get("US_CODE") != null){
								qxsql = qxsql.replace("#{lnnm}", "'"+user.get("US_LNNM").toString()+"'");
								sqlstr = sqlstr.replace(fstr, "(select * from "+fstr.replaceAll("(?i)auth.", "")+" where "+qxitem.get("PSD_FIELD").toString()+" in ("+qxsql+"))");
							}else{
								//如果用户不存在，从参数里取USCODE字段。APP临时方案
								result.setError("登录失效，请重新登录!");
								result.setResult(false);
								result.setRedirect("redirect");
								return result;
							}
						}
					}

				}else{
					sqlstr = sqlstr.replaceAll("(?i)auth.","");
				}
			}
			
			//替换注释
			sqlstr = sqlstr.replaceAll("(?ms)--.*?$|/\\*.*?\\*/", " ");
			
			//准备参数
			Pattern pattern = Pattern.compile("(\\#\\{)(\\w*)(\\})");    
			Matcher matcher = pattern.matcher(sqlstr);  
			List<String> list = new ArrayList();  
			while(matcher.find()){
				String group = matcher.group(2);
				list.add(group);
			}
			//检查参数
			ArrayList<HashMap<String, Object>> params = new ArrayList<HashMap<String,Object>>();
			boolean isparamright = true;
			
			try{
				JSONObject rparams = new JSONObject(serviceinfo.get("DS_PRAPTYPE").toString());        //参数类型
				//检查生成主键UUID
				Iterator iterator = rparams.keys();
				while(iterator.hasNext()){
		            String key = (String) iterator.next();
				    String value = rparams.getString(key);
				    
				    //是否包含输入输出类型信息
				    if(value.indexOf("#") != -1){
				    	value = value.split("#")[0];
				    }
				    //生成UUID
				    if(value.equalsIgnoreCase("UUID")){
						Sqlparam.put(key, UUID.randomUUID().toString());
				    }
				    //解密加密字符串
				    if(value.equalsIgnoreCase("ENCRYPT")){
				    	String encryptStr = Sqlparam.get(key).toString();
				    	String decryptStr = "";
				    	try{
				    		decryptStr = AesEncryptUtil.desEncrypt(encryptStr);
				    	}catch (Exception e) {
				    		result.setError("解密数据失败!");
							result.setResult(false);
							return result;
						}
						Sqlparam.put(key, decryptStr);
				    }
				    //服务器时间
				    if(value.equalsIgnoreCase("SYSDATE")){
				    	Sqlparam.put(key, sdf.format(new Date()));
				    }
				    //解析用户ID
				    if(value.equalsIgnoreCase("USCODE")){
				    	//先去session里面取用户信息
						if(user != null && !user.toString().equals("") && user.get("US_CODE") != null){
							Sqlparam.put(key, user.get("US_CODE").toString());
						}else{
							//如果用户不存在，从参数里取USCODE字段。APP临时方案
							result.setError("登录失效，请重新登录!");
							result.setResult(false);
							result.setRedirect("redirect");
							return result;
						}
				    }
				    //解析文件
				    if(value.equalsIgnoreCase("FILE")){				    	
				    	String filestr = Sqlparam.get(key).toString();
				    	
						if(filestr.trim().toString().equals("")){
							Sqlparam.put(key, "");
							continue;
						}else{
							String[] fileArr = filestr.split("#|:");
							String re_code = UUID.randomUUID().toString();
							if(fileArr.length == 3){
								HashMap<String, String> map = null;
								try{
									map = FileUploadUtil.transFile(fileArr[2], fileArr[1],fileArr[0],re_code);
									String rt_code = CacheTask.resTypeMap.get(map.get("RT_NAME"));
									if(rt_code == null || rt_code.equals(""))
										rt_code = fileUploadMapper.getResourceType(map).get("RT_CODE").toString();//获得资源类型主键
									if(rt_code == null || rt_code.equals(""))
										throw new RuntimeException("不支持的文件类型！");
									map.put("RE_CODE", re_code);
									map.put("RT_CODE", rt_code);
									map.put("RE_DATE", sdf.format(new Date()));
									fileUploadMapper.addResource(map);
								}catch(Exception e){
									throw e;
								}
							}else{
								throw new RuntimeException("上传文件信息编码错误！");
							}
							
							Sqlparam.put(key, re_code);
							continue;
						}
				    }
				    //解析文件数组
				    if(value.equalsIgnoreCase("FILE[]")){
				    	Object obj1 = Sqlparam.get(key);
				    	String[] filestrArr = null;
				    	if(obj1 instanceof ArrayList){
				    		ArrayList arr = (ArrayList)obj1;
				    		filestrArr = new String[arr.size()];
				    		arr.toArray(filestrArr);
				    	}else{
				    		String filestr = (String)obj1;
				    		filestrArr = filestr.split(",");
				    	}

						if(filestrArr == null || filestrArr.length == 0){
							Sqlparam.put(key, "");
							continue;
						}else{
							HashMap<String, Object> gmap = new HashMap<String, Object>();
							String g_code = UUID.randomUUID().toString();  // 资源组ID
							gmap.put("RESG_CODE", g_code);
							gmap.put("RESG_NAME", g_code);
							gmap.put("RESG_REMARKS", "");
							ArrayList<HashMap<String, Object>> list1 = new ArrayList<HashMap<String,Object>>();
							try{
								fileUploadMapper.addResourceGroup(gmap);    //添加资源组信息
								for(String filebase64:filestrArr){
									String[] filebArr = filebase64.split("#|:");
									String re_code = UUID.randomUUID().toString();  //资源ID
									if(filebArr.length == 3){
										HashMap<String, String> map = null;
										try{
											map = FileUploadUtil.transFile(filebArr[2], filebArr[1],filebArr[0],re_code);
											String rt_code = CacheTask.resTypeMap.get(map.get("RT_NAME"));
											if(rt_code == null || rt_code.equals(""))
												rt_code = fileUploadMapper.getResourceType(map).get("RT_CODE").toString();//获得资源类型主键
											if(rt_code == null || rt_code.equals(""))
												throw new RuntimeException("不支持的文件类型！");
											map.put("RE_CODE", re_code);
											map.put("RT_CODE", rt_code);
											map.put("RE_DATE", sdf.format(new Date()));
											fileUploadMapper.addResource(map);
											
											HashMap<String, Object> obj = new HashMap<String, Object>();
											obj.put("RE_CODE", re_code);
											obj.put("RESG_CODE", g_code);
											list1.add(obj);
										}catch(Exception e){
											throw e;
										}
									}else{
										throw new RuntimeException("上传文件信息编码错误！");
									}
								}
								fileUploadMapper.addResourceGroupItem(list1);
							}catch (Exception e) {
								fileUploadMapper.deleteResourceGroup(gmap);
								for(int k = 0 ;k<list.size();k++){
									fileUploadMapper.deleteResource(list1.get(k));
								}
								throw e;
							}
							
							Sqlparam.put(key, g_code);
							continue;
						}
				    }
				}
				//替换参数
				for(int i = 0;i<list.size();i++){
					String itemp = list.get(i);
					if(Sqlparam == null){
						isparamright = false;
						break;
					}
					Object value = Sqlparam.get(itemp);
					if(value == null){
						String type = rparams.has(itemp)?rparams.getString(itemp):"string";
						String inout = "IN";
						String t = type;
						if(type.indexOf("#") != -1){
							t = type.split("#")[0];
							if(t == null || t.equals("")){
								t = "string";
							}
					    	inout = type.split("#")[1].toUpperCase();
					    	if(!inout.equals("IN")&&!inout.equals("OUT")){
					    		result.setError("参数输入输出类型错误!位置类型"+inout+",请联系系统管理员");
								result.setResult(false);
								return result;
					    	}
					    }
						
						HashMap<String, Object> param = new HashMap<String, Object>();
						if(inout.equals("IN")){
							param.put("type", "keyw");
							param.put("value", "null");
						}else{
							param.put("type", t);
							param.put("value", "");
						}
						param.put("inout", inout);
						param.put("name", itemp);
						params.add(param);
					}else{
						String type = rparams.has(itemp)?rparams.getString(itemp):"string";
						String inout = "IN";
						String t = type;
						if(type.indexOf("#") != -1){
							t = type.split("#")[0];
							if(t == null || t.equals("")){
								t = "string";
							}
					    	inout = type.split("#")[1].toUpperCase();
					    	if(!inout.equals("IN")&&!inout.equals("OUT")){
					    		result.setError("参数输入输出类型错误!位置类型"+inout+",请联系系统管理员");
								result.setResult(false);
								return result;
					    	}
					    }
						
						HashMap<String, Object> param = new HashMap<String, Object>();
						param.put("type", t);
						param.put("value", value);
						param.put("inout", inout);
						param.put("name", itemp);
						params.add(param);
					}
					
					sqlstr = sqlstr.replace("#{"+itemp+"}", "?");
				}
			}catch(Exception e){
				e.printStackTrace();
				result.setError("解析参数出错。"+e.getMessage());
				result.setResult(false);
				return result;
			}
			
			if(!isparamright){
				result.setError("缺少参数！");
				result.setResult(false);
				return result;
			}
			
			//执行语句
			Object data = null;
			Object pageinfo = null;
			Object updatecount = null;
			Object sqllist = null;
			Object paramlist = null;
			try{
				//初始化缓存容器  设置缓存策略
//				cache = createRtuCacheInfo(serviceid,serviceinfo,params,commonParam);
//				if(null != cache){
//					resultDataObject ret = cache.getV1().get(cache.getV2());
//					if (ret != null){
//						ret.setCache(true);
//						return ret;
//					}
//				}
				
				//更换为从redis中取数据
				//TODO
				if(cacheInfo.isNeedCache()){
					getResultFromRedis(cacheInfo,serviceid,serviceinfo,params,commonParam);
					if (cacheInfo.getValue() != null){
						resultDataObject cacheValue = cacheInfo.getValue();
						cacheValue.setCache(true);
						return cacheValue;
					}
				}
				
				//查询
				if(serviceinfo.get("SQL_TYPE").toString().equals("3")){
					HashMap<String, Object> queryresult = baseDao.querySQL(
							Integer.parseInt(serviceinfo.get("DTSR_CODE").toString()),
							sqlstr,params,
							commonParam);
					data = queryresult.get("data");
					pageinfo = queryresult.get("pageinfo");
					sqllist = queryresult.get("sqllist");
					paramlist = queryresult.get("paramlist");
				}else if(serviceinfo.get("SQL_TYPE").toString().equals("2")){
					//修改
					HashMap<String, Object> queryresult = baseDao.queryUpdaetSQL(
							Integer.parseInt(serviceinfo.get("DTSR_CODE").toString()),
							sqlstr,params,
							commonParam);
					data = queryresult.get("data");
					updatecount = queryresult.get("updatecount");
					sqllist = queryresult.get("sqllist");
					paramlist = queryresult.get("paramlist");
				}else if(serviceinfo.get("SQL_TYPE").toString().equals("1")){
					//删除
					result.setError("服务为删除操作，平台暂不支持，请联系管理员!");
					result.setResult(false);
					return result;
				}else if(serviceinfo.get("SQL_TYPE").toString().equals("0")){
					//添加
					HashMap<String, Object> queryresult = baseDao.queryInsertSQL(
							Integer.parseInt(serviceinfo.get("DTSR_CODE").toString()),
							sqlstr,params,
							commonParam);
					data = queryresult.get("data");
					updatecount = queryresult.get("updatecount");
					sqllist = queryresult.get("sqllist");
					paramlist = queryresult.get("paramlist");
				}else if(serviceinfo.get("SQL_TYPE").toString().equals("5")){
					//增删改存储过程
					HashMap<String, Object> queryresult = baseDao.queryProcessSQL(    
							Integer.parseInt(serviceinfo.get("DTSR_CODE").toString()),
							sqlstr,params,
							commonParam);
					
					data = queryresult.get("data");
					updatecount = queryresult.get("updatecount");
					sqllist = queryresult.get("sqllist");
					paramlist = queryresult.get("paramlist");
				}else if(serviceinfo.get("SQL_TYPE").toString().equals("6")){
					//拼接SQL
					HashMap<String, Object> queryresult = baseDao.queryProcessSQL(    
							Integer.parseInt(serviceinfo.get("DTSR_CODE").toString()),
							sqlstr,params,
							commonParam);
					
					data = queryresult.get("data");
					updatecount = queryresult.get("updatecount");
					pageinfo = queryresult.get("pageinfo");
					sqllist = queryresult.get("sqllist");
					paramlist = queryresult.get("paramlist");
				}else{
					result.setError("不支持的SQL操作类型!");
					result.setResult(false);
					return result;
				}
			}catch (MySqlException e) {
				e.printStackTrace();
				result.setError("执行语句失败!"+e.getMessage());
				result.setSql(e.getSql());
				result.setResult(false);
				return result;
			}catch (RequireLoginException e) {
				result.setError("登录失效，请重新登录!");
				result.setResult(false);
				result.setRedirect("redirect");
				return result;
			}catch (Exception e) {
				e.printStackTrace();
				result.setError("执行语句失败!"+e.getMessage());
				result.setResult(false);
				return result;
			}
			result.setResult(true);
			result.setData(data);
			result.setUpdatecount(updatecount);
			result.setPageinfo(pageinfo);
			result.setSql(sqllist);
			result.setSqlparam(paramlist);
		}else{
			result.setError("服务配置出错，请联系管理员解决!");
			result.setResult(false);
			return result;
		}
		//重新设置缓存
//		if(null != cache && result.isResult())
//		{
//			if(cache.getV3()){
//				cache.getV1().put(cache.getV2(),result);
//			}else{
//				if(null != result.getData()){
//					cache.getV1().put(cache.getV2(),result);
//				}
//			}
//		}
		cacheService.setCache(cacheInfo,result);		
		return result;
	}

	/**
	 * 日志
	 * 
	 * */
	public boolean excuteDataServerLogItem(HashMap<String,Object> Sqlparam,HashMap<String,Object> commonParam,resultDataObject data){
		boolean result = false;
		HashMap<String, Object> svrinfo = data.getServiceinfo();
		try{
			//服务信息存在
			if(svrinfo != null){
				String islog = svrinfo.get("DS_ISLOG") != null?svrinfo.get("DS_ISLOG").toString():"0";
				String logsql = svrinfo.get("DS_LOGSQL") != null?svrinfo.get("DS_LOGSQL").toString():"";
				String ywtype = svrinfo.get("DS_RZTYPE") != null?svrinfo.get("DS_RZTYPE").toString():"";
				String svrcode = svrinfo.get("DS_CODE") != null?svrinfo.get("DS_CODE").toString():"";
				if(islog.equals("1")){
					HashMap<String, Object> user = (HashMap<String, Object>)commonParam.get("user");
			
					HashMap<String, Object> map = new HashMap<String, Object>();
					map.put("LG_SESSION", ConvertUtil.getMapValue(commonParam,"jsessionid"));
					map.put("SYS_CODE", ConvertUtil.getMapValue(commonParam,"sysCode"));
					map.put("FUNC_CODE", ConvertUtil.getMapValue(commonParam,"funcCode"));
					map.put("OPE_CODE", ConvertUtil.getMapValue(commonParam,"opeCode"));
					map.put("US_CODE", user == null ? "" : user.get("US_CODE"));
					Date bb = (Date)ConvertUtil.getMapValue(commonParam,"begintime");
					Date cc = (Date)ConvertUtil.getMapValue(commonParam,"endtime");
					map.put("LG_STM", bb);
					map.put("LG_COST", cc.getTime() - bb.getTime());
					map.put("LG_TM", new Date());
					map.put("LG_ERR", data.getError());
					map.put("LG_CLIENTTYPE", ConvertUtil.getMapValue(commonParam,"clienttype"));
					map.put("LG_CLIENTIP", ConvertUtil.getMapValue(commonParam,"ip"));
					map.put("DSLG_CLIENTNAME", ConvertUtil.getMapValue(commonParam,"clientname"));
					map.put("LG_CLIENTPORT", ConvertUtil.getMapValue(commonParam,"port"));
					map.put("LG_ISSUCESS", data.isResult());
					map.put("LG_SQLCLOB", JSON.toJSONString(data.getSql()));
					map.put("LG_SQLPARAM", JSON.toJSONString(data.getSqlparam()));
					map.put("LG_TYPE", ywtype);
//					map.put("LG_DESC", desc == null?"":desc);//描述信息
					map.put("LG_REMARKS", "通用服务");
					map.put("LG_URL", commonParam.get("referer"));
					map.put("LG_SVRCODE", svrcode == null?"":svrcode);
					map.put("LG_SYSVERSION", ConvertUtil.getMapValue(commonParam,"sysVersion"));
					map.put("LG_CLIENTINFO", ConvertUtil.getMapValue(commonParam,"clientInfo"));
					
					addLog(map);
					result = true;
				}
			}
		}catch (Exception e) {
			e.printStackTrace();
		}
		
		return result;
	}
	
	private void saveSysLog(ArrayList<HashMap<String, Object>> logList)
	{
		if(logList != null && logList.size() > 0)
			try{
				MongoDao.SaveLogData(logList);
			}catch(Exception e){
				logger.error("批量存入日志发生错误",e);
				for (HashMap<String, Object> hashMap : logList) {
					logger.error("批量存入日志发生错误,参数为:"+hashMap);
				}
			}
	}
	
	public void addLog(HashMap<String, Object> map){
		this.SYSLOG_QUEUE.add(map);
	}
	
	public HashMap<String,Object> login(HashMap<String,Object> paraMap){
		List<HashMap<String,Object>> userList = baseMapper.searchUser(paraMap);
		if(userList.size() > 0)
			return userList.get(0);
		else
			return null;
				
	}
	
	public HashMap<String,Object> OtherLogin(HashMap<String,Object> paraMap){
		List<HashMap<String,Object>> userList = baseMapper.searchUserByUsSubcode(paraMap);
		if(userList.size()<=0){
			HashMap<String, Object> userTypeMap = baseMapper.getUtCode(paraMap);
			paraMap.put("UT_CODE", userTypeMap.get("UT_CODE"));
			baseMapper.addUser(paraMap);
		}else{
			return userList.get(0);
		}
		return baseMapper.searchUserByUsSubcode(paraMap).get(0);
	}
	
	/**
	 *设置缓存策略 
	 **/
	public Comb<LRUHashMap<String, resultDataObject>,String,Boolean> createRtuCacheInfo(int serviceid,HashMap<String,Object> serviceinfo,
			List<HashMap<String,Object>> params,HashMap<String,Object> commonParams)
	{
		String servicetype = serviceinfo.get("SQL_TYPE").toString();
		
		if(!servicetype.equals("3")&&!servicetype.equals("5"))
		{
			return null;
		}
		
		Object obj = serviceinfo.get("DS_CACHECONFIG");
		if(null == obj)
		{
			return null;
		}
		int maxCapacity,maxExpire,maxIdle;
		boolean isCacheEmpty = false;
		
		try{
            com.alibaba.fastjson.JSONObject config = (com.alibaba.fastjson.JSONObject)obj;
			maxCapacity  = config.getInteger("maxCapacity");               //
			maxIdle  = config.getInteger("maxIdle");                       //
			maxExpire  = config.getInteger("maxExpire");                   //
			isCacheEmpty = "1".equals(config.getInteger("isCacheEmpty"));  //是否缓存空结果
		} 
		catch (Exception e) 
		{
			logger.error("createRtuCacheInfo:"+serviceid,e);
			return null;
		}
		
		//TODO lru就是一个LinkedHashMap  初始化缓存容器  并设置缓存策略
		LRUHashMap<String, resultDataObject> lru = null;
		lru = LRU_MAP.get(String.valueOf(serviceid));
		//如果是null 创建新的lru对象，并放入 LRU_MAP (chm)中
		if (lru == null )
		{
			lru = new LRUHashMap<String, resultDataObject>(maxCapacity, null, maxIdle,maxExpire);
			LRUHashMap<String, resultDataObject> old = LRU_MAP.putIfAbsent(String.valueOf(serviceid), lru);
			if (old != null)
			{
				lru = old;
			}
		}
		
		String requestCacheKey = cacheService.createCacheKeyByParams(serviceid,params,commonParams);
//		for(int m = 0;m < params.size();m++){
//			HashMap<String, Object> param = params.get(m);
//			String value = param.get("value").toString();
//			
//			requestCacheKey += "_"+value;
//		}
//		
//		String commonStr = "";
//		if(commonParams != null){
//			if(commonParams.get("ispages")!=null){
//				int cts = 100;
//				if(commonParams.get("pagecount")!=null)
//					cts = Integer.parseInt(commonParams.get("pagecount").toString());
//				int cp = 1;
//				if(commonParams.get("currentpage")!=null)
//					cp = Integer.parseInt(commonParams.get("currentpage").toString());
//				commonStr += "_" + cts + "_" + cp;
//			}
//			if(commonParams.get("orderfield")!=null){
//				if(commonParams.get("order")!=null){
//					commonStr += "_" + commonParams.get("orderfield").toString() +"_"+ commonParams.get("order").toString();
//				}else{
//					commonStr += "_" + commonParams.get("orderfield").toString() +"_asc nulls first";
//				}
//			}
//			if(commonParams.get("advaceorder")!=null){
//				commonStr += "_"+commonParams.get("advaceorder");
//			}
//		}
//		
//		requestCacheKey += commonStr;
//		requestCacheKey = ConvertUtil.stringToMD5(requestCacheKey);
		
		return new Comb<LRUHashMap<String, resultDataObject>,String,Boolean>(lru, requestCacheKey,isCacheEmpty);
	}
	
	
	/**从redis中获取缓存结果
	 * @param cacheInfo */
	private void getResultFromRedis(
			CacheInfo cacheInfo, int serviceid, 
			HashMap<String, Object> serviceinfo,
			ArrayList<HashMap<String, Object>> params,
			HashMap<String, Object> commonParam) {
		
		resultDataObject result = null;
		Jedis jedis = null;
		
        String servicetype = serviceinfo.get("SQL_TYPE").toString();		
		if(!servicetype.equals("3")&&!servicetype.equals("5")){
			return;
		}
		
		if(null != cacheInfo.getConfig()){
			com.alibaba.fastjson.JSONObject config = cacheInfo.getConfig();
            int expireSeconds = Math.min(config.getInteger("maxIdle"),config.getInteger("maxExpire"));
            cacheInfo.setExpireSeconds(expireSeconds/1000);
			cacheInfo.setCacheEmpty(1 == config.getInteger("isCacheEmpty"));
		}
				
		String cacheKey = cacheService.createCacheKeyByParams(serviceid,params,commonParam);
		cacheInfo.setKey(cacheKey);
		
		try{
			jedis = RedisUtil.getJedis(15);
			String cacheValue = jedis.get(cacheKey);
			if(null == cacheValue || "".equals(cacheValue)){
				return;
			}			
			cacheInfo.setValue(com.alibaba.fastjson.JSONObject.parseObject(jedis.get(cacheKey),resultDataObject.class));
		}finally{
			RedisUtil.returnResource(jedis);
		}
	}	

	/**获取用户在线人数,历史访问量*/
	public HashMap<String, Object> getOnlineNumInf() {
		HashMap<String,Object> hashMap = new HashMap<String, Object>();
		
		List<HashMap<String,Object>> list = xaMapper.getHistoryVisit();
		hashMap.put("HIS_NUM",list.get(0).get("HISTORY_NUM")); //历史访问
		hashMap.put("NOW_NUM",RedisSessionManager.getSessionCount());//当前在线人数
		hashMap.put("COM_HIS_NUM",0); //公众历史访问
		hashMap.put("COM_NOW_NUM",0); //公众当前在线
		
		return hashMap;
	}

}
