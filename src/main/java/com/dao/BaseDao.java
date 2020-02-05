package com.dao;
import java.net.URLDecoder;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Resource;

import oracle.jdbc.driver.OracleConnection;
import oracle.sql.ARRAY;
import oracle.sql.ArrayDescriptor;
import oracle.sql.CLOB;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Repository;

import com.alibaba.druid.support.spring.DruidNativeJdbcExtractor;
import com.connection.DBPoolManager;
import com.dao.mapper.FileUploadMapper;
import com.manage.tasks.CacheTask;
import com.model.MySqlException;
import com.model.RequireLoginException;
import com.util.ConvertUtil;
/**
 * 
 * */
@Repository(value = "BaseDao")
@SuppressWarnings("all")
public class BaseDao{
//	@Resource(name = "DBServerConn")
//	private DBServerConn server;
	
	@Resource(name="FileUploadMapper")
	private FileUploadMapper fileUploadMapper;
	
	@SuppressWarnings("unused")
	private static Logger logger=Logger.getLogger(BaseDao.class);
	
	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	/***
	 * 执行指定数据库连接的SQL查询语句
	 * @param linkstr String 数据库链接字符串
	 * @param usnm String 数据库连接用户名
	 * @param uspw String 数据库密码
	 * @param sqlstr String SQL语句  如果为空默认查询指定ID服务信息
	 * @param params HashMap<String,Object> 参数map，包含类型等信息
	 * @param commonParam HashMap<String,Object> 通用参数
	 * */	
	public HashMap<String,Object> querySQL(
			int dsid,
			String sqlstr,
			ArrayList<HashMap<String,Object>> params,
			HashMap<String,Object> commonParam) throws Exception{		
		HashMap<String,Object> result = new HashMap<String, Object>();                           //返回结果
		ArrayList<HashMap<String,Object>> resultdata= new ArrayList<HashMap<String,Object>>();   //保存数据
		HashMap<String,Object> pagesdata = null;                                                 //保存分页信息

		int count = countStrInString(sqlstr,"?");
		if(count != params.size())
			throw new RuntimeException("参数数量不匹配，应该有"+count+"个参数，但是"+params.size());
		
		Connection conn = null;
		List<String> sqllist = new ArrayList<String>();
		List<Object> paramlist = new ArrayList<Object>();
		try{
			String sqlstr2 = "";
			boolean pages = false;
			conn = DBPoolManager.getConnect(dsid);
			
			//处理分页
			PreparedStatement pstmt2 = null;
			ResultSet rs2 = null;
			if(commonParam != null){
				if(commonParam.get("ispages")!=null)
					pages = Boolean.valueOf(commonParam.get("ispages").toString());
				if(pages){
					int cts = 100;
					if(commonParam.get("pagecount")!=null)
						cts = Integer.parseInt(commonParam.get("pagecount").toString());
					int cp = 1;
					if(commonParam.get("currentpage")!=null)
						cp = Integer.parseInt(commonParam.get("currentpage").toString());

					//构造查询总数
					sqlstr2 = "select count(rownum) cnt from ("+sqlstr+")";
					HashMap<String, Object> temp = prepareParam1(conn,sqlstr2,params);
					pstmt2 = (PreparedStatement)temp.get("stmt");
					rs2 = pstmt2.executeQuery();
					while(rs2.next()){
						int c = rs2.getInt("cnt");
						pagesdata = new HashMap<String, Object>();
						pagesdata.put("count", c);
						pagesdata.put("pagecount", commonParam.get("pagecount"));
						pagesdata.put("currentpage", commonParam.get("currentpage"));
							
						if(c!=0 && ((cp-1)*cts+1 > c || cp<=0)){
							throw new RuntimeException("当前分页超出范围！共有 "+ c +" 条数据");
						}
					}
					
					String orderstr = "";
					//历史原因暂时保留
					if(commonParam.get("orderfield")!=null){
						orderstr = "order by "+commonParam.get("orderfield").toString();
						if(commonParam.get("order")!=null){
							String orders = commonParam.get("order").toString();
							if(orders.indexOf("desc") != -1){
								if(orders.indexOf("last") != -1)
									orderstr = orderstr + " desc nulls last";
								else
									orderstr = orderstr + " desc nulls first";
							}else{
								if(orders.indexOf("last") != -1)
									orderstr = orderstr + " asc nulls last";
								else
									orderstr = orderstr + " asc nulls first";
							}
						}else{
							orderstr = orderstr + " asc nulls first";
						}
					}
					
					//改进order
					if(commonParam.get("advaceorder")!=null && commonParam.get("advaceorder").toString().trim().length() != 0){
						orderstr = "order by "+commonParam.get("advaceorder").toString();
					}
					
					if(orderstr.indexOf("'") != -1 
							|| orderstr.indexOf("=") != -1 
							|| orderstr.indexOf("@") != -1
							|| orderstr.indexOf("#") != -1
							|| orderstr.indexOf("(") != -1
							|| orderstr.indexOf(";") != -1
							|| orderstr.indexOf("\"") != -1
							|| orderstr.toLowerCase().indexOf("insert") != -1
							|| orderstr.toLowerCase().indexOf("update") != -1
							|| orderstr.toLowerCase().indexOf("select") != -1
							|| orderstr.toLowerCase().indexOf("grant") != -1
							|| orderstr.toLowerCase().indexOf("delete") != -1
							|| orderstr.toLowerCase().indexOf("where") != -1){
						throw new RuntimeException("排序字段不合法，");
					}
					
					//构造分页SQL
					sqlstr = "select * from (select temp.*,rownum rn from (select t.* from ("+sqlstr+") t "+orderstr+") temp) where rn between "+((cp-1)*cts+1)+" and "+(cp*cts);
				}else{
					String orderstr = "";
					//历史原因暂时保留
					if(commonParam.get("orderfield")!=null){
						orderstr = "order by "+commonParam.get("orderfield").toString();
						if(commonParam.get("order")!=null){
							String orders = commonParam.get("order").toString();
							if(orders.indexOf("desc") != -1){
								if(orders.indexOf("last") != -1)
									orderstr = orderstr + " desc nulls last";
								else
									orderstr = orderstr + " desc nulls first";
							}else{
								if(orders.indexOf("last") != -1)
									orderstr = orderstr + " asc nulls last";
								else
									orderstr = orderstr + " asc nulls first";
							}
						}else{
							orderstr = orderstr + " asc nulls first";
						}
					}
					//改进order
					if(commonParam.get("advaceorder")!=null && commonParam.get("advaceorder").toString().trim().length() != 0){
						orderstr = "order by "+commonParam.get("advaceorder").toString();
					}
					
					if(orderstr.indexOf("'") != -1 
							|| orderstr.indexOf("=") != -1 
							|| orderstr.indexOf("@") != -1
							|| orderstr.indexOf("#") != -1
							|| orderstr.indexOf("(") != -1
							|| orderstr.indexOf(";") != -1
							|| orderstr.indexOf("\"") != -1
							|| orderstr.toLowerCase().indexOf("insert") != -1
							|| orderstr.toLowerCase().indexOf("update") != -1
							|| orderstr.toLowerCase().indexOf("select") != -1
							|| orderstr.toLowerCase().indexOf("grant") != -1
							|| orderstr.toLowerCase().indexOf("delete") != -1
							|| orderstr.toLowerCase().indexOf("where") != -1){
						throw new RuntimeException("排序字段不合法，");
					}
					
					sqlstr = "select temp.*,rownum rn from ("+sqlstr+") temp " + orderstr;
				}
			}
			
			HashMap<String, Object> temp = prepareParam1(conn,sqlstr,params);
			PreparedStatement pstmt = (PreparedStatement)temp.get("stmt");
			sqllist.add(temp.get("sql") != null ? temp.get("sql").toString():"");
			paramlist.add(temp.get("param"));
			
			ResultSet rs = pstmt.executeQuery();
			ResultSetMetaData data=rs.getMetaData(); 
			
			while(rs.next()){
				HashMap<String,Object> columdata = new HashMap<String, Object>();
				for(int i = 1 ; i<= data.getColumnCount() ; i++){  
					String columnName = data.getColumnName(i); 
					String columnTypeName=data.getColumnTypeName(i);
					
					if(columnName.equalsIgnoreCase("RN")){
						boolean isrn = true;
						if(commonParam != null && commonParam.get("isrn") != null)
							isrn = Boolean.valueOf(commonParam.get("isrn").toString());
						if(!isrn){
							continue;
						}
					}
					
					//获得指定列的列值 
					Object columnValue = rs.getObject(i); 
					if(columnValue != null)
						columdata.put(columnName, columnValue);
					if(columnTypeName.equals("DATE")&&columnValue != null){
						Date date = (Timestamp)columnValue;
						columdata.put(columnName+"_STR", sdf.format(date));
					}
					if(columnTypeName.equals("CLOB")&&columnValue != null){
						CLOB clob = (oracle.sql.CLOB)columnValue;
						columdata.put(columnName, ConvertUtil.ClobToString(clob));
					}
					//处理空字段
					boolean containsblank = false;
					if(commonParam != null && commonParam.get("containsblank") != null)
						containsblank = Boolean.valueOf(commonParam.get("containsblank").toString());
					if(columnValue == null&&containsblank)
						columdata.put(columnName, "");
				}
				resultdata.add(columdata);
			}
			
			rs.close();
			pstmt.close();
			if(pages){
				rs2.close();
				pstmt2.close();
			}
			DBPoolManager.closeConn(conn);
		}catch(Exception e){
			DBPoolManager.closeConn(conn);
			throw new MySqlException(e.getMessage(),sqllist);
		}
		
		result.put("data", resultdata);
		result.put("sqllist", sqllist);
		result.put("paramlist", paramlist);
		if(pagesdata != null)
			result.put("pageinfo", pagesdata);
//		if (lru != null)
//		{
//			lru.put(serviceLRUCacheKey, result);
//		}
		
		return result;
	}
	
	/***
	 * 执行指定数据库连接的SQL查询语句
	 * @param linkstr String 数据库链接字符串
	 * @param usnm String 数据库连接用户名
	 * @param uspw String 数据库密码
	 * @param sqlstr String SQL语句  如果为空默认查询指定ID服务信息
	 * @param params HashMap<String,Object> 参数map，包含类型等信息
	 * @param commonParam HashMap<String,Object> 通用参数
	 * */	
	public HashMap<String,Object> queryUpdaetSQL(
			int id,
			String sqlstr,
			ArrayList<HashMap<String,Object>> params,
			HashMap<String,Object> commonParam) throws Exception{
		
		HashMap<String,Object> result = new HashMap<String, Object>();
		int count = countStrInString(sqlstr,"?");
		if(count != params.size()){
			throw new RuntimeException("参数数量不匹配，应该有"+count+"个参数，但是"+params.size()); 
		}
		Connection conn = null;
		conn = DBPoolManager.getConnect(id);
		boolean defaultCommit = conn.getAutoCommit();
		
		List<String> sqllist = new ArrayList<String>();
		List<Object> paramlist = new ArrayList<Object>();
		try{
			//1、开启事务
			conn.setAutoCommit(false);
			//写入参数			
			String[] sqlitems = sqlstr.split(";");
			int updatecount = 0;//影响行数
			ArrayList<HashMap<String,Object>> resultdata= new ArrayList<HashMap<String,Object>>();   //保存数据
			int length = sqlitems.length;
			int begin = 0;
			for(int i = 0;i<sqlitems.length;i++){
				String sql = sqlitems[i];
				if(sql == null || sql.trim().equals("")){
					continue;
				}
				
				int countt = countStrInString(sql,"?");
				HashMap<String, Object> tt = prepareParam1(conn,sql,params.subList(begin, begin + countt));
				PreparedStatement stmt = (PreparedStatement)tt.get("stmt");
				sqllist.add(tt.get("sql") != null ? tt.get("sql").toString():"");
				paramlist.add(tt.get("param"));
				
				begin = countt;
				boolean hasResultSet = stmt.execute();
				
				if(!hasResultSet){
					int rowCount = stmt.getUpdateCount();
					updatecount += rowCount;
				}
				if(hasResultSet&&i == sqlitems.length -1){
					ResultSet rs = stmt.getResultSet();
					ResultSetMetaData data=rs.getMetaData(); 
					while(rs.next()){
						HashMap<String,Object> columdata = new HashMap<String, Object>();
						for(int j = 1 ; j<= data.getColumnCount() ; j++){  
							String columnName = data.getColumnName(j); 
							String columnTypeName=data.getColumnTypeName(j);
							//获得指定列的列值 
							Object columnValue = rs.getObject(j); 
							if(columnValue != null)
								columdata.put(columnName, columnValue);
							if(columnTypeName.equals("DATE")&&columnValue != null){
								Date date = (Timestamp)columnValue;
								columdata.put(columnName+"_STR", sdf.format(date));
							}
							if(columnTypeName.equals("CLOB")&&columnValue != null){
								CLOB clob = (oracle.sql.CLOB)columnValue;
								columdata.put(columnName, ConvertUtil.ClobToString(clob));
							}
							//处理空字段
							boolean containsblank = false;
							if(commonParam != null && commonParam.get("containsblank") != null)
								containsblank = Boolean.valueOf(commonParam.get("containsblank").toString());
							if(columnValue == null&&containsblank)
								columdata.put(columnName, "");
						}
						resultdata.add(columdata);
					}
				}
			}
			result.put("updatecount", updatecount);
			result.put("data", resultdata);
			result.put("sqllist", sqllist);
			result.put("paramlist", paramlist);
			//6、提交事务
		    conn.commit();
		    
			 // 恢复原提交状态 
			 conn.setAutoCommit(defaultCommit);
		     DBPoolManager.closeConn(conn);
		}catch(Exception e){
			conn.rollback();
			
			// 恢复原提交状态 
			conn.setAutoCommit(defaultCommit);
			DBPoolManager.closeConn(conn);
			e.printStackTrace();
			throw new MySqlException(e.getMessage(),sqllist);
		}
		
		return result;
	}
	
	
	
	/***
	 * 执行指定数据库连接的SQL查询语句
	 * @param linkstr String 数据库链接字符串
	 * @param usnm String 数据库连接用户名
	 * @param uspw String 数据库密码
	 * @param sqlstr String SQL语句  如果为空默认查询指定ID服务信息
	 * @param params HashMap<String,Object> 参数map，包含类型等信息
	 * @param commonParam HashMap<String,Object> 通用参数
	 * */
	public HashMap<String,Object> queryInsertSQL(
			int id,
			String sqlstr,
			ArrayList<HashMap<String,Object>> params,
			HashMap<String,Object> commonParam) throws Exception{
		HashMap<String,Object> result = new HashMap<String, Object>();
		
		Connection conn = null;
		conn = DBPoolManager.getConnect(id);
		boolean defaultCommit = conn.getAutoCommit();
		
		List<String> sqllist = new ArrayList<String>();
		List<Object> paramlist = new ArrayList<Object>();
		try{
			//1. 写入参数 201906
//			sqlstr = prepareParam(sqlstr,params);
			
			//2. 查询是否有中间变量
//			Pattern pattern = Pattern.compile("(\\&\\{)(\\w*)(\\})");    
//			Matcher matcher = pattern.matcher(sqlstr);  
//			List<String> templist = new ArrayList();  
//			while(matcher.find()){
//				String group = matcher.group(2);
//				templist.add(group);
//			}
			//3、开启事务
			conn.setAutoCommit(false);
			String[] sqlitems = sqlstr.split(";");
			int updatecount = 0;//影响行数
			ArrayList<HashMap<String,Object>> resultdata= new ArrayList<HashMap<String,Object>>();   //保存数据
			int length = sqlitems.length;
			
			int begin = 0;
			for(int i = 0;i<sqlitems.length;i++){
				String sql = sqlitems[i];
				if(sql == null || sql.trim().equals("")){
					continue;
				}
				
				int count = countStrInString(sql,"?");
				HashMap<String, Object> tt = prepareParam1(conn,sql,params.subList(begin, begin + count));
				PreparedStatement stmt = (PreparedStatement)tt.get("stmt");
				sqllist.add(tt.get("sql") != null ? tt.get("sql").toString():"");
				paramlist.add(tt.get("param"));
				
				begin += count;
				boolean hasResultSet = stmt.execute();
				//4. 更新数量
				if(!hasResultSet){
					int rowCount = stmt.getUpdateCount();
					updatecount += rowCount;
				}
				//5. 最终结果
				if(hasResultSet&&i == sqlitems.length -1){
					ResultSet rs = stmt.getResultSet();
					ResultSetMetaData data=rs.getMetaData(); 
					while(rs.next()){
						HashMap<String,Object> columdata = new HashMap<String, Object>();
						for(int j = 1 ; j<= data.getColumnCount() ; j++){  
							String columnName = data.getColumnName(j); 
							String columnTypeName=data.getColumnTypeName(j);
							//6。 获得指定列的列值 
							Object columnValue = rs.getObject(j); 
							if(columnValue != null)
								columdata.put(columnName, columnValue);
							if(columnTypeName.equals("DATE")&&columnValue != null){
								Date date = (Timestamp)columnValue;
								columdata.put(columnName+"_STR", sdf.format(date));
							}
							if(columnTypeName.equals("CLOB")&&columnValue != null){
								CLOB clob = (oracle.sql.CLOB)columnValue;
								columdata.put(columnName, ConvertUtil.ClobToString(clob));
							}
							//7. 处理空字段
							boolean containsblank = false;
							if(commonParam != null && commonParam.get("containsblank") != null)
								containsblank = Boolean.valueOf(commonParam.get("containsblank").toString());
							if(columnValue == null&&containsblank)
								columdata.put(columnName, "");
						}
						resultdata.add(columdata);
					}
				}else if(hasResultSet){//8. 中间变量
					if(true){//templist.size() > 1
						ResultSet rs = stmt.getResultSet();
						ResultSetMetaData data=rs.getMetaData(); 
//						List<HashMap<String, Object>> temparamlist = new ArrayList<HashMap<String,Object>>();
						//注入中间参数
						HashMap<String,String> sqltempparam = new HashMap<String, String>();
						while(rs.next()){
							HashMap<String,Object> temdata = new HashMap<String, Object>();
							for(int j = 1 ; j<= data.getColumnCount() ; j++){
								String columnName = data.getColumnName(j).toUpperCase();
								String columnTypeName=data.getColumnTypeName(j);
								//获得指定列的列值 
								Object columnValue = rs.getObject(j); 
								if(columnValue != null){
									if(columnTypeName.equals("CHAR")
										||columnTypeName.equals("VARCHAR")
										||columnTypeName.equals("NVARCHAR")
										||columnTypeName.equals("NCHAR")){
										if(sqltempparam.get(columnName) == null || sqltempparam.get(columnName).toString().equals("")){
											sqltempparam.put(columnName, "'"+columnValue.toString()+"'");
										}else{
											sqltempparam.put(columnName, sqltempparam.get(columnName).toString() + ",'"+columnValue.toString()+"'");
										}
//										temdata.put(columnName, columnValue);
									}else if(columnTypeName.equals("DOUBLE")
										||columnTypeName.equals("BIGINT")
										||columnTypeName.equals("FLOAT")
										||columnTypeName.equals("INTEGER")
										||columnTypeName.equals("NUMREIC")){
										if(sqltempparam.get(columnName) == null || sqltempparam.get(columnName).toString().equals("")){
											sqltempparam.put(columnName, ""+columnValue.toString()+"");
										}else{
											sqltempparam.put(columnName, sqltempparam.get(columnName).toString() + ","+columnValue.toString()+"");
										}
//										temdata.put(columnName, columnValue);
									}else if(columnTypeName.equals("DATE")){
										if(sqltempparam.get(columnName) == null || sqltempparam.get(columnName).toString().equals("")){
											sqltempparam.put(columnName, "'"+sdf.format((Timestamp)columnValue)+"'");
										}else{
											sqltempparam.put(columnName, sqltempparam.get(columnName).toString() + ",'"+sdf.format((Timestamp)columnValue)+"'");
										}
									}else{
										throw new RuntimeException("中间变量不支持的类型转递！"+columnTypeName); 
									}
								}
							}
						}
						for(int k = i+1;k<sqlitems.length;k++){
							String sqltemp = sqlitems[k];
							Pattern pattern = Pattern.compile("(\\&\\{)(\\w*)(\\})");    
							Matcher matcher = pattern.matcher(sqltemp);
							while(matcher.find()){
								String group = matcher.group(2);
								if(sqltempparam.get(group.toUpperCase()) != null){
									sqlitems[k] = sqltemp.replace("&{"+group+"}", sqltempparam.get(group.toUpperCase()));
								}
							}
						}
					}
				}
			}
			result.put("updatecount", updatecount);
			result.put("data", resultdata);
			result.put("sqllist", sqllist);
			result.put("paramlist", paramlist);
			//6、提交事务
		    conn.commit();
		    
		    // 恢复原提交状态 
		 	conn.setAutoCommit(defaultCommit);
		 	DBPoolManager.closeConn(conn);
		}catch(Exception e){
			//7、出错回滚事务
			conn.rollback();
			// 恢复原提交状态
			conn.setAutoCommit(defaultCommit);
			DBPoolManager.closeConn(conn); 

			e.printStackTrace();
			throw new MySqlException(e.getMessage(),sqllist);
		}
		
		return result;
	}
	
	/***
	 * 执行指定数据库连接的存储过程语句
	 * @param id String 数据库ID
	 * @param sqlstr String SQL语句  如果为空默认查询指定ID服务信息
	 * @param params HashMap<String,Object> 参数map，包含类型等信息
	 * @param commonParam HashMap<String,Object> 通用参数
	 * */
	public HashMap<String,Object> queryProcessSQL(
			int id,
			String sqlstr,
			ArrayList<HashMap<String,Object>> params,
			HashMap<String,Object> commonParam) throws Exception{
		HashMap<String,Object> result = new HashMap<String, Object>();
		HashMap<String,Object> r = new HashMap<String, Object>();
		Object pageinfo = null;
		int count = countStrInString(sqlstr,"?");
		if(count != params.size()){
			throw new RuntimeException("参数数量不匹配"); 
		}
		Connection conn = null;
		//1.获取链接对象
		conn = DBPoolManager.getConnect(id);
		
		List<String> sqllist = new ArrayList<String>();
		List<Object> paramlist = new ArrayList<Object>();
		try{	
			sqlstr = sqlstr.trim();
			if(sqlstr.trim().endsWith(";")){
				sqlstr = sqlstr.substring(0, sqlstr.length()-1);
			}
			// 2. 执行语句
			CallableStatement callableStatement = conn.prepareCall(sqlstr);
			sqllist.add(sqlstr);
			paramlist.add(params);
			// 3、设置参数
			// 输出参数
			ArrayList<HashMap<String, Object>> outparams = new ArrayList<HashMap<String,Object>>();
			//Druid connect 转jdbc connect
			DruidNativeJdbcExtractor druidNativeJdbcExtractor = new DruidNativeJdbcExtractor();
			OracleConnection oraconn = (OracleConnection) druidNativeJdbcExtractor.getNativeConnection(callableStatement.getConnection());
			
			for(int i = 1;i<=params.size();i++){
				HashMap<String,Object> item = params.get(i-1);
				String type = item.get("type").toString().toUpperCase();
				String inout = item.get("inout").toString().toUpperCase();
				if(inout.equalsIgnoreCase("IN")){
					if(type.equalsIgnoreCase("KEYW")){
						if(item.get("value").toString().equalsIgnoreCase("null")){
							callableStatement.setString(i, "");
						}else{
							throw new RuntimeException("存储过程暂不支持输入数据类型："+type+"，值："+item.get("value").toString()+",请凉席管理员解决！"); 
						}
					}else if(type.equalsIgnoreCase("STRING")
							||type.equalsIgnoreCase("UUID")
							||type.equalsIgnoreCase("USCODE")
							||type.equalsIgnoreCase("SYSDATE")
							||type.equalsIgnoreCase("FILE")
							||type.equalsIgnoreCase("FILE[]")
							||type.equalsIgnoreCase("ENCRYPT")){
						
						String value = item.get("value").toString();
						if(/*value.indexOf("'") != -1 
								|| value.indexOf("=") != -1 
								|| */value.toLowerCase().indexOf("insert") != -1
								|| value.toLowerCase().indexOf("update") != -1
								|| value.toLowerCase().indexOf("select") != -1
								|| value.toLowerCase().indexOf("grant") != -1
								|| value.toLowerCase().indexOf("delete") != -1
								|| value.toLowerCase().indexOf("where") != -1){
							throw new Exception("参数不合法，请检查");
						}
						callableStatement.setString(i, value);
					}else if(type.equalsIgnoreCase("ENCODE")){
						String encodeStr = item.get("value").toString();
				    	String decodeStr = URLDecoder.decode(encodeStr);
				    	
						callableStatement.setString(i, decodeStr);
					}else if(type.equalsIgnoreCase("INT")){
						callableStatement.setObject(i, item.get("value").toString()==""?null:Integer.parseInt(item.get("value").toString()));
						//callableStatement.setInt(i, null);
					}else if(type.equalsIgnoreCase("FLOAT")){
						callableStatement.setObject(i, item.get("value").toString()==""?null:Float.parseFloat(item.get("value").toString()));
					}else if(type.equalsIgnoreCase("DATE")){
						//处理UTC时间
						SimpleDateFormat jsDateSdf1 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
						SimpleDateFormat jsDateSdf2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
						
						String dateStr = item.get("value").toString();
						jsDateSdf1.setTimeZone(TimeZone.getTimeZone("UTC"));
						if(dateStr.indexOf("T") == -1)
							jsDateSdf1 = jsDateSdf2;
						Date d = jsDateSdf1.parse(dateStr);
						
						callableStatement.setDate(i, (java.sql.Date)d);
					}else if(type.equalsIgnoreCase("LONG")){
						callableStatement.setObject(i, item.get("value").toString()==""?null:Long.parseLong(item.get("value").toString()));
						//callableStatement.setLong(i, Long.parseLong(item.get("value").toString()));
					}else if(type.equalsIgnoreCase("DOUBLE")){
						callableStatement.setObject(i, item.get("value").toString()==""?null:Double.parseDouble(item.get("value").toString()));
						//callableStatement.setDouble(i, Double.parseDouble(item.get("value").toString()));
					}else if(type.equalsIgnoreCase("STRING[]")){
						ArrayList arr = (ArrayList)item.get("value");
						
						ArrayDescriptor desc2 = ArrayDescriptor.createDescriptor("STRARR", oraconn);
						String[] param = new String[arr.size()];
						arr.toArray(param);
						ARRAY sqlStrsArr = new ARRAY(desc2, oraconn, param);
						callableStatement.setArray(i, sqlStrsArr);
					}else if(type.equalsIgnoreCase("INT[]")){
						ArrayList arr = (ArrayList)item.get("value");
						
						ArrayDescriptor desc2 = ArrayDescriptor.createDescriptor("INTARR", oraconn);
						Integer[] param = new Integer[arr.size()];
						arr.toArray(param);
						ARRAY sqlStrsArr = new ARRAY(desc2, oraconn, param);
						callableStatement.setArray(i, sqlStrsArr);
					}else if(type.equalsIgnoreCase("FLOAT[]")){
						ArrayList arr = (ArrayList)item.get("value");
						
						ArrayDescriptor desc2 = ArrayDescriptor.createDescriptor("FLOATARR", oraconn);
						Float[] param = new Float[arr.size()];
						arr.toArray(param);
						ARRAY sqlStrsArr = new ARRAY(desc2, oraconn, param);
						callableStatement.setArray(i, sqlStrsArr);
					}else if(type.equalsIgnoreCase("LONG[]")){
						ArrayList arr = (ArrayList)item.get("value");
						
						ArrayDescriptor desc2 = ArrayDescriptor.createDescriptor("LONGARR", oraconn);
						Long[] param = new Long[arr.size()];
						arr.toArray(param);
						ARRAY sqlStrsArr = new ARRAY(desc2, oraconn, param);
						callableStatement.setArray(i, sqlStrsArr);
					}else if(type.equalsIgnoreCase("DOUBLE[]")){
						ArrayList arr = (ArrayList)item.get("value");
						
						ArrayDescriptor desc2 = ArrayDescriptor.createDescriptor("DOUBLEARR", oraconn);
						Double[] param = new Double[arr.size()];
						arr.toArray(param);
						ARRAY sqlStrsArr = new ARRAY(desc2, oraconn, param);
						callableStatement.setArray(i, sqlStrsArr);
					}else{
						throw new RuntimeException("存储过程暂不支持输入数据类型："+type+",请联系管理员解决！"); 
					}
				}else{
					int oracletype = oracle.jdbc.OracleTypes.VARCHAR;
					String dtype = "";
					if(type.equalsIgnoreCase("KEYW")){
						oracletype = oracle.jdbc.OracleTypes.VARCHAR;
					}else if(type.equalsIgnoreCase("STRING")){
						oracletype = oracle.jdbc.OracleTypes.VARCHAR;
					}else if(type.equalsIgnoreCase("SQL")){
						oracletype = oracle.jdbc.OracleTypes.CLOB;
					}else if(type.equalsIgnoreCase("INT")){
						oracletype = oracle.jdbc.OracleTypes.INTEGER;
					}else if(type.equalsIgnoreCase("FLOAT")){
						oracletype = oracle.jdbc.OracleTypes.FLOAT;
					}else if(type.equalsIgnoreCase("DATE")){
						oracletype = oracle.jdbc.OracleTypes.DATE;
					}else if(type.equalsIgnoreCase("LONG")){
						oracletype = oracle.jdbc.OracleTypes.BIGINT;
					}else if(type.equals("DOUBLE")){
						oracletype = oracle.jdbc.OracleTypes.DOUBLE;
					}else if(type.equalsIgnoreCase("STRING[]")){
						oracletype = oracle.jdbc.OracleTypes.ARRAY;
						dtype = "STRARR";
					}else if(type.equalsIgnoreCase("INT[]")){
						oracletype = oracle.jdbc.OracleTypes.ARRAY;
						dtype = "INTARR";
					}else if(type.equalsIgnoreCase("FLOAT[]")){
						oracletype = oracle.jdbc.OracleTypes.ARRAY;
						dtype = "FLOATARR";
					}else if(type.equalsIgnoreCase("LONG[]")){
						oracletype = oracle.jdbc.OracleTypes.ARRAY;
						dtype = "LONGARR";
					}else if(type.equalsIgnoreCase("DOUBLE[]")){
						oracletype = oracle.jdbc.OracleTypes.ARRAY;
						dtype = "DOUBLEARR";
					}else if(type.equalsIgnoreCase("CURSOR")){
						oracletype = oracle.jdbc.OracleTypes.CURSOR;
					}else{
						throw new RuntimeException("存储过程暂不支持输出数据类型："+type+",请联系管理员解决！"); 
					}
					if(dtype.equals(""))
						callableStatement.registerOutParameter(i, oracletype);
					else
						callableStatement.registerOutParameter(i, oracletype,dtype);
			        item.put("index", i);
			        outparams.add(item);
				}
			}
			
			//4、执行
	        callableStatement.execute();
	        
	        //5、获取数据
	        for(int i = 1;i<=outparams.size();i++){
	        	HashMap<String,Object> item = outparams.get(i-1);
	        	
	        	String name = item.get("name").toString();
	        	String type = item.get("type").toString();
	        	int index = Integer.parseInt(item.get("index").toString());
	        	Object value = null;
	        	if(type.equalsIgnoreCase("STRING")||type.equalsIgnoreCase("KEYW")){
	        		value = callableStatement.getObject(index);
				}else if(type.equalsIgnoreCase("SQL")){
					Object tvalue = callableStatement.getObject(index);
					CLOB clob = (CLOB)tvalue;
					value = ConvertUtil.ClobToString(clob);
				}else if(type.equalsIgnoreCase("INT")){
					value = callableStatement.getInt(index);
				}else if(type.equalsIgnoreCase("FLOAT")){
					value = callableStatement.getFloat(index);
				}else if(type.equalsIgnoreCase("DATE")){
					value = (Date)callableStatement.getDate(index);
				}else if(type.equalsIgnoreCase("LONG")){
					value = callableStatement.getLong(index);
				}else if(type.equalsIgnoreCase("DOUBLE")){
					value = callableStatement.getDouble(index);
				}else if(type.equalsIgnoreCase("STRING[]")){
					value = (String[])callableStatement.getArray(index).getArray();
				}else if(type.equalsIgnoreCase("INT[]")){
					value = (Integer[])callableStatement.getArray(index).getArray();
				}else if(type.equalsIgnoreCase("FLOAT[]")){
					value = (Float[])callableStatement.getArray(index).getArray();
				}else if(type.equalsIgnoreCase("LONG[]")){
					value = (Long[])callableStatement.getArray(index).getArray();
				}else if(type.equalsIgnoreCase("DOUBLE[]")){
					value = (Double[])callableStatement.getArray(index).getArray();
				}else if(type.equalsIgnoreCase("CURSOR")){
					ArrayList<HashMap<String,Object>> resultdata= new ArrayList<HashMap<String,Object>>();   //保存数据
					ResultSet rs = (ResultSet)callableStatement.getObject(index);
					ResultSetMetaData data=rs.getMetaData(); 
					while(rs.next()){
						HashMap<String,Object> columdata = new HashMap<String, Object>();
						for(int j = 1 ; j<= data.getColumnCount() ; j++){  
							String columnName = data.getColumnName(j); 
							String columnTypeName=data.getColumnTypeName(j); 
							Object columnValue = rs.getObject(j); 
							if(columnValue != null)
								columdata.put(columnName, columnValue);
							if(columnTypeName.equals("DATE")&&columnValue != null){
								Date date = (Timestamp)columnValue;
								columdata.put(columnName+"_STR", sdf.format(date));
							}
							if(columnTypeName.equals("CLOB")&&columnValue != null){
								CLOB clob = (oracle.sql.CLOB)columnValue;
								columdata.put(columnName, ConvertUtil.ClobToString(clob));
							}
							//7. 处理空字段
							boolean containsblank = false;
							if(commonParam != null && commonParam.get("containsblank") != null)
								containsblank = Boolean.valueOf(commonParam.get("containsblank").toString());
							if(columnValue == null&&containsblank)
								columdata.put(columnName, "");
						}
						resultdata.add(columdata);
					}
					value = resultdata;
				}else{
					throw new RuntimeException("存储过程暂不支持输出数据类型："+type+",请联系管理员解决！"); 
				}
	        	
	        	if(!type.equalsIgnoreCase("SQL")){
	        		r.put(name, value);
	        	}else{
	        		String returnSql = value.toString();
	        		if(returnSql.toUpperCase().indexOf("AUTH.") == -1){
	        			returnSql = returnSql;
	        		}else{
	        			HashMap<String, Object> u = (HashMap<String, Object>)commonParam.get("user");
		        		if(u == null || u.get("US_LNNM")==null){
		        			throw new RequireLoginException("服务需要登陆！");
		        		}
						if (!"admin".equals(u.get("US_LNNM").toString().trim())){
							List<Integer> authList = CacheTask.serverAuthRefs.get(Integer.valueOf(commonParam.get("DS_CODE").toString()));
							if(authList == null)
								authList = new ArrayList<Integer>();
							for (Integer integer : authList) {
								HashMap<String,Object> qxitem = CacheTask.authInfos.get(integer);
								Pattern p = Pattern.compile( "(\\s|,)"+"auth."+"(" + qxitem.get("PSD_DUSNM") + ".){0,1}" + qxitem.get("PSD_TNAME").toString()+"(\\s|,)",Pattern.CASE_INSENSITIVE);
								Matcher m = p.matcher(returnSql);
								if (m.find()){
									String fstr = m.group();
									if (fstr.startsWith(",") || fstr.startsWith(" "))
										fstr = fstr.substring(1);
									if (fstr.endsWith(",") || fstr.endsWith(" "))
										fstr = fstr.substring(0, fstr.length() - 1);
									String qxsql = qxitem.get("PSD_VALUESQL").toString();
									qxsql = qxsql.replace("#{lnnm}", "'"+u.get("US_LNNM").toString()+"'");
									returnSql = returnSql.replace(fstr, "(select * from "+fstr.replaceAll("(?i)auth.", "")+" where "+qxitem.get("PSD_FIELD").toString()+" in ("+qxsql+"))");
								}
							}
						}else{
							returnSql = returnSql.replaceAll("(?i)auth.", "");
						}
	        		}
	        		
	        		HashMap<String, Object> re = querySQL(id,returnSql,new ArrayList<HashMap<String,Object>>(),commonParam);
	        		Object data = re.get("data");
	        		pageinfo = re.get("pageinfo");
	        		List<String> sql = (List<String>)re.get("sqllist");
	        		List<Object> param = (List<Object>)re.get("paramlist");
	        		
	        		sqllist.addAll(sql);
	        		paramlist.addAll(param);
	        		r.put(name, data);
	        	}
	        }
	        DBPoolManager.closeConn(conn);
		}catch (Exception e) {
			e.printStackTrace();
			DBPoolManager.closeConn(conn);
			
			if(e instanceof MySqlException)
				throw new MySqlException(e.getMessage(),sqllist);
			if(e instanceof RequireLoginException)
				throw new RequireLoginException(e.getMessage());
		}
		
		result.put("updatecount", 0);
		result.put("data", r);
		result.put("pageinfo", pageinfo);
		result.put("sqllist", sqllist);
		result.put("paramlist", paramlist);
		return result;
	}
	
	/**
	 * 
	 * 
	 * */
	private HashMap<String, Object> prepareParam1(Connection conn,String sqlstr,List<HashMap<String, Object>> params) throws Exception{
		HashMap<String, Object> result = new HashMap<String, Object>();
		
		sqlstr = sqlstr.trim();
		if(sqlstr.endsWith(";")){
			sqlstr = sqlstr.substring(0, sqlstr.length()-1);
		}

		//解析数组
		ArrayList<HashMap<String, Object>> newparams = new ArrayList<HashMap<String,Object>>();
		for(int i = 0;i<params.size();i++){
			HashMap<String,Object> item = params.get(i);
			String ttype = item.get("type").toString().toUpperCase();
			String tname = item.get("name").toString();
			
			if(ttype.equals("STRING[]") || ttype.equals("INT[]") || ttype.equals("LONG[]") || ttype.equals("FLOAT[]") 
					|| ttype.equals("DOUBLE[]") || ttype.equals("DATE[]")){
				Object obj1 = item.get("value");
		    	Object[] filestrArr = null;
		    	if(obj1 instanceof ArrayList){
		    		ArrayList arr = (ArrayList)obj1;
		    		filestrArr = new Object[arr.size()];
		    		arr.toArray(filestrArr);
		    	}else{
		    		String filestr = (String)obj1.toString();
		    		filestrArr = filestr.split(",");
		    	}
		    	
		    	sqlstr = sqlstr.replaceFirst("\\?", getStr(filestrArr.length,",#temphold#").substring(1));
		    	for(int k = 0;k < filestrArr.length;k++){
		    		String itemtype = ttype.replace("[]", "");
		    		
		    		HashMap<String, Object> p = new HashMap<String, Object>();
		    		p.put("type", itemtype);
		    		if(itemtype.equals("STRING")){
		    			p.put("value", filestrArr[k] == null ? null:filestrArr[k].toString());
		    		}else if(itemtype.equals("INT")){
		    			p.put("value", filestrArr[k] == null ? null:Integer.parseInt(filestrArr[k].toString()));
		    		}else if(itemtype.equals("LONG")){
		    			p.put("value", filestrArr[k] == null ? null:Integer.parseInt(filestrArr[k].toString()));
		    		}else if(itemtype.equals("FLOAT")){
		    			p.put("value", filestrArr[k] == null ? null:Float.parseFloat(filestrArr[k].toString()));
		    		}else if(itemtype.equals("DOUBLE")){
		    			p.put("value", filestrArr[k] == null ? null:Double.parseDouble(filestrArr[k].toString()));
		    		}else if(itemtype.equals("DATE")){
		    			//处理UTC时间
						SimpleDateFormat jsDateSdf1 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
						SimpleDateFormat jsDateSdf2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
						String dateStr = filestrArr[k].toString();
						jsDateSdf1.setTimeZone(TimeZone.getTimeZone("UTC"));
						if(dateStr.indexOf("T") == -1)
							jsDateSdf1 = jsDateSdf2;
						Date d = jsDateSdf1.parse(dateStr);
		    			
		    			p.put("value", d == null ? null:d);
		    		}
					
					p.put("inout", "IN");
					p.put("name", tname);
					newparams.add(p);
		    	}
				continue;
			}else if(item.get("type").toString().toUpperCase().equals("KEYW")){
				if(item.get("value").toString().indexOf("'") == -1 
					&& item.get("value").toString().indexOf("=") == -1 
					&& item.get("value").toString().indexOf("@") == -1
					&& item.get("value").toString().indexOf("#") == -1
					&& item.get("value").toString().indexOf("(") == -1
					&& item.get("value").toString().indexOf(";") == -1
					&& item.get("value").toString().indexOf("\"") == -1
					&& item.get("value").toString().toLowerCase().indexOf("insert") == -1
					&& item.get("value").toString().toLowerCase().indexOf("update") == -1
					&& item.get("value").toString().toLowerCase().indexOf("select") == -1
					&& item.get("value").toString().toLowerCase().indexOf("grant") == -1
					&& item.get("value").toString().toLowerCase().indexOf("delete") == -1
					&& item.get("value").toString().toLowerCase().indexOf("where") == -1){
				
					sqlstr = sqlstr.replaceFirst("\\?", item.get("value").toString()+"");
					continue;
				}else{
					throw new RuntimeException("参数不合法！...");
				}
			}else{
				newparams.add(params.get(i));
				sqlstr = sqlstr.replaceFirst("\\?", "#temphold#");
				continue;
			}
		}
		
		params = newparams;
		sqlstr = sqlstr.replaceAll("#temphold#", "?");
		
		result.put("sql", sqlstr);
		result.put("param", params);
		
		PreparedStatement pstmt = conn.prepareStatement(sqlstr);
		for(int i = 1;i<=params.size();i++){
			HashMap<String,Object> item = params.get(i-1);
			if(item.get("type").toString().equalsIgnoreCase("STRING")
					||item.get("type").toString().toUpperCase().equals("UUID")
					||item.get("type").toString().toUpperCase().equals("USCODE")
					||item.get("type").toString().toUpperCase().equals("SYSDATE")
					||item.get("type").toString().equalsIgnoreCase("FILE")
					||item.get("type").toString().equalsIgnoreCase("FILE[]")
					||item.get("type").toString().equalsIgnoreCase("ENCRYPT")){
				
				pstmt.setString(i, item.get("value").toString());
				continue;
			}else if(item.get("type").toString().equalsIgnoreCase("LIKE")){
				pstmt.setString(i, "%"+item.get("value").toString()+"%");
			}else if(item.get("type").toString().equalsIgnoreCase("LLIKE")){
				pstmt.setString(i, "%"+item.get("value").toString());
			}else if(item.get("type").toString().equalsIgnoreCase("RLIKE")){
				pstmt.setString(i, item.get("value").toString()+"%");
			}else if(item.get("type").toString().toUpperCase().equals("ENCODE")){
				String encodeStr = item.get("value").toString();
		    	String decodeStr = URLDecoder.decode(encodeStr);
		    			    	
		    	pstmt.setString(i, decodeStr);
		    	continue;
			}else if(item.get("type").toString().toUpperCase().equals("INT")){				
				pstmt.setInt(i, Integer.parseInt(item.get("value").toString()));
				continue;
			}else if(item.get("type").toString().toUpperCase().equals("LONG")){
				pstmt.setLong(i, Long.parseLong(item.get("value").toString()));
				continue;
			}else if(item.get("type").toString().toUpperCase().equals("FLOAT")){
				pstmt.setFloat(i, Float.parseFloat(item.get("value").toString()));
				continue;
			}else if(item.get("type").toString().toUpperCase().equals("DOUBLE")){
				pstmt.setDouble(i, Double.parseDouble(item.get("value").toString()));
				continue;
			}else if(item.get("type").toString().toUpperCase().equals("DATE")){
				//处理UTC时间
				SimpleDateFormat jsDateSdf1 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
				SimpleDateFormat jsDateSdf2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				
				String dateStr = item.get("value").toString();
				jsDateSdf1.setTimeZone(TimeZone.getTimeZone("UTC"));
				if(dateStr.indexOf("T") == -1)
					jsDateSdf1 = jsDateSdf2;
				Date d = jsDateSdf1.parse(dateStr);
				
				pstmt.setDate(i, (java.sql.Date)d);
				continue;
			}else{
				throw new RuntimeException("未知类型！"+item.get("type").toString());
			}
		}
		result.put("stmt", pstmt);
		
		return result;
	}
	
	/**
	 * 
	 * 
	 * */
//	private String prepareParam(String sqlstr,ArrayList<HashMap<String, Object>> params) throws Exception{
//		sqlstr = sqlstr.trim();
//		if(sqlstr.endsWith(";")){
//			sqlstr = sqlstr.substring(0, sqlstr.length()-1);
//		}
//		HashMap<String, String> finalreplace = new HashMap<String, String>();
//		
//		for(int i = 0;i<params.size();i++){
//			HashMap<String,Object> item = params.get(i);
//			if(item.get("type").toString().equalsIgnoreCase("STRING")
//					||item.get("type").toString().toUpperCase().equals("UUID")
//					||item.get("type").toString().toUpperCase().equals("USCODE")
//					||item.get("type").toString().toUpperCase().equals("SYSDATE")
//					||item.get("type").toString().equalsIgnoreCase("FILE")
//					||item.get("type").toString().equalsIgnoreCase("FILE[]")
//					||item.get("type").toString().equalsIgnoreCase("ENCRYPT")){
//				sqlstr = sqlstr.replaceFirst("\\?", "'"+item.get("value").toString()+"'");
//				continue;
//			}else if(item.get("type").toString().toUpperCase().equals("ENCODE")){
//				String encodeStr = item.get("value").toString();
//		    	String decodeStr = URLDecoder.decode(encodeStr);
//		    	
//		    	sqlstr = sqlstr.replaceFirst("\\?", "&#{"+i+"}#&");
//		    	finalreplace.put("&#{"+i+"}#&", "'"+decodeStr+"'");
//			}else if(item.get("type").toString().toUpperCase().equals("STRING[]")){
//				Object obj1 = item.get("value");
//		    	String[] filestrArr = null;
//		    	if(obj1 instanceof ArrayList){
//		    		ArrayList arr = (ArrayList)obj1;
//		    		filestrArr = new String[arr.size()];
//		    		arr.toArray(filestrArr);
//		    	}else{
//		    		String filestr = (String)obj1;
//		    		filestrArr = filestr.split(",");
//		    	}
//				
//				sqlstr = sqlstr.replaceFirst("\\?", "'"+StringUtils.join(filestrArr, "','")+"'");
//				continue;
//			}else if(item.get("type").toString().toUpperCase().equals("INT")){
//				sqlstr = sqlstr.replaceFirst("\\?", Integer.parseInt(item.get("value").toString())+"");
//				continue;
//			}else if(item.get("type").toString().toUpperCase().equals("INT[]")){
//				Object obj1 = item.get("value");
//				Integer[] filestrArr = null;
//		    	if(obj1 instanceof ArrayList){
//		    		ArrayList arr = (ArrayList)obj1;
//		    		filestrArr = new Integer[arr.size()];
//		    		arr.toArray(filestrArr);
//		    		
//		    		sqlstr = sqlstr.replaceFirst("\\?", ""+StringUtils.join(filestrArr, ",")+"");
//		    	}else{
//		    		String filestr = obj1.toString();		    		
//		    		sqlstr = sqlstr.replaceFirst("\\?", ""+filestr+"");
//		    	}
//				continue;
//			}else if(item.get("type").toString().toUpperCase().equals("LONG")){
//				sqlstr = sqlstr.replaceFirst("\\?", Long.parseLong(item.get("value").toString())+"");
//				continue;
//			}else if(item.get("type").toString().toUpperCase().equals("LONG[]")){
//				Object obj1 = item.get("value");
//				Long[] filestrArr = null;
//		    	if(obj1 instanceof ArrayList){
//		    		ArrayList arr = (ArrayList)obj1;
//		    		filestrArr = new Long[arr.size()];
//		    		arr.toArray(filestrArr);
//		    		
//		    		sqlstr = sqlstr.replaceFirst("\\?", ""+StringUtils.join(filestrArr, ",")+"");
//		    	}else{
//		    		String filestr = obj1.toString();		    		
//		    		sqlstr = sqlstr.replaceFirst("\\?", ""+filestr+"");
//		    	}
//				continue;
//			}else if(item.get("type").toString().toUpperCase().equals("FLOAT")){
//				sqlstr = sqlstr.replaceFirst("\\?", Float.parseFloat(item.get("value").toString())+"");
//				continue;
//			}else if(item.get("type").toString().toUpperCase().equals("FLOAT[]")){
//				Object obj1 = item.get("value");
//				Float[] filestrArr = null;
//		    	if(obj1 instanceof ArrayList){
//		    		ArrayList arr = (ArrayList)obj1;
//		    		filestrArr = new Float[arr.size()];
//		    		arr.toArray(filestrArr);
//		    		
//		    		sqlstr = sqlstr.replaceFirst("\\?", ""+StringUtils.join(filestrArr, ",")+"");
//		    	}else{
//		    		String filestr = obj1.toString();		    		
//		    		sqlstr = sqlstr.replaceFirst("\\?", ""+filestr+"");
//		    	}
//				continue;
//			}else if(item.get("type").toString().toUpperCase().equals("DOUBLE")){
//				sqlstr = sqlstr.replaceFirst("\\?", Double.parseDouble(item.get("value").toString())+"");
//				continue;
//			}else if(item.get("type").toString().toUpperCase().equals("DOUBLE[]")){
//				Object obj1 = item.get("value");
//				Double[] filestrArr = null;
//		    	if(obj1 instanceof ArrayList){
//		    		ArrayList arr = (ArrayList)obj1;
//		    		filestrArr = new Double[arr.size()];
//		    		arr.toArray(filestrArr);
//		    		
//		    		sqlstr = sqlstr.replaceFirst("\\?", ""+StringUtils.join(filestrArr, ",")+"");
//		    	}else{
//		    		String filestr = obj1.toString();;		    		
//		    		sqlstr = sqlstr.replaceFirst("\\?", ""+filestr+"");
//		    	}
//				continue;
//			}else if(item.get("type").toString().toUpperCase().equals("DATE")){
//				//处理UTC时间
//				SimpleDateFormat jsDateSdf1 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
//				SimpleDateFormat jsDateSdf2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//				
//				String dateStr = item.get("value").toString();
//				jsDateSdf1.setTimeZone(TimeZone.getTimeZone("UTC"));
//				if(dateStr.indexOf("T") == -1)
//					jsDateSdf1 = jsDateSdf2;
//				java.util.Date d = jsDateSdf1.parse(dateStr);
//				
//				sqlstr = sqlstr.replaceFirst("\\?", "to_date('"+(sdf.format(d))+"','yyyy-MM-dd hh24:mi:ss')");
//				continue;
//			}else if(item.get("type").toString().toUpperCase().equals("DATE[]")){
//				Object obj1 = item.get("value");
//		    	String[] tms = null;
//		    	if(obj1 instanceof ArrayList){
//		    		ArrayList arr = (ArrayList)obj1;
//		    		tms = new String[arr.size()];
//		    		arr.toArray(tms);
//		    	}else{
//		    		String filestr = (String)obj1;
//		    		tms = filestr.split(",");
//		    	}
//				
//				//处理UTC时间
//				String psql = "";
//				for(int j = 0;j<tms.length;j++){
//					SimpleDateFormat jsDateSdf1 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
//					SimpleDateFormat jsDateSdf2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//					
//					String dateStr = tms[j];
//					jsDateSdf1.setTimeZone(TimeZone.getTimeZone("UTC"));
//					if(dateStr.indexOf("T") == -1)
//						jsDateSdf1 = jsDateSdf2;
//					java.util.Date d = jsDateSdf1.parse(dateStr);
//					
//					psql = psql+",to_date("+(sdf.format(d))+" 'yyyy-MM-dd hh24:mi:ss')";
//				}
//				if(psql == ""){
//					throw new RuntimeException("参数错误！");
//				}
//				sqlstr = sqlstr.replaceFirst("\\?", psql.substring(1, psql.length()-1));
//				continue;
//			}else if(item.get("type").toString().toUpperCase().equals("KEYW")){
//				sqlstr = sqlstr.replaceFirst("\\?", item.get("value").toString()+"");
//				continue;
//			}else{
//				throw new RuntimeException("未知类型！"+item.get("type").toString());
//			}
//		}
//		
//		Iterator iter = finalreplace.entrySet().iterator();
//		while (iter.hasNext()) {
//			Map.Entry entry = (Map.Entry) iter.next();
//			Object key = entry.getKey();
//			Object val = entry.getValue();
//			
//			sqlstr = sqlstr.replace(key.toString(), val.toString());
//		}
//		System.out.println(sqlstr);
//		return sqlstr;
//	}
		
	
	/**
     * 通过正则表达式的方式获取字符串中指定字符的个数
     * @param text 指定的字符串
     * @param str 待搜索字符
     * @return 指定字符的个数
     */
    private int countStrInString(String text,String str) {
        // 根据指定的字符构建正则
        Pattern pattern = Pattern.compile("/"+str);
        // 构建字符串和正则的匹配
        Matcher matcher = pattern.matcher(text);
        int count = 0;
        // 循环依次往下匹配
        while (matcher.find()){ // 如果匹配,则数量+1
            count++;
        }
        return  count;
    }
    
    
    public static String getStr(int num, String str){
    	StringBuffer sb = new StringBuffer("");
    	for(int i=0;i<num;i++){
    		sb.append(str);
    	}
    	return sb.toString();
    }

}
