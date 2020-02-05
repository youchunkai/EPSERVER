package com.model;

/**
 * Desc:
 * Author:Kevin
 * Date:2019/11/10
 **/
public class ResponseVO<T> {

    private static final long serialVersionUID = -456412583671584738L;

    private int code;

    private String error;

    private boolean result;

    private T data;

    private PageInfo pageinfo;


    private ResponseVO(int code, String error, boolean result, T data, PageInfo pageinfo) {
        this.code = code;
        this.error = error;
        this.result = result;
        this.data = data;
        this.pageinfo = pageinfo;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public boolean isResult() {
        return result;
    }

    public void setResult(boolean result) {
        this.result = result;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public PageInfo getPageinfo() {
        return pageinfo;
    }

    public void setPageinfo(PageInfo pageinfo) {
        this.pageinfo = pageinfo;
    }

    /**
     * Desc 创建一个带返回值与分页信息的返回体
     * Param: data 返回的数据  page分页信息
     * @return ResponseVO 返回体
    */
    public static <T> ResponseVO<T> createOKWithDataWithPageinfo(String error,T data,PageInfo pageInfo){
        return new ResponseVO(ResponseCode.OK, error, true, data, pageInfo);
    }


    /**
     * Desc 创建一个带返回值 不带分页信息的返回体
     * Param: data 返回的数据  page分页信息
     * @return ResponseVO 返回体
     */
    public static <T> ResponseVO<T> createOKWithDataWithoutPageinfo(String error,T data){
        return createOKWithDataWithPageinfo(error,data, null);
    }


    /**
     * Desc 自定义返回体
     * Param: code 状态码  error 执行信息  result 执行结果  data 返回的数据  page分页信息
     * @return ResponseVO 返回体
     */
    public static <T> ResponseVO<T> create(int code, String error, boolean result, T data, PageInfo pageinfo) {
        return new ResponseVO<T>(code, error, result, data, pageinfo);
    }

    /**
     * Desc 自定义异常返回体
     * Param: code 状态码  error 执行信息  result 执行结果  data 返回的数据  page分页信息
     * @return ResponseVO 返回体
     */
    public static <T> ResponseVO<T> createWithException(int code, String error) {
        return new ResponseVO<T>(code, error, false, null, null);
    }







}
