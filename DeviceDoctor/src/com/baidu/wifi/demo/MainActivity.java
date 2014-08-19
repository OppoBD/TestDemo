package com.baidu.wifi.demo;

import java.io.IOException;
import java.util.List;

import org.json.JSONException;

import com.baidu.BasicMeta.BasicNetworkUpload;
import com.baidu.http.NetworkCheck;
import com.baidu.http.NetworkCollect;
import com.baidu.lib.router.ConntectRouter;
import com.baidu.lib.router.RouterAdminPwdCheckAsyncTask;
import com.baidu.lib.router.RouterCheckAsyncTask;
import com.baidu.lib.router.RouterListener;
import com.baidu.lib.router.RouterMeta;
import com.baidu.lib.wifi.NetPing;
import com.baidu.lib.wifi.NetworkCheckAsyncTask;
import com.baidu.lib.wifi.NetworkListener;
import com.baidu.lib.wifi.WifiCompute;
import com.baidu.lib.wifi.WifiDetails;
import com.baidu.wearable.test.wristbandtester.AutoTestActivity;

import com.baidu.wifi.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.net.DhcpInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.text.format.Formatter;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * 

 */
public class MainActivity extends Activity  implements View.OnClickListener {
	

	private static final String TAG = MainActivity.class.getSimpleName();
	RelativeLayout mainLayout = null;

	int networkBtnId = 0;
	int routerBtnId = 0;
	int iermuBtnId = 0;
	int shouhuanBtnId = 0;
	int bookBtnId = 0;

	ImageButton check_networkButton = null;
	ImageButton check_iermuButton = null;
	ImageButton check_routerButton = null;
	ImageButton check_shouhuanButton = null;
	ImageButton bookButton = null;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

//		Resources resource = this.getResources();
//		String pkgName = this.getPackageName();
//
//		setContentView(resource.getIdentifier("custom_activity", "layout",
//				pkgName));
		
		Resources resource = this.getResources();
		String pkgName = this.getPackageName();

		setContentView(resource.getIdentifier("new_main_activity", "layout", pkgName));

		networkBtnId = resource.getIdentifier("btn_check_network", "id", pkgName);
		routerBtnId = resource.getIdentifier("btn_check_router", "id", pkgName);
		iermuBtnId = resource.getIdentifier("btn_check_iermu", "id", pkgName);
		shouhuanBtnId = resource.getIdentifier("btn_check_shouhuan", "id", pkgName);
		bookBtnId = resource.getIdentifier("btn_check_book", "id", pkgName);

		check_networkButton = (ImageButton) findViewById(networkBtnId);
		check_routerButton = (ImageButton) findViewById(routerBtnId);
		check_iermuButton = (ImageButton) findViewById(iermuBtnId);
		check_shouhuanButton = (ImageButton) findViewById(shouhuanBtnId);
		bookButton = (ImageButton) findViewById(bookBtnId);

		check_networkButton.setOnClickListener(this);
		check_routerButton.setOnClickListener(this);
		check_iermuButton.setOnClickListener(this);
		check_shouhuanButton.setOnClickListener(this);
		bookButton.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {

		if (v.getId() == networkBtnId) {
			start_check_network();
		} else if (v.getId() == routerBtnId) {
			try {
				start_check_router();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else if (v.getId() == iermuBtnId) {
			;
		} else if (v.getId() == shouhuanBtnId) {
			start_check_shouhuan();
		} else if (v.getId() == bookBtnId) {
			show_network_faster_book();
		} else {
			;
		}
		
	}
	
	
	private void start_check_router() throws IOException, JSONException{
		
		
		if(!NetworkCheck.isWifi(getApplicationContext())){
			Toast.makeText(MainActivity.this, "当前非WIFI网络,路由检测功能只针对百度路由器，请先连接百度路由器wifi网络", Toast.LENGTH_SHORT).show();
			return;
		}
		
		
		LinearLayout layout = new LinearLayout(MainActivity.this);
		layout.setOrientation(LinearLayout.VERTICAL);

		final EditText textviewGid = new EditText(MainActivity.this);
		textviewGid.setHint("输入管理密码");
		layout.addView(textviewGid);

		AlertDialog.Builder builder = new AlertDialog.Builder(
				MainActivity.this);
		builder.setView(layout);
		builder.setPositiveButton("确定",
				new DialogInterface.OnClickListener() {

					public void onClick(DialogInterface dialog,
							int which) {
						String pwd = textviewGid
								.getText().toString();
						Log.d(TAG, "输入的密码是"+pwd);
						RouterAdminPwdCheckAsyncTask asyncTask = new RouterAdminPwdCheckAsyncTask(getApplicationContext(),pwd, 
								 new RouterListener() {

							@Override
							public void onSuccess(Object obj) {
								Log.d(TAG, "密码正确");
								
								
								
								final ProgressDialog progressdialog = new ProgressDialog(MainActivity.this);
								progressdialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
								progressdialog.setTitle("体检进行中。。。");
								progressdialog.setMessage("正在进行体检，请稍后。。。");
								//progressdialog.setIcon(R.drawable.icon_health);
								progressdialog.show();
								
								
								 RouterCheckAsyncTask asyncTask = new RouterCheckAsyncTask(getApplicationContext(), 
										 new RouterListener() {

											@Override
											public void onSuccess(Object obj) {
												RouterMeta routerMeta = (RouterMeta)obj;
												Log.d(TAG,routerMeta.getDeviceId());
												Log.d(TAG,routerMeta.getStatus()+"");
												Log.d(TAG,routerMeta.getWifiType()+"\t 当前速度："+routerMeta.getCur_speed()+"\t设备数： "+routerMeta.getDeviceNum()+"\t"
														+routerMeta.getDiskUseRate()+"\t tasknum: "+routerMeta.getTaskNum()+"\twan:"+routerMeta.getWan()+"\t network: "
														+routerMeta.getNetwork());
												progressdialog.dismiss();
												Intent intent = new Intent();
												intent.putExtra("routerMeta", routerMeta);
											    intent.setClass(MainActivity.this, RouterActivity.class);
											    startActivity(intent);
												
											}
											
											@Override
											public void  onFailure(int errCode, String errMsg){
												
												
											}

									 
								 });  
						         asyncTask.execute(getApplicationContext());  
								
							}
							@Override
							public void  onFailure(int errCode, String errMsg){
								Log.d(TAG, "密码cuowu");

//								 new AlertDialog.Builder(MainActivity.this)
//							     .setTitle("提醒")
//							     .setMessage("当前wifi网络非百度路由局域网")
//							     .show();
								if(errCode == -2){
									Log.d(TAG, "当前wifi网络非百度路由局域网");
									Toast.makeText(MainActivity.this, "当前wifi网络非百度路由局域网", Toast.LENGTH_SHORT).show();
								}
								else if(errCode == -1){
								
									Toast.makeText(MainActivity.this, "系统异常", Toast.LENGTH_SHORT).show();
								}
								else
									Toast.makeText(MainActivity.this, "管理密码输入错误", Toast.LENGTH_SHORT).show();
								
								 
							}
							
				});
				asyncTask.execute(getApplicationContext());  
					}
		});
		builder.show();


		
		
	
	}
	
	private void start_check_network() {
		
		if(NetworkCheck.isWifi(getApplicationContext())){
			
			new WifiCompute().clear_WifiInfo(getApplicationContext());
			new NetworkCollect().clearNetWork(getApplicationContext());
			new NetPing().clear_ping(getApplicationContext());
			
			setWifiInFo();
			
			
			final ProgressDialog progressdialog = new ProgressDialog(MainActivity.this);
			progressdialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			progressdialog.setTitle("体检进行中。。。");
			progressdialog.setMessage("正在进行体检，请稍后。。。");
			//progressdialog.setIcon(R.drawable.icon_health);
			progressdialog.show();
			
			
			 NetworkCheckAsyncTask asyncTask = new NetworkCheckAsyncTask(getApplicationContext(), 
					 new NetworkListener() {
	
						@Override
						public void onReceive() {
							progressdialog.dismiss();
	//						textView_device_avg_speed.setText(new NetworkCollect().getNetwork_speed(getApplicationContext()));
	//						
							Intent intent = new Intent(MainActivity.this, WifiActivity.class);
							startActivity(intent);
						
						}
						
				
				 
			 });  
	         asyncTask.execute(getApplicationContext());  
         
		}else{
				Intent intent = new Intent(MainActivity.this, WifiActivity.class);
				startActivity(intent);
		}
		
		
//		 ProgressBarAsyncTask asyncTask = new ProgressBarAsyncTask(logText, scrollView);  
//         asyncTask.execute(getApplicationContext());  


	}
	
	@SuppressWarnings("deprecation")
	public  void setWifiInFo() {
		String wserviceName = getApplicationContext().WIFI_SERVICE;
		WifiManager wm = (WifiManager) getSystemService(wserviceName);
		WifiInfo info = wm.getConnectionInfo();
		List<WifiConfiguration> confnets = wm.getConfiguredNetworks();
		DhcpInfo dhcpinfo = wm.getDhcpInfo();
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
		Log.d(TAG,"dhcpinfo"+wm.getDhcpInfo());
		
		Log.d("cookie_status",
				info.getSupplicantState() + "   " + info.describeContents());

		

		new WifiCompute().saveWifiInfo(wifisum,getApplicationContext());
		
	}
	
	private void show_network_faster_book(){
		Intent intent = new Intent(MainActivity.this, BookActivity.class);
		startActivity(intent);
	}
	
	//---检测手环---
	private void start_check_shouhuan() {
		Intent intent = new Intent(MainActivity.this, AutoTestActivity.class);
		startActivity(intent);
	}
	
	
}
