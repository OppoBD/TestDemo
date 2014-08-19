package com.baidu.http;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.TelephonyManager;

/**
 * net util class
 * @author zhangjieting
 *
 */
public class NetworkCheck {
	
	private static ConnectivityManager mConnManager;
	/**  
     * 网络是否可用  
     */ 
	public static boolean isNetworkAvailable(Context context) {   
		ConnectivityManager connManager = getConnectivityManager(context);   
	    if (connManager == null) {   
	    } else {   
	    	NetworkInfo[] info = connManager.getAllNetworkInfo();   
	        if (info != null) {   
	        	for (int i = 0; i < info.length; i++) {   
	            	if (info[i].getState() == NetworkInfo.State.CONNECTED) {   
	                	return true;   
	                }   
	            }   
	        }   
	    }   
	    return false;   
	}   
	
	/**  
     * wifi是否打开  
     */   
	public static boolean isWifiEnabled(Context context) {   
		ConnectivityManager connManager = getConnectivityManager(context); 
	    TelephonyManager mgrTel = (TelephonyManager) context   
	                .getSystemService(Context.TELEPHONY_SERVICE);   
	    return ((connManager.getActiveNetworkInfo() != null && connManager   
	                .getActiveNetworkInfo().getState() == NetworkInfo.State.CONNECTED) || mgrTel   
	                .getNetworkType() == TelephonyManager.NETWORK_TYPE_UMTS);
	}
	
	 /**  
     * 判断当前网络是否是wifi网络  
     * if(activeNetInfo.getType()==ConnectivityManager.TYPE_MOBILE) { //判断3G网  
     *   
     * @param context  
     * @return boolean  
     */   
    public static boolean isWifi(Context context) {   
        ConnectivityManager connManager = getConnectivityManager(context);   
        NetworkInfo activeNetInfo = connManager.getActiveNetworkInfo();   
        if (activeNetInfo != null   
                && activeNetInfo.getType() == ConnectivityManager.TYPE_WIFI) {   
            return true;   
        }   
        return false;   
    }   
    
    /**  
     * 判断当前网络是否是3G网络  
     *   
     * @param context  
     * @return boolean  
     */   
    public static boolean is3G(Context context) {   
        ConnectivityManager connManager = getConnectivityManager(context);   
        NetworkInfo activeNetInfo = connManager.getActiveNetworkInfo();   
        if (activeNetInfo != null   
                && activeNetInfo.getType() == ConnectivityManager.TYPE_MOBILE) {   
            return true;   
        }   
        return false;   
    }  
    
    private static ConnectivityManager getConnectivityManager(Context context) {
    	if(null == mConnManager) {
    		mConnManager = (ConnectivityManager) context   
            	.getSystemService(Context.CONNECTIVITY_SERVICE);  
    	}
    	
    	return mConnManager;
    }

}

