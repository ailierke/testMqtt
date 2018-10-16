package com.diwinet.pojo;

import java.io.Serializable;

public class ReceiveData implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String sbtm;
	private String k;
	private String v;
	private String s;//反馈成功与否
	public String getK() {
		return k;
	}
	public String getS() {
		return s;
	}
	public String getSbtm() {
		return Long.valueOf(sbtm, 16).toString();
	}
	public String getV() {
		return v;
	}
	public void setK(String k) {
		this.k = k;
	}
	public void setS(String s) {
		this.s = s;
	}
	public void setSbtm(String sbtm) {
		this.sbtm = sbtm;
	}
	public void setV(String v) {
		this.v = v;
	}
	@Override
	public String toString() {
		return "ReceiveData [sbtm=" + this.getSbtm() + ", k=" + k + ", v=" + v + "]";
	}
}
