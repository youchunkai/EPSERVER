/****
 * @description:  JDBC方式连接数据库
 * */
package com.connection;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;


@Component(value = "DBServerConn")
public class DBServerConn{
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(DBServerConn.class);
	
	private Connection conn;
	/****
	 * @description 通过连接字符串获取Oracle数据库JDBC连接
	 * @param linkstr 连接字符串    jdbc:oracle:thin:@DBIP:DBPort:DBName 如果为空默认支撑库
	 * @param usnm 用户名   如果为空默认支撑库用户名 
	 * @param uspw 密码   如果为空默认支撑库密码
	 * */
	public Connection getConn4OLinkStr(String linkstr,String usnm,String uspw) throws Exception {
		try {
			Class.forName("oracle.jdbc.OracleDriver").newInstance();

			conn = DriverManager.getConnection(linkstr, usnm, uspw);

			return conn;
		} catch (InstantiationException e) {
			e.printStackTrace();
			throw e;
		} catch (IllegalAccessException e) {
			e.printStackTrace();
			throw e;
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}
	
	/**
	 * 通过连接字符串获取SQLServer数据库连接
	 * @param linkstr 连接字符串    jdbc:sqlserver://DBIP:DBPort; DatabaseName=DBName
	 * @param usnm 用户名
	 * @param uspw 密码
	 * 
	 * */
	public Connection getConn4SLinkStr(String linkstr,String usnm,String uspw) {
		try {
			Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver").newInstance();
				
			conn = DriverManager.getConnection(linkstr, usnm, uspw);
			return conn;
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/****
	 * @description 关闭连接
	 * */
	public void closeConn(Connection conn) {
		try {
			if(!conn.isClosed())
				conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args){
		Connection conn =null;
		DBServerConn con = new DBServerConn();
		try{
			conn = con.getConn4OLinkStr("jdbc:oracle:thin:@117.78.60.174:8083/orcl","xadb","abc123");
		}catch(Exception e){
			e.printStackTrace();
		}
		if(con != null){
			logger.info("打开数据库连接成功");
			con.closeConn(conn);
			logger.info("关闭数据库连接成功");
		}
	}
}
