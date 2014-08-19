package com.baidu.book;

import java.io.Serializable;
import java.util.List;

import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.util.Log;

/**
 * 解决方案相关信息
 * 
 * @author li_jing02
 * 
 */
public class BookMeta implements Serializable{
	
	public int book_type; //0表示无法上网
	public int id;
	public String appearance;
	public String reason;
	public String solution;
//	private static enum  book_type {BASIC,ROUTER,IERMU,SHOUHUAN};
	
	public BookMeta(int book_type,int id,String appearance,String reason,String solution){
		this.book_type = book_type;
		this.id = id;
		this.appearance = appearance;
		this.reason = reason;
		this.solution = solution;
		
	}
   	
}