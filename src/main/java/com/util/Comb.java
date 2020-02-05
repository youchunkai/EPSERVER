package com.util;

public class Comb<T1,T2,T3> {
	//缓存策略
	private T1 V1;
	//缓存条件参数
	private T2 V2;
	//是否缓存空结果标志位
	private T3 V3;

	public Comb()
	{
		
	}	
	public Comb(T1 v1,T2 v2,T3 v3)
	{
		this.V1 = v1;
		this.V2 = v2;
		this.V3 = v3;
	}
	
	public T1 getV1() {
		return V1;
	}

	public void setV1(T1 v1) {
		V1 = v1;
	}

	public T2 getV2() {
		return V2;
	}

	public void setV2(T2 v2) {
		V2 = v2;
	}
	public T3 getV3() {
		return V3;
	}
	public void setV3(T3 v3) {
		V3 = v3;
	}
	
	
}
