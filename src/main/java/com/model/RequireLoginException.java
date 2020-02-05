package com.model;

import java.util.List;

public class RequireLoginException extends RuntimeException {
	
	/**
     * 构造一个基本异常.
     *
     * @param message
     *            信息描述
     */
    public RequireLoginException(String message)
    {
        super(message);
    }
    
    public RequireLoginException(String message, List<String> sql)
    {
        super(message);
    }
}
