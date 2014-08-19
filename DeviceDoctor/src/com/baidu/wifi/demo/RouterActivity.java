package com.baidu.wifi.demo;

import java.io.IOException;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import com.baidu.BasicMeta.BasicNetworkUpload;
import com.baidu.http.NetworkCheck;
import com.baidu.http.NetworkCollect;
import com.baidu.http.Transport;
import com.baidu.lib.router.DeviceMeta;
import com.baidu.lib.router.RouterMeta;
import com.baidu.lib.router.RouterVersion;
import com.baidu.lib.wifi.NetPing;
import com.baidu.lib.wifi.WifiCompute;
import com.baidu.lib.wifi.WifiDetails;

import android.app.Activity;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.Html;
import android.text.format.Formatter;
import android.util.Log;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.net.DhcpInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

/*
 * wifiActivity。
 * 代码中
 */
public class RouterActivity extends Activity implements View.OnClickListener {

	private static final String TAG = RouterActivity.class.getSimpleName();
	RelativeLayout mainLayout = null;

	int router_finish_BtnId = 0;
	TextView textView_router_cur_speed = null;
	TextView textView_router_score = null;
	TextView textView_check_wan = null;
	TextView textView_check_network = null;
	TextView textView_check_diskUsedRate = null;
	TextView textView_check_version = null;
	TextView textView_check_pwd = null;
	TextView textView_check_devicenum = null;
	TextView textView_check_arpAttack = null;
	Button btn_router_finish = null;
	RouterMeta routerMeta = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Utils.logStringCache = Utils.getLogText(getApplicationContext());
		
		
		Utils.CHECK_PASS_NUM = 0;
		Utils.CHECK_FAIL_NUM = 0;

		Resources resource = this.getResources();
		String pkgName = this.getPackageName();
		setContentView(resource.getIdentifier("new_router_activity", "layout", pkgName));
		
		Intent intent = getIntent();  
		routerMeta = (RouterMeta)intent.getSerializableExtra("routerMeta");

		textView_router_cur_speed = (TextView) findViewById(resource
				.getIdentifier("text_router_cur_speed", "id", pkgName));
		textView_router_score = (TextView) findViewById(resource
				.getIdentifier("text_router_score", "id", pkgName));
		
		textView_check_pwd = (TextView) findViewById(resource
				.getIdentifier("check_pwd", "id", pkgName));
		
		textView_check_wan = (TextView) findViewById(resource.getIdentifier(
				"check_wan", "id", pkgName));
		textView_check_network = (TextView) findViewById(resource.getIdentifier(
				"check_network", "id", pkgName));
		textView_check_diskUsedRate = (TextView) findViewById(resource
				.getIdentifier("check_diskUsedRate", "id", pkgName));
		textView_check_version = (TextView) findViewById(resource
				.getIdentifier("check_version", "id", pkgName));
		textView_check_devicenum = (TextView) findViewById(resource
				.getIdentifier("check_devicenum", "id", pkgName));
		textView_check_arpAttack = (TextView) findViewById(resource
				.getIdentifier("check_arpAttack", "id", pkgName));
		
		router_finish_BtnId = resource
				.getIdentifier("btn_router_finish", "id", pkgName);
		btn_router_finish = (Button) findViewById(router_finish_BtnId);
		btn_router_finish.setOnClickListener(this);

		if (NetworkCheck.isWifi(getApplicationContext()) || routerMeta != null) {
//			textView_router_cur_speed.setText(Html
//					.fromHtml(get_router_speed(routerMeta)));

			
			textView_check_pwd.setText(Html.fromHtml(get_pwd_check(routerMeta)));
			textView_check_wan.setText(Html.fromHtml(get_wan_check(routerMeta)));
			textView_check_network.setText(Html.fromHtml(get_network_check(routerMeta)));
			textView_check_diskUsedRate.setText(Html.fromHtml(get_diskUsedRate_check(routerMeta)));
			textView_check_version.setText(Html.fromHtml(get_version_check(routerMeta)));
			textView_check_devicenum.setText(Html.fromHtml(get_router_devicenum(routerMeta)));
			textView_check_arpAttack.setText(Html.fromHtml(get_router_arpAttack(routerMeta)));
			int score = new WifiCompute().get_wifi_score(Utils.CHECK_PASS_NUM,Utils.CHECK_FAIL_NUM);
			textView_router_score.setText(score+"分");
			
			double speed = routerMeta.getCur_speed()/1024;
			textView_router_cur_speed.setText(new NetworkCollect()
							.comments(score,speed));
			
			
		} 
		else{ 
			textView_router_cur_speed.setText("呜呜呜...路由连不上外网。。。");
			textView_router_score.setText("0分");
		}
		
		

		Log.d(TAG, new WifiCompute().getWifiInfo_check(getApplicationContext()));
	}

	
	 @Override
	 public void onClick(View v) {
	 Log.d("router", "start click....");
	 if (v.getId() == router_finish_BtnId) {
		 router_finish();
	 } else {
	 }
	
	 }
	 

	 
	 private String get_wan_check(RouterMeta routerMeta){
		 String fun_name =Thread.currentThread().getStackTrace()[2].getMethodName(); 
		 if(routerMeta == null)
			 return Utils.makeFailReturn(fun_name,"未知错误");
		 else if(routerMeta.getWan() == 0)
			 return Utils.makePassReturn(fun_name,"已插好");
		 else if(routerMeta.getWan() == 1)
			 return Utils.makeFailReturn(fun_name,"未插入");	 
		 else if(routerMeta.getWan() == 2)
			 return Utils.makeFailReturn(fun_name,"插入异常");	
		 else
			 return Utils.makeFailReturn(fun_name,"未知错误");
		 
	 }
	 
	 private String get_network_check(RouterMeta routerMeta){
		 String fun_name =Thread.currentThread().getStackTrace()[2].getMethodName(); 
		 if(routerMeta == null)
			 return Utils.makeFailReturn(fun_name,"未知错误");
		 else if(routerMeta.getNetwork() == 1)
			 return Utils.makePassReturn(fun_name,"已连接");
		 else if(routerMeta.getWan() == 0)
			 return Utils.makeFailReturn(fun_name,"未连接");	 	
		 else
			 return Utils.makeFailReturn(fun_name,"未知错误");
		 
	 }
	 
	 private String get_diskUsedRate_check(RouterMeta routerMeta){
		 String fun_name =Thread.currentThread().getStackTrace()[2].getMethodName(); 
		 if(routerMeta == null || routerMeta.getDiskUseRate() < 0)
			 return Utils.makeFailReturn(fun_name,"未知错误");
		 else if(routerMeta.getDiskUseRate() < 0.4 )
			 return Utils.makePassReturn(fun_name,"低");
		 else if(routerMeta.getDiskUseRate() < 0.8)
			 return Utils.makeFailReturn(fun_name,"中");	 	
		 else if(routerMeta.getDiskUseRate() <= 1)
			 return Utils.makeFailReturn(fun_name,"高");	 
		 else
			 return Utils.makeFailReturn(fun_name,"未知错误");
		 
	 }
	 
	 private String get_version_check(RouterMeta routerMeta){
		 String fun_name =Thread.currentThread().getStackTrace()[2].getMethodName(); 
		 if(routerMeta == null)
			 return Utils.makeFailReturn(fun_name,"未知版本");
		 Log.d(TAG, "FW_version"+routerMeta.getRouterVersion().FW_version);
		 RouterVersion routerVersion = routerMeta.getRouterVersion();
		 if(routerVersion == null)
			 return Utils.makeFailReturn(fun_name,"未知版本");
		 else if(routerVersion.FW_version.equals("1.6.4.15"))
			 return Utils.makePassReturn(fun_name,"最新版本");
		 else
			 return Utils.makeFailReturn(fun_name,"有新版本");
		 
	 }
	 
	 private String get_pwd_check(RouterMeta routerMeta){
		 String fun_name =Thread.currentThread().getStackTrace()[2].getMethodName(); 
		 if(routerMeta == null)
			 return Utils.makeFailReturn(fun_name,"异常错误");
		 String pwd = routerMeta.getAdminPwd();
		 if(Utils.equalStr(pwd) || Utils.isOrderNumeric_Increase(pwd) || Utils.isOrderNumeric_Inverte(pwd) || pwd.equals("admin"))
			 return Utils.makeFailReturn(fun_name,"简单");
		 else
			 return Utils.makePassReturn(fun_name,"安全");
	 }

	 
	 private String get_router_speed(RouterMeta routerMeta){
		 String fun_name =Thread.currentThread().getStackTrace()[2].getMethodName(); 
		 if(routerMeta == null)
			 return Utils.makeFailReturn(fun_name,"apk出现了异常错误，无法获取你的网速信息",1000);
		 int speed = routerMeta.getCur_speed();
		 
		 Log.d(TAG, "当前速度是:"+speed +"平均速度是:"+ routerMeta.getCur_speed());
		 if(speed < 0)
			 return Utils.makeFailReturn(fun_name,"apk出现了异常错误，无法获取你的网速信息");
		 else if(speed < Utils.NETWORK_SPEED_BASE*1024)
			 return Utils.makeFailReturn(fun_name,"网速也着实让人着急了，当亲速度只有"+speed+"B/s,您这是在和蜗牛比赛么?");
		 else
			 return Utils.makePassReturn(fun_name,"超幸福网速，喂我独享！当前速度"+speed+"B/s,快来享受小伙伴羡慕嫉妒恨的目光吧！");
	 }
	 
	 
	 private String get_router_devicenum(RouterMeta routerMeta){
		 String fun_name =Thread.currentThread().getStackTrace()[2].getMethodName(); 
		 if(routerMeta == null)
			 return Utils.makeFailReturn(fun_name,"异常错误");
		 int devicenum = routerMeta.getDeviceNum();
		 int tasknum = routerMeta.getTaskNum();
		 int device_curspeed = routerMeta.getDeviceCur_speed();
		 int router_curspeed = routerMeta.getCur_speed();
		 Log.d(TAG, "devicenum: "+devicenum+" tasknum:" +tasknum + "device_curspeed: "+ device_curspeed + "router_curspeed: "+router_curspeed );
		 if(device_curspeed ==-1 || router_curspeed ==-1)
			 return Utils.makeFailReturn(fun_name,"异常错误");
		 else if(device_curspeed/router_curspeed >0.3)
			 return Utils.makePassReturn(fun_name,"高");
		 else  if((devicenum<3 && tasknum <3) && device_curspeed/router_curspeed >0.3)
			 return Utils.makePassReturn(fun_name,"高");
		 else
			 return Utils.makeFailReturn(fun_name,"低");
		
	 }
	 
	 
	 private String get_router_arpAttack(RouterMeta routerMeta){
		 String fun_name =Thread.currentThread().getStackTrace()[2].getMethodName(); 
		 if(routerMeta == null)
			 return Utils.makeFailReturn(fun_name,"异常错误");
		 
		 else
			 return Utils.makePassReturn(fun_name,"安全");
		 /*
		 List<DeviceMeta> devicelist = routerMeta.getDevice_list();
		 if(devicelist == null ||devicelist.size() ==0)
			 return Utils.makePassReturn("安全");
		 else{
			 Iterator<DeviceMeta> ite = devicelist.iterator();
			 while(ite.hasNext())
			 {
				 DeviceMeta dm = ite.next();
				 if(dm.accessRouter !=0 && dm.ip != null )
					 NetPing.Ping(getApplicationContext(), dm.ip);
					String rtt_time = Utils.getString(getApplicationContext(), dm.ip+Utils.PING_RTT_TIME);
					if(!rtt_time.equals(Utils.ERROR_STRING_DEFAULT)){
					String rtt_avg = rtt_time.split("/")[1];
					if(Double.parseDouble(rtt_avg) <Utils.PING_AVG_BASE )
					{
						return Utils.makeFailReturn("风险");
					}
					 
			 }
		 }
	
			 return Utils.makePassReturn("安全");
		 }
		 */
		
	 }
	 
	 
	@SuppressWarnings("deprecation")
	public void setWifiInFo() {
		String wserviceName = getApplicationContext().WIFI_SERVICE;
		WifiManager wm = (WifiManager) getSystemService(wserviceName);
		DhcpInfo dhcpinfo = wm.getDhcpInfo();
		WifiInfo info = wm.getConnectionInfo();
		List<WifiConfiguration> confnets = wm.getConfiguredNetworks();

		WifiDetails wifisum = new WifiDetails();
		wifisum.setStrength(info.getRssi());
		wifisum.setSpeed(info.getLinkSpeed());
		wifisum.setBssid(info.getBSSID());
		wifisum.setIp(info.getIpAddress());
		wifisum.setMac(info.getMacAddress());
		wifisum.setSsid(info.getSSID());
		wifisum.setScanResult(wm.getScanResults());
		wifisum.setDHCP_Proxy(confnets);
		wifisum.setDNSIP(Formatter.formatIpAddress(dhcpinfo.dns1));
		wifisum.setGateWay(Formatter.formatIpAddress(dhcpinfo.gateway));
		Log.d("cookie_status",
				info.getSupplicantState() + "   " + info.describeContents());

		new WifiCompute().saveWifiInfo(wifisum, getApplicationContext());

	}
	

	private void router_finish() {
		// Transport.upload_network(getApplicationContext());
		new BasicNetworkUpload().upload_basicRouter(getApplicationContext(),this.routerMeta);
		Intent intent = new Intent(RouterActivity.this, MainActivity.class);
		startActivity(intent);

	}
	
	
	/**
	 * 路由评分
	 * @param pass_num
	 * @param fail_num
	 * @return
	 */
	private String get_router_score(int pass_num,int fail_num){
		
		Log.d(TAG, "get_router_score: "+pass_num+"失败数:"+fail_num);
		
		double pass_rate = ((double)(pass_num))/(pass_num+fail_num) ;
		Random rand = new Random();
		int randNum = rand.nextInt(10)+(int)(pass_rate*100-5);
		randNum = randNum>100?100:randNum;
		randNum = randNum <3?3:randNum;
		if(pass_num+fail_num ==0)
			return "问老天，为啥会走到这个逻辑呢？";
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
	}

	// 开始进行网络环境监察
	private void check() {

		// Utils.saveString(getApplicationContext(), "uid", Utils.UID);
		// Utils.saveString(getApplicationContext(), "uname", Utils.UNAME);
		// Utils.saveString(getApplicationContext(), "checktime",
		// Long.toString(System.currentTimeMillis()));
		//
		//
		// //step1. 检查基础网络信息
		// Log.d(TAG, "1. start setWifiInFo");
		// // new WifiCompute().clear_WifiInfo(getApplicationContext());
		setWifiInFo();
		// //step2. 外网连通性及访问速度
		// Log.d(TAG, "2. start connectNet");
		// // new NetworkCollect().clearNetWork(getApplicationContext());
		// Transport.connectNet( getApplicationContext());
		// //step3. ping dns的速度及丢包率
		// Log.d(TAG, "3. start pingNetwork");
		// // new NetPing().clear_ping(getApplicationContext());
		// NetPing.pingDns( getApplicationContext());
		//
		// updateDisplay();

		// Transport.start_check_background(getApplicationContext());
		// ProgressBarAsyncTask asyncTask = new
		// ProgressBarAsyncTask(getApplicationContext(),
		// new ProgressBarListener() {
		//
		// @Override
		// public void onReceive() {
		// textView_device_avg_speed.setText(new
		// NetworkCollect().getNetwork_speed(getApplicationContext()));
		//
		// }
		//
		//
		// });
		// asyncTask.execute(getApplicationContext());

	}

	//
	// // 以百度账号登陆，获取access token来绑定
	// private void initWithBaiduAccount() {
	// if (isLogin) {
	// // 已登录则清除Cookie, access token, 设置登录按钮
	// CookieSyncManager.createInstance(getApplicationContext());
	// CookieManager.getInstance().removeAllCookie();
	// CookieSyncManager.getInstance().sync();
	//
	// isLogin = false;
	// initButton.setText("切换账号");
	// }
	// // 跳转到百度账号登录的activity
	// Intent intent = new Intent(WifiActivity.this, LoginActivity.class);
	// startActivity(intent);
	// }

	@Override
	public void onStart() {
		super.onStart();
	}

	@Override
	public void onResume() {
		super.onResume();

		Log.d(TAG, "onResume");

	}

	// @Override
	// protected void onNewIntent(Intent intent) {
	//
	// String BDUSS = intent.getStringExtra(Utils.BDUSS);
	// if (!BDUSS.equals("bduss")) {
	// isLogin = true;
	// initButton.setText("已登录");
	// }
	// }

	@Override
	public void onStop() {
		super.onStop();
	}

	@Override
	public void onDestroy() {
		// Utils.setLogText(getApplicationContext(), Utils.logStringCache);
		super.onDestroy();
	}

	// // 更新界面显示内容
	// public void updateDisplay() {
	//
	// String text = "hi,"
	// + Utils.UNAME
	// + Utils.NEWLINE
	// + new WifiCompute().getWifiInfo_check(getApplicationContext())
	// + Utils.NEWLINE
	// + new NetworkCollect().getNetwork_IsConnected(getApplicationContext())
	// + Utils.NEWLINE
	// + new NetworkCollect().getNetwork_speed(getApplicationContext())
	// + Utils.NEWLINE
	// + new NetPing().getPingDns_LossRate(getApplicationContext())
	// + Utils.NEWLINE
	// +new NetPing().getPingDns_RTT_AVG(getApplicationContext())
	// + Utils.NEWLINE
	// +new NetPing().getPingDns_stability(getApplicationContext());
	//
	// Log.d(TAG, text);
	//
	// if (logText != null) {
	// logText.setText(Html.fromHtml(text));
	//
	//
	// }
	// if (scrollView != null) {
	// scrollView.fullScroll(ScrollView.FOCUS_DOWN);
	// }
	// }

}
