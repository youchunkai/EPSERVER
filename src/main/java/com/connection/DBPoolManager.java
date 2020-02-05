package com.connection;

import com.alibaba.druid.pool.DruidDataSource;
import com.dao.mapper.XaMapper;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component(value = "DBPoolManager")
public class DBPoolManager implements InitializingBean,DisposableBean{
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(DBPoolManager.class);
	
	@Resource(name = "XaMapper")
	private XaMapper xaMapper;

	private static HashMap<Integer, DruidDataSource> datasmap = new HashMap<Integer, DruidDataSource>();
	
	@Override
	public void destroy(){
		for (Map.Entry<Integer, DruidDataSource> entry : datasmap.entrySet()) { 
			
			DruidDataSource ds = entry.getValue();
			ds.close();
			logger.info("关闭连接池连接:"+entry.getKey()+"-----------------------------");
		}  
	}
	
	@Override
	public void afterPropertiesSet() throws Exception {
		//查询数据库连接池初始化数据列表  获取所有数据库列表
		List<HashMap<String,Object>> datasources = xaMapper.queryDataSources();
		
		for(int i = 0;i<datasources.size();i++){
			HashMap<String,Object> item = datasources.get(i);
			
			DruidDataSource dataSource=new DruidDataSource();  
		    //设置连接参数  
			dataSource.setUrl(item.get("DTSR_LSTR").toString());
			dataSource.setDriverClassName(item.get("DTSR_DRIVER").toString());
			dataSource.setUsername(item.get("DTSR_USNM").toString());  
			dataSource.setPassword(item.get("DTSR_USPW").toString());  
			
			// 查看连接池初始化连接数是否为空
			if(item.get("DTSR_INITALNUM") != null && !item.get("DTSR_INITALNUM").toString().equals("0") && !item.get("DTSR_INITALNUM").toString().equals(""))
				dataSource.setInitialSize(Integer.parseInt(item.get("DTSR_INITALNUM").toString()));
			else
				dataSource.setInitialSize(5);//设置连接池初始化连接数为5
			
			if(item.get("DTSR_MAXNUM") != null && !item.get("DTSR_MAXNUM").toString().equals("0") && !item.get("DTSR_MAXNUM").toString().equals(""))
				dataSource.setMaxActive(Integer.parseInt(item.get("DTSR_MAXNUM").toString()));
			else
				dataSource.setMaxActive(50); //连接池最大连接数设置为50
			
			if(item.get("DTSR_MINNUM") != null && !item.get("DTSR_MINNUM").toString().equals("0") && !item.get("DTSR_MINNUM").toString().equals(""))
				dataSource.setMinIdle(Integer.parseInt(item.get("DTSR_MINNUM").toString()));
			else
				dataSource.setMinIdle(5);//连接池最小连接数设置为5
			
			if(item.get("DTSR_MAXIDLETM") != null && !item.get("DTSR_MAXIDLETM").toString().equals("0") && !item.get("DTSR_MAXIDLETM").toString().equals(""))
				dataSource.setMaxWait(Integer.parseInt(item.get("DTSR_MAXIDLETM").toString()));
			else
				dataSource.setMaxWait(40000);//链接最大空闲时间
			
			//当连接池用完时客户端调用getConnection()后等待获取新连接的时间，超时后将抛出SQLException,如设为0则无限期等待。单位毫秒
			dataSource.setTimeBetweenEvictionRunsMillis(60000);
			//是否缓存preparedStatement，也就是PSCache。PSCache对支持游标的数据库性能提升巨大，比如说oracle
			dataSource.setPoolPreparedStatements(true);
			//要启用PSCache，必须配置大于0，当大于0时，poolPreparedStatements自动触发修改为true
			dataSource.setMaxOpenPreparedStatements(100);
			
			dataSource.setValidationQuery("select 1 from dual");
			dataSource.setTestOnBorrow(false);
			dataSource.setTestOnReturn(false);
			dataSource.setTestWhileIdle(true);
			dataSource.setMinEvictableIdleTimeMillis(3600000);
			
			dataSource.setRemoveAbandoned(true);
			dataSource.setRemoveAbandonedTimeout(300);
			dataSource.setLogAbandoned(false);
			
			datasmap.put(Integer.parseInt(item.get("DTSR_CODE").toString()), dataSource);
			
			logger.info("*****************创建druid连接池连接："+item.get("DTSR_CODE").toString()+"-----------");
		}
		logger.info("*****************druid连接池初始化完成-----------");
	}
	
	public static Connection getConnect(int id) throws Exception{
		DruidDataSource dataSource = datasmap.get(id);
		Connection resultconnect = null;
		if(dataSource != null){
			resultconnect = dataSource.getConnection();
		}
		return resultconnect;
	}
	
	/****
	 * @description 关闭连接
	 * */
	public static void closeConn(Connection conn) {
		try {
			if(conn != null && !conn.isClosed())
				conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
