package com.baidu.lib.wifi;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Vector;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

import com.baidu.book.BookMeta;
import com.baidu.wifi.demo.Utils;

/**
 * wifi相关信息获取
 * 
 * @author li_jing02
 * 
 */
public class WifiCompute {
	
    /** log tag. */
    private static final String TAG = WifiCompute.class.getSimpleName();
	
	public  String NOW_CONNECTED_WIFI_NAME = "now_connect_wifi_name";
	public String NOW_CONNECTED_WIFI_IP = "now_connect_wifi_ip";
	public String NOW_CONNECTED_WIFI_MAC = "now_connect_wifi_mac";
	public String NOW_CONNECTED_WIFI_SPEED = "now_connect_wifi_speed";
	public String NOW_CONNECTED_WIFI_STRENGTH = "now_connect_wifi_strength";
	public String NOW_CONNECTED_WIFI_DNSIP = "now_connect_wifi_dnsip";
	public String NOW_CONNECTED_WIFI_GATEWAY = "now_connect_wifi_gateway";
	public String NOW_CONNECTED_WIFI_ISDHCP = "now_connect_wifi_isdhcp";
	public String NOW_CONNECTED_WIFI_ISPROXY = "now_connect_wifi_isproxy";
	public String NOW_CONNECTED_WIFI_CAPABILITY = "now_connect_wifi_capabilities";
	public String NOW_CONNECTED_WIFI_CHANNEL = "now_connect_wifi_channel";
	public String NOW_CONNECTED_WIFI_LEVEL = "now_connect_wifi_level";
	public String WIFI_SET = "wifi_set";
	private String REGEX = "\t";
	private final static int MIN_RSSI  = -100;  
	private final static int MAX_RSSI   = -55;    


	
	private String[] channel_array_24G = {"1","2","3","4","5","6","7","8","9","10","11","12","13"};
	private String[] channel_array_5G = {"149","150","151","152","153","154"};
	
	public void saveWifiInfo(WifiDetails wifisum,Context context) {
		
		
		List<ScanResult> results = wifisum.getScanResult();
		HashSet<String> set = new HashSet<String>();

		for (ScanResult result : results) {
			String channel = "invalid";
			if (result.frequency >= 2412 && result.frequency <= 2472)
				channel = Integer.toString((result.frequency - 2412) / 5 + 1);
			else if (result.frequency >= 5745 && result.frequency <= 5825)
				channel = Integer
						.toString((result.frequency - 5745) / 20 + 149);
			else
				channel = "unkonw, but frequency is" + result.frequency;

			String tmp = Utils.getNowTime() + this.REGEX + result.SSID + this.REGEX
					+ result.level + this.REGEX + channel + this.REGEX
					+ result.capabilities;
			set.add(tmp);
			
			if(result.SSID.equals(wifisum.getSsid())) {
				wifisum.set_capabilities(result.capabilities);
				wifisum.set_channel(channel);
				wifisum.set_level(calculateSignalLevel(result.level,4));
			}
			
		}
		
		String now_connect = wifisum.getSsid() + this.REGEX + wifisum.getSpeed()
				+ this.REGEX + wifisum.getIp() + this.REGEX + wifisum.getMac() + this.REGEX
				+ wifisum.getStrength() + this.REGEX + wifisum.getDNSIP() +this.REGEX + wifisum.get_isDHCP()+this.REGEX+wifisum.get_isProxy()+this.REGEX+wifisum.get_capabilities();

		Log.d(TAG, now_connect);
		//		
		Utils.saveString(context, NOW_CONNECTED_WIFI_NAME, wifisum.getSsid());
		Utils.saveString(context, NOW_CONNECTED_WIFI_IP, wifisum.getIp());
		Utils.saveString(context, NOW_CONNECTED_WIFI_MAC, wifisum.getMac());
		Utils.saveString(context, NOW_CONNECTED_WIFI_SPEED, wifisum.getSpeed()+"");
		Utils.saveString(context, NOW_CONNECTED_WIFI_STRENGTH, wifisum.getStrength()+"");
		Utils.saveString(context, NOW_CONNECTED_WIFI_DNSIP, wifisum.getDNSIP());
		Utils.saveString(context, NOW_CONNECTED_WIFI_GATEWAY, wifisum.getGateWay());
		Utils.saveString(context, NOW_CONNECTED_WIFI_ISDHCP, wifisum.get_isDHCP()+"");
		Utils.saveString(context, NOW_CONNECTED_WIFI_ISPROXY, wifisum.get_isProxy()+"");
		Utils.saveString(context, NOW_CONNECTED_WIFI_CAPABILITY, wifisum.get_capabilities());
		Utils.saveString(context, NOW_CONNECTED_WIFI_CHANNEL, wifisum.get_channle());
		Utils.saveInteger(context, NOW_CONNECTED_WIFI_LEVEL, wifisum.get_level());
		
		
	//	Utils.saveString(context, this.NOW_CONNECTED_WIFI, now_connect);
		
		Utils.saveSet(context, this.WIFI_SET, set);
	}
	
	public String getWifiInfo_check(Context context){
//		String connected_wifi_return =get_connected_wifi_return( Utils.getString(context,this.NOW_CONNECTED_WIFI));
//		String wifi_set =get_wifi_set_return( Utils.getSet(context, this.WIFI_SET));
//		 return connected_wifi_return+"<br>"+wifi_set;
		
		String result = "";
		String connected_wifi_name = Utils.getString(context,this.NOW_CONNECTED_WIFI_NAME);
		String connected_wifi_ip = Utils.getString(context,this.NOW_CONNECTED_WIFI_IP);
		String connected_wifi_mac = Utils.getString(context,this.NOW_CONNECTED_WIFI_MAC);
		String connected_wifi_speed = Utils.getString(context,this.NOW_CONNECTED_WIFI_SPEED);
		String connected_wifi_strength = Utils.getString(context,this.NOW_CONNECTED_WIFI_STRENGTH);
		String connected_wifi_dnsip = Utils.getString(context,this.NOW_CONNECTED_WIFI_DNSIP);
		String connected_wifi_isdhcp = Utils.getString(context,this.NOW_CONNECTED_WIFI_ISDHCP);
		String connected_wifi_isproxy = Utils.getString(context,this.NOW_CONNECTED_WIFI_ISPROXY);
		String connected_wifi_capabilities = Utils.getString(context,this.NOW_CONNECTED_WIFI_CAPABILITY);
		result  +="当前连接的WIFI名称:"+connected_wifi_name+Utils.NEWLINE+"MAC地址："+connected_wifi_mac+Utils.NEWLINE
				+"是否开启DHCP:"+connected_wifi_isdhcp+Utils.NEWLINE+"代理检测："+get_proxy_check(connected_wifi_isproxy)+Utils.NEWLINE
				+"加密检测:"+get_capabilities_check(connected_wifi_capabilities)+Utils.NEWLINE;
		
		return result;
		
		
	}
	public String get_proxy_check(String is_proxy){
		String fun_name =Thread.currentThread().getStackTrace()[2].getMethodName(); 
		Boolean Bl = new Boolean(is_proxy);
		boolean bl = Bl.booleanValue();
		if(bl)
		//	return Utils.FAIL;
			return Utils.makeFailReturn(fun_name,"设置");
		else
		//	return Utils.PASS;
			return Utils.makePassReturn(fun_name,"默认");
	}
	
	public String get_capabilities_check(String capabilities){
		
		String fun_name =Thread.currentThread().getStackTrace()[2].getMethodName(); 
		
		if(capabilities.indexOf("WPA")>=0||capabilities.indexOf("WEP")>=0)
			
			return Utils.makePassReturn(fun_name,"安全");
		//	return Utils.PASS;
		else
		//	return Utils.FAIL;
			return Utils.makeFailReturn(fun_name,"风险");
	}
	
	
	/**
	 * 通过连接wifi时，通过dhcpinfo获取的dnsip是路由器ip
	public String get_dns_check(String dnsip,String gateway){
		
		Log.d(TAG, "dnsip and gateway is "+dnsip+"-"+gateway);
		if(dnsip.equals(gateway))
		//	return Utils.PASS;
			return Utils.makePassReturn("通过");
		else
		//	return Utils.FAIL;
			return Utils.makeFailReturn("风险");
	}
	*/
	
	public String get_dns_check(String DNS_hijacking_res){
		String fun_name =Thread.currentThread().getStackTrace()[2].getMethodName(); 
		Log.d(TAG, "DNS_hijacking_res: "+DNS_hijacking_res);
		if(DNS_hijacking_res.equals(Utils.ERROR_STRING_DEFAULT))
		//	return Utils.PASS;
			return Utils.makePassReturn(fun_name,"通过");
		else
		//	return Utils.FAIL;
			return Utils.makeFailReturn(fun_name,"风险");
	}
	
	public String get_connected_channel_check(String channel,HashSet<String> set){
		
		Log.d(TAG, "当前信道：" + channel);
		String fun_name =Thread.currentThread().getStackTrace()[2].getMethodName(); 
		boolean is_5G = false;
		boolean is_free = false;
		for(int i =0 ;i<channel_array_5G.length;i++){
			if(channel_array_5G[i].equals(channel))
				is_5G = true;
		}
		
		Vector <String> ve =  get_free_channel(set);
		Iterator<String> iterator=ve.iterator();
		while(iterator.hasNext()){
			if(iterator.next().equals(channel))
				is_free = true;
		}
		
		if(is_5G || is_free )
			return Utils.makePassReturn(fun_name,"通畅");
		else
			return Utils.makeFailReturn(fun_name,"拥堵");
	}
	
	public Vector<String> get_free_channel(HashSet<String> set){
		HashMap<String,Integer> channel_map = new HashMap<String,Integer>();
		Vector <String> ve = new Vector<String>();
		Iterator<String> iterator=set.iterator();
	//	String result = "信道拥堵程度评估：";
		while(iterator.hasNext()){
			String[] item = iterator.next().split(REGEX);
			Log.d(TAG, "信道："+item[3]);
			if(channel_map.containsKey(item[3])){
				int num = channel_map.get(item[3])+1;
				channel_map.remove(item[3]);
				channel_map.put(item[3], num);
			}
			else
				channel_map.put(item[3], 1);
		}
		if(channel_map.size() < channel_array_24G.length + channel_array_5G.length){
		//	for(int i=0;i<channel_array_24G.length;i++)
		}
		for(int i=0;i<channel_array_24G.length;i++){
			if(!channel_map.containsKey(channel_array_24G[i]) || channel_map.get(channel_array_24G[i])<3)
				ve.add(channel_array_24G[i]);
		}
		
		Log.d(TAG, "优质信道集合： "+ve.toString());
			
		return ve;
	}
	

	public static int calculateSignalLevel(int rssi, int numLevels) {
        if (rssi <= MIN_RSSI) {
            return 0;
        } else if (rssi >= MAX_RSSI) {
            return numLevels - 1;
        } else {
            int partitionSize = (MAX_RSSI - MIN_RSSI) / (numLevels - 1);
            return (rssi - MIN_RSSI) / partitionSize;
        }
    }
	
	public String get_wifi_level_check(int level){
		String fun_name =Thread.currentThread().getStackTrace()[2].getMethodName();
		int id = 0;
		if(level == 0){
			Log.d(TAG, "wifi_level = " + 0);
			BookMeta bookmeta_wifi_level = new BookMeta(0,id++,"一般路由器上有对应的wan口连接的灯，该灯不亮","wan口没连好","Wan口插好即可");
			Log.d(TAG, "bookmeta_wifi_level="+ bookmeta_wifi_level.reason);
			Utils.booklist.add_BookList(bookmeta_wifi_level);
			Log.d(TAG, "booklist = " + Utils.booklist.get_result());
			return Utils.makeFailReturn(fun_name,"差");
		}	
		else if(level ==1)
			return Utils.makeFailReturn(fun_name,"较差");
		else if(level ==2)
			return Utils.makePassReturn(fun_name,"较强");
		else if(level ==3)
			return Utils.makePassReturn(fun_name,"强");
		else
			return Utils.makeFailReturn(fun_name,"未知");
	}
	
	
	public int get_wifi_score(int pass_num,int fail_num){
		
		double pass_rate = ((double)(pass_num))/(pass_num+fail_num) ;
		Random rand = new Random();
		int randNum = rand.nextInt(10)+(int)(pass_rate*100-5);
		randNum = randNum>100?100:randNum;
		randNum = randNum <3?3:randNum;
		if(pass_num+fail_num ==0)
			return 0;
		return randNum;
		/*
		if(randNum<=40){
			return "您的健康指数才"+randNum+"分，属于老弱病残之类，赶紧修复一下吧";
		}
		else if(randNum<60){
			return "您的健康指数才"+randNum+"分，健康状态实属堪忧，加紧修复吧";
		}
		else if(randNum<80){
			return "您的健康指数才"+randNum+"分，勉强及格，建议修复一下";
		}
		else
			return "您的健康指数为"+randNum+"分，蕴天地之灵气，藏宇宙之能量，超极致巅峰";
		*/
	}
	
	public void clear_WifiInfo(Context context){

			Utils.clearSP(context,this.NOW_CONNECTED_WIFI_NAME);
			Utils.clearSP(context,this.NOW_CONNECTED_WIFI_IP);
			Utils.clearSP(context,this.NOW_CONNECTED_WIFI_MAC);
			Utils.clearSP(context,this.NOW_CONNECTED_WIFI_SPEED);
			Utils.clearSP(context,this.NOW_CONNECTED_WIFI_STRENGTH);
			Utils.clearSP(context,this.NOW_CONNECTED_WIFI_DNSIP);
			Utils.clearSP(context, this.NOW_CONNECTED_WIFI_GATEWAY);
			Utils.clearSP(context,this.NOW_CONNECTED_WIFI_ISDHCP);
			Utils.clearSP(context,this.NOW_CONNECTED_WIFI_ISPROXY);
			Utils.clearSP(context,this.NOW_CONNECTED_WIFI_CAPABILITY);
			Utils.clearSP(context, this.NOW_CONNECTED_WIFI_CHANNEL);
			Utils.clearSP(context, this.NOW_CONNECTED_WIFI_LEVEL);
			

	}
	
//	public void add_WifiInfo(Context context){
//
//				
//		Utils.clearSP(context,this.NOW_CONNECTED_WIFI_NAME);
//		Utils.clearSP(context,this.NOW_CONNECTED_WIFI_IP);
//		Utils.clearSP(context,this.NOW_CONNECTED_WIFI_MAC);
//		Utils.clearSP(context,this.NOW_CONNECTED_WIFI_SPEED);
//		Utils.clearSP(context,this.NOW_CONNECTED_WIFI_STRENGTH);
//		Utils.clearSP(context,this.NOW_CONNECTED_WIFI_DNSIP);
//		Utils.clearSP(context,this.NOW_CONNECTED_WIFI_ISDHCP);
//		Utils.clearSP(context,this.NOW_CONNECTED_WIFI_ISPROXY);
//		Utils.clearSP(context,this.NOW_CONNECTED_WIFI_CAPABILITY);
//		
//}
}