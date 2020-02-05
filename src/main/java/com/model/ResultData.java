package com.model;


public class ResultData {
	
	private String msg;
	private boolean result;
	private Object data;
	
	public ResultData(){}
	public ResultData(boolean _reuslt,String _msg){
		this.msg = _msg;
		this.result = _reuslt;
	}
	
	public String getMsg(){
		return msg;
	}
	
	public void setMsg(String msg){
		this.msg = msg;
	}
	
	public boolean getResult(){
		return result;
	}
	
	public void setResult(boolean result){
		this.result = result;
	}
	
	public Object getData(){
		return data;
	}
	
	public void setData(Object data){
		this.data = data;
	}


}
