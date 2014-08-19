package com.baidu.lib.wifi;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;


import com.baidu.lib.router.RouterCheckAsyncTask;
import com.baidu.lib.router.RouterListener;
import com.baidu.lib.router.RouterMeta;
import com.baidu.wifi.demo.Utils;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;


public class NetPing{
	private final static String TAG =  NetPing.class.getSimpleName();
/*
public class NetPingAsyncTask  extends AsyncTask<Object, Integer, Void>{
	
	private final static String TAG =  NetPingAsyncTask.class.getSimpleName();
    private  Context mContext;
    private RouterListener mListener; 
    private String mHost;

   
    public NetPingAsyncTask(Context context, String host,RouterListener listener) {  
        super();  
        mContext = context;
        mListener = listener;
        mHost = host;
    }  
	
	@Override
	protected Void doInBackground(Object... arg0) {
		Log.d(TAG, "start ping doInBackground" );
		Ping(mContext,mHost);
		return null;		
	}
	*/
    
	public static int Ping(Context context,String host) {
		String result = "";
		int status =-1;
		Process p;
		try {
			// ping -c 3 -w 100 中 ，-c 是指ping的次数 3是指ping 3次 ，-w 100
			// 以秒为单位指定超时间隔，是指超时时间为100秒
			Log.d(TAG, "start ping" +host);
		//	p = Runtime.getRuntime().exec("ping -c 10 -w 100 " + Utils.getString(context, new WifiCompute().NOW_CONNECTED_WIFI_GATEWAY));
			p = Runtime.getRuntime().exec("ping -c 3 -w 100 " + host);
			
			status = p.waitFor();
			InputStream input = p.getInputStream();
			BufferedReader in = new BufferedReader(new InputStreamReader(input));
			StringBuffer buffer = new StringBuffer();
			String line = "";
			while ((line = in.readLine()) != null) {
				buffer.append(line);
			}
			Log.d(TAG, "host ping result" + host+"==="+ buffer.toString());
			Log.d(TAG, "ping status"+status);
	//		savePingDns(context,buffer.toString());
		//	Log.d(TAG, buffer.toString());
			
			if(status ==0)
			{
				savePingDns(context,host,buffer.toString());
			}
			else
			{
				Utils.saveString(context, host+Utils.PING_PACKAGE_LOSS_RATE, Utils.ERROR_STRING_DEFAULT);
				Utils.saveString(context, host+Utils.PING_RTT_TIME, Utils.ERROR_STRING_DEFAULT);
				
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return status;
	}
	
	public static void savePingDns(Context context,String host,String buffer){
		
		String loss_rate = host+Utils.PING_PACKAGE_LOSS_RATE;
		String rtt_time = host+Utils.PING_RTT_TIME;
		if(!(buffer.indexOf("packet loss") >= 0 && buffer.indexOf("ms") >0)){
			Utils.saveString(context, loss_rate, Utils.ERROR_STRING_DEFAULT);
			Utils.saveString(context, rtt_time, Utils.ERROR_STRING_DEFAULT);
			return;
		}
		
		String result = buffer.substring(buffer.lastIndexOf("---"));
		String []item = result.split(",");
		for(int i=0;i<item.length;i++){
			int index_loss = item[i].indexOf("packet loss");
			if( index_loss > 0 && item[i].substring(0, index_loss).trim().length() >0){
//				Log.d(TAG, "1111"+item[i]);
//				Log.d(TAG, "2222"+item[i].substring(0, index_loss).trim());
				Utils.saveString(context, loss_rate, item[i].substring(0, index_loss).trim());
			}
			int index_start_rtt_time = item[i].indexOf("=");
			int index_end_rtt_time = item[i].indexOf(" ms");
			if(index_start_rtt_time > 0 && index_end_rtt_time >0 ){
				Utils.saveString(context, rtt_time, item[i].substring(index_start_rtt_time, index_end_rtt_time));
			}
		}
		
	}
	

	
	public String getPingDns_LossRate(Context context,String host){
		String fun_name =Thread.currentThread().getStackTrace()[2].getMethodName(); 
		String loss_rate = Utils.getString(context, host+Utils.PING_PACKAGE_LOSS_RATE);
		if(loss_rate.equals(Utils.ERROR_STRING_DEFAULT))
			return Utils.makeFailReturn(fun_name,"失败");
		else if(Integer.parseInt(loss_rate.replaceAll("%", "")) >=2 )
			return Utils.makeFailReturn(fun_name,"失败");
		else
			return Utils.makePassReturn(fun_name,"通过");
		
	}
	
	public String getPingDns_RTT_AVG(Context context,String host){
		String fun_name =Thread.currentThread().getStackTrace()[2].getMethodName(); 
		String rtt_time = Utils.getString(context, host+Utils.PING_RTT_TIME);
		if(!rtt_time.equals(Utils.ERROR_STRING_DEFAULT)){
		String rtt_avg = rtt_time.split("/")[1];
		if(Double.parseDouble(rtt_avg) <Utils.PING_AVG_BASE )
			return Utils.makePassReturn(fun_name,"通过");
		//	return "AVG_RTT_TIME:" +rtt_avg+"ms "+Utils.PASS;
		else
			//return "AVG_RTT_TIME:" +rtt_avg+"ms "+Utils.FAIL;	
			return Utils.makeFailReturn(fun_name,"失败");
		}
		//return "AVG_RTT_TIME:" +"none "+Utils.FAIL;	
		return Utils.makeFailReturn(fun_name,"失败");
	}
	
//	public String getPingDns_stability(Context context){
//		String rtt_time = Utils.getString(context, Utils.PING_RTT_TIME);
//		if(!rtt_time.equals(Utils.ERROR_STRING_DEFAULT)){
//		String rtt_mdev = rtt_time.split("/")[3];
//		Log.d(TAG, "rtt_mdev: "+rtt_mdev);
//		if(Double.parseDouble(rtt_mdev) < Utils.PING_MDEV_BASE )
//		//	return "RTT_MDEV" +rtt_mdev+"ms "+Utils.PASS;
//			return Utils.makePassReturn("通过");
//		else
//		//	return "RTT_MDEV:" +rtt_mdev+"ms "+Utils.FAIL;	
//			return Utils.makeFailReturn("失败");
//		}
//			//return "RTT_MDEV:" +"none "+Utils.FAIL;	
//			return Utils.makeFailReturn("失败");
//	}
	
	public void clear_ping(Context context){
		Utils.clearSP(context, new WifiCompute().NOW_CONNECTED_WIFI_GATEWAY+Utils.PING_PACKAGE_LOSS_RATE);
		Utils.clearSP(context, new WifiCompute().NOW_CONNECTED_WIFI_GATEWAY+Utils.PING_RTT_TIME);
		Utils.clearSP(context, Utils.DNS_hijacking+Utils.PING_PACKAGE_LOSS_RATE);
	}
		
}