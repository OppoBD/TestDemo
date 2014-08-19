package com.baidu.wifi.demo;

import java.io.IOException;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;

import com.baidu.BasicMeta.BasicNetworkUpload;
import com.baidu.book.BookList_Construct;
import com.baidu.book.BookMeta;
import com.baidu.doctor.scheduler.AlarmReceiver;
import com.baidu.http.NetworkCheck;
import com.baidu.http.NetworkCollect;
import com.baidu.http.Transport;
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
public class WifiActivity extends Activity implements View.OnClickListener {

	private static final String TAG = WifiActivity.class.getSimpleName();
	RelativeLayout mainLayout = null;

	int network_finish_BtnId = 0;
	int network_book_BtnId = 0;
	TextView textView_network_avg_speed = null;
	TextView textView_network_score = null;
	TextView textView_check_capability = null;
	TextView textView_check_dns = null;
	TextView textView_check_proxy = null;
	TextView textView_check_connectNetwork = null;
	TextView textView_check_NetworkStability = null;
	TextView textView_check_WIFI_channel = null;
	TextView textView_check_WIFI_Strength = null;
	Button btn_network_finish = null;
	Button btn_network_book = null;
	
	public AlarmReceiver alarm = new AlarmReceiver();
	public int id = 0;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Utils.logStringCache = Utils.getLogText(getApplicationContext());
		
		
		Utils.CHECK_PASS_NUM = 0;
		Utils.CHECK_FAIL_NUM = 0;
		Utils.booklist = null;
		Utils.booklist= new BookList_Construct();
		
		Resources resource = this.getResources();
		String pkgName = this.getPackageName();

		setContentView(resource.getIdentifier("new_wifi_activity", "layout", pkgName));

		textView_network_avg_speed = (TextView) findViewById(resource
				.getIdentifier("text_network_avg_speed", "id", pkgName));
		textView_network_score = (TextView) findViewById(resource
				.getIdentifier("text_network_score", "id", pkgName));
		textView_check_capability = (TextView) findViewById(resource
				.getIdentifier("check_capability", "id", pkgName));
		textView_check_dns = (TextView) findViewById(resource.getIdentifier(
				"check_dns", "id", pkgName));
		textView_check_proxy = (TextView) findViewById(resource.getIdentifier(
				"check_proxy", "id", pkgName));
		textView_check_connectNetwork = (TextView) findViewById(resource
				.getIdentifier("check_connectNetwork", "id", pkgName));
		textView_check_NetworkStability = (TextView) findViewById(resource
				.getIdentifier("check_NetworkStability", "id", pkgName));
		textView_check_WIFI_channel = (TextView) findViewById(resource
				.getIdentifier("check_WIFI_channel", "id", pkgName));
		textView_check_WIFI_Strength = (TextView) findViewById(resource
				.getIdentifier("check_WIFI_Strength", "id", pkgName));
		
		network_finish_BtnId = resource
				.getIdentifier("btn_network_finish", "id", pkgName);
		btn_network_finish = (Button) findViewById(network_finish_BtnId);
		btn_network_finish.setOnClickListener(this);
		
		network_book_BtnId = resource
				.getIdentifier("btn_network_book", "id", pkgName);
		btn_network_book = (Button) findViewById(network_book_BtnId);
		btn_network_book.setOnClickListener(this);

		if (NetworkCheck.isWifi(getApplicationContext())) {
			
			textView_check_capability.setText(Html.fromHtml(new WifiCompute()
					.get_capabilities_check(Utils.getString(
							getApplicationContext(),
							new WifiCompute().NOW_CONNECTED_WIFI_CAPABILITY))));
			textView_check_dns.setText(Html.fromHtml(new WifiCompute()
					.get_dns_check(Utils.getString(getApplicationContext(),
									Utils.DNS_hijacking+Utils.PING_PACKAGE_LOSS_RATE))));
			textView_check_proxy.setText(Html.fromHtml(new WifiCompute()
					.get_proxy_check(Utils.getString(getApplicationContext(),
							new WifiCompute().NOW_CONNECTED_WIFI_ISPROXY))));
			textView_check_connectNetwork.setText(Html
					.fromHtml(new NetworkCollect()
							.getNetwork_IsConnected(getApplicationContext())));
			String host = Utils.getString(getApplicationContext(),
					new WifiCompute().NOW_CONNECTED_WIFI_GATEWAY);
			textView_check_NetworkStability.setText(Html
					.fromHtml(new NetPing()
					.getPingDns_LossRate(getApplicationContext(),host)
							));
			 textView_check_WIFI_channel.setText(Html.fromHtml(new WifiCompute()
			 		.get_connected_channel_check(Utils.getString(getApplicationContext(),
			 				new WifiCompute().NOW_CONNECTED_WIFI_CHANNEL), 
			 				Utils.getSet(getApplicationContext(), new WifiCompute().WIFI_SET))));
			 textView_check_WIFI_Strength.setText(Html.fromHtml(new WifiCompute()
			 		.get_wifi_level_check(
			 				(Utils.getInteger(getApplicationContext(), 
			 						new WifiCompute().NOW_CONNECTED_WIFI_LEVEL)))));
			 
			double speed =  new NetworkCollect().getNetwork_speed(getApplicationContext());
			int score = new WifiCompute().get_wifi_score(Utils.CHECK_PASS_NUM,Utils.CHECK_FAIL_NUM);
			textView_network_score.setText(score+"分");
			textView_network_avg_speed.setText(Html
					.fromHtml(new NetworkCollect()
							.comments(score,speed)));
				
		} else { 
			textView_network_avg_speed.setText(Html
					.fromHtml("未开启wifi，无法进行检测"));
			textView_network_score.setText("0分");
		}
		
		

		Log.d(TAG, new WifiCompute().getWifiInfo_check(getApplicationContext()));
	}

	
	 @Override
	 public void onClick(View v) {
		 Log.d("wifidemo", "start click....");
		 if (v.getId() == network_finish_BtnId) {
			 network_finish();
		 } else if(v.getId() == network_book_BtnId) {
			 alarm.setAlarm(this);
			 Intent intent = new Intent();
			 intent.setClass(WifiActivity.this, SolveActivity.class);
			 Bundle bundle = new Bundle();
			 bundle.putSerializable("booklist", Utils.booklist);
			 intent.putExtras(bundle);
			 this.startActivity(intent);
		 }
	
	 }

	public void setWifiInFo() {
		String wserviceName = getApplicationContext().WIFI_SERVICE;
		WifiManager wm = (WifiManager) getSystemService(wserviceName);
		WifiInfo info = wm.getConnectionInfo();
		DhcpInfo dhcpinfo = wm.getDhcpInfo();
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
	

	private void network_finish() {
		// Transport.upload_network(getApplicationContext());
		new BasicNetworkUpload().upload_basicNetwork(getApplicationContext());
		new BasicNetworkUpload().upload_advancedNetwork(getApplicationContext());
		Intent intent = new Intent(WifiActivity.this, MainActivity.class);
		startActivity(intent);

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
