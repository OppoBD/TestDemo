package com.baidu.http;

import java.text.DecimalFormat;

import android.content.Context;
import android.util.Log;

import com.baidu.lib.wifi.WifiCompute;
import com.baidu.lib.wifi.WifiDetails;
import com.baidu.wifi.demo.Utils;

/**
 * 网络类
 * 
 * @li_jing02
 * 
 */
public class NetworkCollect {
	
    /** log tag. */
    private static final String TAG = NetworkCollect.class.getSimpleName();
    
    
    
	public String getNetwork_IsConnected(Context context) {
		String fun_name =Thread.currentThread().getStackTrace()[2].getMethodName(); 
		boolean bl = true;
		for(int i=0;i<Transport.connect_url_arr.length;i++){
			int connected_result = Utils.getInteger(context,Transport.connect_url_arr[i]+"_result");
			if(connected_result == Utils.ERROR_INT_DEFAULT)
				bl = false;			
		}
		
		if(bl)
			return Utils.makePassReturn(fun_name,"通过");
			//return "外网连通性检测："+ Utils.PASS;
		else 
			return Utils.makeFailReturn(fun_name,"异常");
	}
	
	public double getNetwork_speed(Context context) {
		String fun_name =Thread.currentThread().getStackTrace()[2].getMethodName(); 
		int time_ms = 0;
		int htmlsize =0;
		double speed =0;
		for(int i=0;i<Transport.connect_url_arr.length;i++){
			int connected_speed = Utils.getInteger(context,Transport.connect_url_arr[i]+"_speed");
			int length = Utils.getInteger(context, Transport.connect_url_arr[i]+"_htmlsize");
			Log.d(TAG, "speed is: "+connected_speed);
			if(connected_speed >=0) {
				time_ms += connected_speed;
				htmlsize += length;
				}
			else{
				Utils.makeFailReturn(fun_name,"悲了个剧的，连百度都没法访问，赶紧查看解决方案吧",10);
				return -1;
				}
			//	return Utils.makeFailReturn("悲了个剧的，连百度都没法访问，赶紧查看解决方案吧",10);
		}
		
		speed = ((double)htmlsize/(1024*1024))/((double)time_ms/1000);
		int tmp = (int)(speed*100);
		speed = (double)tmp/100;
	
//		DecimalFormat df=new DecimalFormat("0.0");
//		speed = Float.parseFloat(df.format((htmlsize/(1024*1024))/(time_ms/1000)));
		Log.d(TAG, "speed:"+speed +" htmlsize: "+htmlsize+"time_ms:"+time_ms + "tmp: "+tmp);
		
		if(speed >= Utils.NETWORK_SPEED_BASE)
			Utils.makePassReturn(fun_name,speed+"",3);
		//	return "平均访问外网速度:"+speed_ms+"ms "+Utils.PASS;
		else 
			Utils.makeFailReturn(fun_name,speed+"",3);
		//	return "平均访问外网速度:"+speed_ms+"ms "+Utils.FAIL;
		 
		 
		return speed;
	}
	
	public String comments(int score,double speed){
		
		if(speed < 0 )
			return "悲剧，连百度都访问不了，赶紧查看解决方案吧！";
		else if(score<=50 && speed<Utils.NETWORK_SPEED_BASE){
			return "您属于老弱病残之类，网速龟速，赶紧修复一下吧";
		}
		else if(score<=50 && speed>=Utils.NETWORK_SPEED_BASE){
			return "您的健康状态实属堪忧，抓紧修复一下吧！";
		}
		else if(score>50 && score <=80 && speed<Utils.NETWORK_SPEED_BASE){
			return "您的健康状态实属堪忧，抓紧修复一下吧！";
		}
		else if(score>50 && score <=80  && speed>=Utils.NETWORK_SPEED_BASE){
			return "您的健康状况一般，需要加油哦";
		}
		else if(score>80 && speed<Utils.NETWORK_SPEED_BASE){
			return "您的健康状态还不错，继续保持哦";
		}
		else
			return "您的健康状态非常棒，把小伙伴们远远甩在后面了！";	
		
	}
	
	
	public void clearNetWork(Context context){
		
		for(int i=0;i<Transport.connect_url_arr.length;i++){
			Utils.clearSP(context,Transport.connect_url_arr[i]+"_result");
			Utils.clearSP(context,Transport.connect_url_arr[i]+"_speed");
		}

	}
	
	
}