package com.baidu.lib.router;

import java.io.Serializable;
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
public class RouterMeta  implements Serializable{
	
    /** log tag. */
    private static final String TAG = RouterMeta.class.getSimpleName();

    private String deviceId = "deviceid";
	private String deviceName ="devicename";
	private String adminpwd = "adminpwd"; //管理密码
	private int cur_speed = -1; //路由当前wan口下行速度(单位Bps -- Bytes per second)
	private int device_cur_speed = -1;//当前检测设备的网速
	private int avg_speed = -1; //平均速度
	private int accessInternet = -1;//是否允许接入网络
	private int accessRouter = -1;//是否允许接入路由
	private int devicenum = -1 ;
	private int  network = -1; //1: 连接外网  0: 未连接
	private int wan = -1; //0: wan口已经插入网线并获得ip 1: 未插入网线	2: wan口插入网线未获取ip
	private float diskUseRate = -1; //磁盘利用率
	private int taskNum = -1;//当前下载任务数
	private String connect_type = "connect_type"; //DHCP/PPPOE/STATIC
	private int wifiType = -1 ;//wifiType0: 2.4GHz 1: 5GHz
	private int status = -1 ;//status0: wifi关闭 1: wifi开启
	private List<DeviceMeta> device_list =null;
	private RouterVersion routerVersion;
	

	public String getAdminPwd() {
		return this.adminpwd;
	}

	public void setAdminPwd(String pwd) {
		this.adminpwd = pwd;
	}
	
	public String getDeviceId() {
		return this.deviceId;
	}

	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}
	
	
	public String getDeviceName() {
		return this.deviceName;
	}

	public void setDeviceName(String deviceName) {
		this.deviceName = deviceName;
	}

	public int getCur_speed() {
		return cur_speed;
	}

	public void setCur_Speed(int speed) {
		this.cur_speed = speed;
	}
	
	public int getDeviceCur_speed() {
		return this.device_cur_speed;
	}

	public void setDeviceCur_Speed(int speed) {
		this.device_cur_speed = speed;
	}
	
	public void setAvg_Speed(int speed) {
		this.avg_speed = speed;
	}

	public int getAvg_speed() {
		return this.avg_speed;
	}
	
	public int getDeviceNum() {
		return this.devicenum;
	}

	public void setDeviceNum(int devicenum) {
		this.devicenum = devicenum;
	}
	
	public void setNetwork(int network){
		this.network = network;
	}
	
	public int getNetwork(){
		return this.network;
	}
	public void setWan(int wan){
		this.wan = wan;
	}
	public int getWan(){
		return this.wan;
	}
	
	public void setDiskUseRate(int diskUse,int diskTotal){
		if(diskTotal == 0)
			this.diskUseRate = -1;
		else
			this.diskUseRate = (float)(diskUse)/diskTotal;
	}
	
	public float getDiskUseRate(){
		return this.diskUseRate;
	}
	
	public void setTaskNum(int tasknum){
		this.taskNum = tasknum;
	}
	public int getTaskNum(){
		return this.taskNum;
	}
		
	public void setConnect_type(String type){
		this.connect_type = type;
	}
	public String getConnect_type(){
		return this.connect_type;
	}
	public void setWifiType(int type){
		this.wifiType = type;
	}
	public int getWifiType(){
		return this.wifiType;
	}
	public void setStatus(int status){
		this.status = status;
	}
	public int getStatus(){
		return this.status;
	}
	public void setDevice_list(List<DeviceMeta> device_list){
		this.device_list = device_list;
	}
	public List<DeviceMeta> getDevice_list(){
		return this.device_list;
	}
	public void setRouterVersion(RouterVersion routerVersion){
		this.routerVersion = routerVersion;
	}
	public RouterVersion getRouterVersion(){
		return this.routerVersion;
	}
	
	public void setAccessInternet(int accessInternet){
		this.accessInternet = accessInternet;
	}
	public int getAccessInternet(){
		return this.accessInternet;
	}
	public void setAccessRouter(int accessRouter){
		this.accessRouter = accessRouter;
	}
	public int GETAccessRouter(){
		return this.accessRouter;
	}
		
	
}
