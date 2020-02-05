package com.model;

/**
 * Desc:
 * Author:Kevin
 * Date:2019/11/10
 **/
public class ResponseCode {

	public final static int OK = 200;

	public final static int GATEWAY_NO_AUTH = 401;

	public final static int GATEWAY_FORBIDDEN = 403;

	public final static int GATEWAY_NOT_FOUND_INTERFACE = 404;

	public final static int GATEWAY_INTERNAL_ERROR = 500;

	public final static int GATEWAY_OVER_FREQUENCY_LIMIT = 501;

	public final static int GATEWAY_NOT_ROUTE = 502;

	public final static int GATEWAY_INNER_BUSY = 503;

	public final static int GATEWAY_NOT_IN_IP_WHITE_LIST = 507;

	public final static int BAD_REQUEST = 400;

	public final static int ACCESS_DENIED = 403;

	public final static int INTERNAL_ERROR = 500;

}
