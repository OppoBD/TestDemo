package com.baidu.BasicMeta;

import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.json.JSONException;
import org.json.JSONObject;

import com.baidu.http.Transport;
import com.baidu.lib.router.RouterMeta;
import com.baidu.lib.wifi.NetPing;
import com.baidu.lib.wifi.WifiCompute;
import com.baidu.wifi.demo.Utils;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.util.Log;

import com.baidu.wearable.test.wristbandtester.AutoTestActivity;

/**
 * wifi相关信息获取
 * 
 * @author li_jing02
 * 
 */
public class BasicNetworkUpload {
	
	
	private static final String TAG = BasicNetworkUpload.class.getSimpleName();
	protected final  String URL_BASE = "http://idevice.baidu.com/function/doctor.php?method=upload_network_info";
	private static String INTERFACE = "interface";
	private static String RESULT = "result";
	private static String TIME = "time";
	private static enum  TYPE {BASIC,ROUTER,IERMU,SHOUHUAN};

	public void upload_basicNetwork(Context context){
 			JSONObject params = new JSONObject();
			try {
				params.put(Utils.UID, Utils.getString(context, Utils.getString(context, Utils.UID)));
				params.put("ctime", Utils.getString(context, Utils.getString(context, "checktime")));
				params.put(Utils.PING_PACKAGE_LOSS_RATE, Utils.getString(context, Utils.PING_PACKAGE_LOSS_RATE));
				params.put("type", TYPE.BASIC);
				params.put(new WifiCompute().NOW_CONNECTED_WIFI_IP, Utils.getString(context, new WifiCompute().NOW_CONNECTED_WIFI_IP));
				params.put(new WifiCompute().NOW_CONNECTED_WIFI_DNSIP, Utils.getString(context, new WifiCompute().NOW_CONNECTED_WIFI_DNSIP));
				params.put(new WifiCompute().NOW_CONNECTED_WIFI_ISDHCP, Utils.getString(context, new WifiCompute().NOW_CONNECTED_WIFI_ISDHCP));
				params.put(new WifiCompute().NOW_CONNECTED_WIFI_ISPROXY, Utils.getString(context, new WifiCompute().NOW_CONNECTED_WIFI_ISPROXY));
				params.put(new WifiCompute().NOW_CONNECTED_WIFI_MAC, Utils.getString(context, new WifiCompute().NOW_CONNECTED_WIFI_MAC));
				params.put(new WifiCompute().NOW_CONNECTED_WIFI_NAME, Utils.getString(context, new WifiCompute().NOW_CONNECTED_WIFI_NAME));
				params.put(new WifiCompute().NOW_CONNECTED_WIFI_SPEED, Utils.getString(context, new WifiCompute().NOW_CONNECTED_WIFI_SPEED));
				params.put(new WifiCompute().NOW_CONNECTED_WIFI_STRENGTH, Utils.getString(context, new WifiCompute().NOW_CONNECTED_WIFI_STRENGTH));
				params.put(new WifiCompute().NOW_CONNECTED_WIFI_CAPABILITY, Utils.getString(context, new WifiCompute().NOW_CONNECTED_WIFI_CAPABILITY));
		//		params.put("RTT_MDEV",new NetPing().getPingDns_stability(context));
				Transport.upload_network(context,this.URL_BASE, params);		
			
			
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
	}
	
	/*
	 * upload the processed data Stored in Utils.PROCESSED_DATA, by WangRuiqi
	 */
	public void upload_advancedNetwork(Context context){
		JSONObject params = new JSONObject();
		try {
			// insert the processed data here
			Log.d("PROCESSED_DATA", "start");
			for(Object obj : Utils.PROCESSED_DATA.keySet()){
				String value = Utils.PROCESSED_DATA.get(obj).toString();
				params.put(obj.toString(), value);
				Log.d(obj.toString(),value);
		    }
			
			Log.d(TAG,params.toString());

			Transport.upload_network(context,this.URL_BASE, params);		
		
		
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public void upload_basicRouter(Context context,RouterMeta routermeta){
			JSONObject params = new JSONObject();
		try {
			params.put(Utils.UID, Utils.getString(context, Utils.getString(context, Utils.UID)));
			params.put("ctime", Utils.getString(context, Utils.getString(context, "checktime")));
			params.put("routermeta",routermeta);
			params.put("type", TYPE.ROUTER);
			Transport.upload_network(context,this.URL_BASE, params);		
		
		
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
}
	
	//上传的手环的检测结果
	public void upload_shouhuan(Context context){
			JSONObject params = new JSONObject();
		try {
			params.put(Utils.UID, Utils.getString(context, Utils.getString(context, Utils.UID)));
			params.put("ctime", Utils.getString(context, Utils.getString(context, "checktime")));
			params.put(Utils.PING_PACKAGE_LOSS_RATE, Utils.getString(context, Utils.PING_PACKAGE_LOSS_RATE));
			params.put("type", TYPE.BASIC);
			params.put(new AutoTestActivity().NOW_CONNECTED_STATUS, Utils.getString(context, new AutoTestActivity().NOW_CONNECTED_STATUS));
			Log.d(TAG,"upload_shouhuan success");
			Transport.upload_network(context,this.URL_BASE, params);		
		
		
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
}
	
	
	protected static String constructUrl(String url, List<NameValuePair> params) {
		// params.add(new BasicNameValuePair(APP_ID, Config.APP_ID));
		// params.add(new BasicNameValuePair(AGENT_ID, Config.DEVICE_ID));

		String param = URLEncodedUtils.format(params, "UTF-8");

		String newUrl = url + "?" + param;
		// LogUtil.e(TAG, "url:" + newUrl);

		return newUrl;
	}


}
