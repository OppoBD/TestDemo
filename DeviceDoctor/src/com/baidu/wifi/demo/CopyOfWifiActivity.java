package com.baidu.wifi.demo;

import java.io.IOException;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;

import com.baidu.BasicMeta.BasicNetworkUpload;
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
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

/*
 * wifiActivity。
 * 代码中
 */

/*
public class CopyOfWifiActivity extends Activity implements View.OnClickListener {

	
	private static final String TAG = CopyOfWifiActivity.class.getSimpleName();
	RelativeLayout mainLayout = null;
	int akBtnId = 0;
	int initBtnId = 0;

	int uploadBtnId = 0;
	int clearLogBtnId = 0;
	int checkBtnId = 0;

	Button initButton = null;

	Button uploads = null;

	Button clearLog = null;
	Button checkButton = null;

	TextView logText = null;
	ScrollView scrollView = null;
	public static int initialCnt = 0;
	private boolean isLogin = false;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Utils.logStringCache = Utils.getLogText(getApplicationContext());

		Resources resource = this.getResources();
		String pkgName = this.getPackageName();

		setContentView(resource.getIdentifier("wifidemo", "layout", pkgName));
		akBtnId = resource.getIdentifier("btn_initAK", "id", pkgName);
		initBtnId = resource.getIdentifier("btn_init", "id", pkgName);

		uploadBtnId = resource.getIdentifier("btn_uploads", "id", pkgName);
		clearLogBtnId = resource.getIdentifier("btn_clear_log", "id", pkgName);
		checkBtnId = resource.getIdentifier("btn_start_check", "id", pkgName);

		initButton = (Button) findViewById(initBtnId);

		uploads = (Button) findViewById(uploadBtnId);

		checkButton = (Button) findViewById(checkBtnId);

		logText = (TextView) findViewById(resource.getIdentifier("text_log",
				"id", pkgName));
		scrollView = (ScrollView) findViewById(resource.getIdentifier(
				"stroll_text", "id", pkgName));

		initButton.setOnClickListener(this);
		uploads.setOnClickListener(this);
		checkButton.setOnClickListener(this);

	

	}

	@Override
	public void onClick(View v) {
		Log.d("wifidemo", "start click....");
		if (v.getId() == initBtnId) {
			initWithBaiduAccount();
		} else if (v.getId() == uploadBtnId) {
			uploads();
		} else if (v.getId() == clearLogBtnId) {
			Utils.logStringCache = "";
			// Utils.setLogText(getApplicationContext(), Utils.logStringCache);

		} else if (v.getId() == checkBtnId) {
			check();
		} else {

		}

	}

	public  void setWifiInFo() {
		String wserviceName = getApplicationContext().WIFI_SERVICE;
		WifiManager wm = (WifiManager) getSystemService(wserviceName);
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
		wifisum.setDNSIP_DHCP_Proxy(confnets);
		Log.d("cookie_status",
				info.getSupplicantState() + "   " + info.describeContents());

		

		new WifiCompute().saveWifiInfo(wifisum,getApplicationContext());
		
	}

	private void uploads() {
	//	Transport.upload_network(getApplicationContext());
		new BasicNetworkUpload().upload_basicNetwork(getApplicationContext());

	}


	// 开始进行网络环境监察
	private void check() {
		

//		Utils.saveString(getApplicationContext(), "uid", Utils.UID);
//		Utils.saveString(getApplicationContext(), "uname", Utils.UNAME);
//		Utils.saveString(getApplicationContext(), "checktime", Long.toString(System.currentTimeMillis()));
//		
//		
//		//step1. 检查基础网络信息
//		Log.d(TAG, "1. start setWifiInFo");
//	//	new WifiCompute().clear_WifiInfo(getApplicationContext());
		setWifiInFo();
//		//step2. 外网连通性及访问速度
//		Log.d(TAG, "2. start connectNet");
//	//	new NetworkCollect().clearNetWork(getApplicationContext());
//		Transport.connectNet( getApplicationContext());
//		//step3. ping dns的速度及丢包率
//		Log.d(TAG, "3. start pingNetwork");
//	//	new NetPing().clear_ping(getApplicationContext());
//		NetPing.pingDns( getApplicationContext());
//
//		updateDisplay();
		
//		Transport.start_check_background(getApplicationContext());
//		 ProgressBarAsyncTask asyncTask = new ProgressBarAsyncTask(logText, scrollView);  
//         asyncTask.execute(getApplicationContext());  
		
		
	
	}

	// 以百度账号登陆，获取access token来绑定
	private void initWithBaiduAccount() {
		if (isLogin) {
			// 已登录则清除Cookie, access token, 设置登录按钮
			CookieSyncManager.createInstance(getApplicationContext());
			CookieManager.getInstance().removeAllCookie();
			CookieSyncManager.getInstance().sync();

			isLogin = false;
			initButton.setText("切换账号");
		}
		// 跳转到百度账号登录的activity
		Intent intent = new Intent(CopyOfWifiActivity.this, LoginActivity.class);
		startActivity(intent);
	}

	@Override
	public void onStart() {
		super.onStart();
	}

	@Override
	public void onResume() {
		super.onResume();

		Log.d(TAG, "onResume");

	}

//	@Override
//	protected void onNewIntent(Intent intent) {
//
//		String BDUSS = intent.getStringExtra(Utils.BDUSS);
//		if (!BDUSS.equals("bduss")) {
//			isLogin = true;
//			initButton.setText("已登录");
//		}
//	}

	@Override
	public void onStop() {
		super.onStop();
	}

	@Override
	public void onDestroy() {
		// Utils.setLogText(getApplicationContext(), Utils.logStringCache);
		super.onDestroy();
	}

	// 更新界面显示内容
	public void updateDisplay() {

		String text = "hi,"
				+ Utils.UNAME
				+ Utils.NEWLINE
				+ new WifiCompute().getWifiInfo_check(getApplicationContext())
				+ Utils.NEWLINE
				+ new NetworkCollect().getNetwork_IsConnected(getApplicationContext())
				+ Utils.NEWLINE
				+ new NetworkCollect().getNetwork_speed(getApplicationContext())
				+ Utils.NEWLINE
				+ new NetPing().getPingDns_LossRate(getApplicationContext())
				+ Utils.NEWLINE
				+new NetPing().getPingDns_RTT_AVG(getApplicationContext())
				+ Utils.NEWLINE
				+new NetPing().getPingDns_stability(getApplicationContext());
				
				Log.d(TAG, text);
		
		if (logText != null) {
			logText.setText(Html.fromHtml(text));
			

		}
		if (scrollView != null) {
			scrollView.fullScroll(ScrollView.FOCUS_DOWN);
		}
	}
	

}
*/
