package com.baidu.lib.router;

import java.io.Serializable;
import java.util.List;

import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.util.Log;

/**
 * 路由版本信息
 * 
 * @author li_jing02
 * 
 */
public class RouterVersion implements Serializable{
	public String hwn = "hwn"; //硬件出品方
	public String hwv = "hwv"; //硬件产品型号
	public String FW_version = "FW_version"; //硬件产品型号
	public String Manager_version = "Manager_version";//下载模块版本
	public RouterVersion(String hwn,String hwv,String FW_version,String Manager_version){
		this.hwn = hwn;
		this.hwv = hwv;
		this.FW_version = FW_version;
		this.Manager_version = Manager_version;
	}
}