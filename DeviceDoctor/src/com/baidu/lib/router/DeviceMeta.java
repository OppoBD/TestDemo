package com.baidu.lib.router;

import java.util.List;

import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.util.Log;

/**
 * wifi相关信息获取
 * 
 * @author li_jing02
 * 
 */
public class DeviceMeta {
	public String deviceName; //设备名称
	public String ip; //设备ip
	public String vendor;
	public String nickName; //设备昵称
	public int upTime;//联网时间，单位为秒
	public int averageSpeed; //平均网速(单位Bps -- Bytes per second)
	public int currentSpeed; //当前网速(单位Bps -- Bytes per second)
	public int totalFlow;//下载总流量
	public int type;//设备接入路由的类型:0表示2.4G	1表示5G 2表示有线接入
	public int accessInternet;//是否允许接入网络 0表示禁止 1表示允许  仅支持无线接入设备
	public int accessRouter;//是否允许接入路由 0表示禁止	1表示允许仅支持无线接入设备
	
	public DeviceMeta(String deviceName,String ip,String vendor,String nickname,int upTime,
			int averageSpeed,int currentSpeed,int totalFlow,int type,int accessInternet,int  accessRouter){
		this.deviceName = deviceName;
		this.ip = ip;
		this.vendor = vendor;
		this.nickName = nickname;
		this.upTime = upTime;
		this.averageSpeed = averageSpeed;
		this.currentSpeed = currentSpeed;
		this.totalFlow = totalFlow;
		this.accessInternet = accessInternet;
		this.accessRouter = accessRouter;
		
	}
   	
}