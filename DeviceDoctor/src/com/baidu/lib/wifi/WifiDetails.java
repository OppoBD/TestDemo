package com.baidu.lib.wifi;

import java.util.List;

import android.net.DhcpInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.util.Log;

/**
 * wifi相关信息获取
 * 
 * @author li_jing02
 * 
 */
public class WifiDetails {
	
    /** log tag. */
    private static final String TAG = WifiDetails.class.getSimpleName();

	private int cur_strength;
	private int cur_speed;
	private String cur_units;
	private String cur_ssid;
	private String cur_ip;
	private String cur_Bssid;
	private String cur_mac;
	private List<ScanResult> cur_ScanResults;
	private String capabilities;//加密算法
	private String cur_DNSIP;
	private String gateway;
	private boolean is_DHCP =false;
	private boolean is_Proxy = false;
	private String channel;  //信道
	private int level;
//	private float Dns_stability = 0;
//	private float PingDns_LossRate = 0;
//	private float PingDns_RTT_AVG = 0;


	public long getStrength() {
		return cur_strength;
	}

	public void setStrength(int strength) {
		this.cur_strength = strength;
	}

	public int getSpeed() {
		return cur_speed;
	}

	public void setSpeed(int speed) {
		this.cur_speed = speed;
	}

	public String getUnits() {
		return cur_units;
	}

	public void setUnits(String units) {
		this.cur_units = units;
	}

	public String getSsid() {
		return cur_ssid;
	}

	public void setSsid(String ssid) {
		this.cur_ssid = ssid;
	}

	public String getIp() {
		return cur_ip;
	}

	public void setIp(int ip) {
		this.cur_ip = (ip & 0xff) + "." + (ip >> 8 & 0xff) + "."
				+ (ip >> 16 & 0xff) + "." + (ip >> 24 & 0xff);
	}

	public String getBssid() {
		return cur_Bssid;
	}

	public void setBssid(String Bssid) {
		this.cur_Bssid = Bssid;
	}

	public String getMac() {
		return cur_mac;
	}

	public void setMac(String mac) {
		this.cur_mac = mac;
	}

	public List<ScanResult> getScanResult() {
		return cur_ScanResults;
	}

	public void setScanResult(List<ScanResult> results) {
		this.cur_ScanResults = results;
	}
	
	public String getDNSIP() {
		return cur_DNSIP;
	}
	public void setDNSIP(String dnsip) {
		this.cur_DNSIP = dnsip;
	}
	
	public String getGateWay() {
		return this.gateway;
	}
	public void setGateWay(String gateway) {
		this.gateway = gateway;
	}

	public boolean get_isDHCP() {
		return this.is_DHCP;
	}
	public boolean get_isProxy() {
		return this.is_Proxy;
	}
	
	public void setDHCP_Proxy(List<WifiConfiguration> results ) {
		 for (WifiConfiguration result : results) {
			
			 if(result.SSID.indexOf(cur_ssid)>=0){
				 Log.d(TAG, result.toString());
				 int start = result.toString().indexOf("DnsAddresses: ");
				 int s1 = result.toString().indexOf("[", start);
				 int s2 = result.toString().indexOf("]", start);
				 this.cur_DNSIP = result.toString().substring(s1+1, s2).replaceAll(",","").trim();
				 if(result.toString().indexOf("IP assignment: DHCP") >=0)
					 this.is_DHCP = true;
				 if(result.toString().indexOf("Proxy settings: NONE") <0)
					 this.is_Proxy = true;
			 }
		 }
	}
	
	
	public int getscan_num() {
		return this.cur_ScanResults.size();
	}
	
	public String get_capabilities() {
		 return this.capabilities;
	}
	
	public void set_capabilities(String capabilities) {
		 this.capabilities = capabilities;
	}
	
	public String get_channle(){
		return this.channel;
	}
	public void set_channel(String channel){
		this.channel = channel;
	}
	
	public int get_level(){
		return this.level;
	}
	public void set_level(int level){
		this.level = level;
	}

}
