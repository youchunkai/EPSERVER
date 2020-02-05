package com.model;

import java.util.List;

public class MySqlException extends RuntimeException {
	
	private List<String> sql = null;
	/**
     * 构造一个基本异常.
     *
     * @param message
     *            信息描述
     */
    public MySqlException(String message)
    {
        super(message);
    }
    
    public MySqlException(String message, List<String> sql)
    {
        super(message);
        this.setSql(sql);
    }

	public List<String> getSql() {
		return sql;
	}

	public void setSql(List<String> sql) {
		this.sql = sql;
	}
}
