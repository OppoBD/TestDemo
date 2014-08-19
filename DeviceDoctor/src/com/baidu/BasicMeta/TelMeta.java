package com.baidu.BasicMeta;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import com.baidu.wifi.demo.MainActivity;

import android.app.ActivityManager;
import android.app.ActivityManager.MemoryInfo;
import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.telephony.TelephonyManager;
import android.text.format.Formatter;
import android.util.Log;

public class TelMeta extends MainActivity{
	
	/**
	 * 获取android当前可用内存大小 
	 */
	 private String getAvailMemory() {// 获取android当前可用内存大小   
		  
	        ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);  
	        MemoryInfo mi = new MemoryInfo();  
	        am.getMemoryInfo(mi);  
	        //mi.availMem; 当前系统的可用内存   
	  
	        return Formatter.formatFileSize(getBaseContext(), mi.availMem);// 将获取的内存大小规格化   
	    }  

	/**
	 * 获得系统总内存
	 */
	private String getTotalMemory() {
		String str1 = "/proc/meminfo";// 系统内存信息文件 
		String str2;
		String[] arrayOfString;
		long initial_memory = 0;

		try {
		FileReader localFileReader = new FileReader(str1);
		BufferedReader localBufferedReader = new BufferedReader(
		localFileReader, 8192);
		str2 = localBufferedReader.readLine();// 读取meminfo第一行，系统总内存大小 

		arrayOfString = str2.split("\\s+");
		for (String num : arrayOfString) {
		Log.d(str2, num + "\t");
		}

		initial_memory = Integer.valueOf(arrayOfString[1]).intValue() * 1024;// 获得系统总内存，单位是KB，乘以1024转换为Byte 
		localBufferedReader.close();

		} catch (IOException e) {
		}
		return Formatter.formatFileSize(getBaseContext(), initial_memory);// Byte转换为KB或者MB，内存大小规格化 
		}

	/**
	 * 获得手机屏幕宽高
	 * @return
	 */
	public String getHeightAndWidth(){
		int width=getWindowManager().getDefaultDisplay().getWidth();
		int heigth=getWindowManager().getDefaultDisplay().getHeight();
		String str=width+""+heigth+"";
		return str;
	}
	/**
	 * 获取IMEI号，IESI号，手机型号
	 */
	private void getInfo() {
       TelephonyManager mTm = (TelephonyManager)this.getSystemService(TELEPHONY_SERVICE);
        String imei = mTm.getDeviceId();
        String imsi = mTm.getSubscriberId();
        String mtype = android.os.Build.MODEL; // 手机型号
        String mtyb= android.os.Build.BRAND;//手机品牌
        String numer = mTm.getLine1Number(); // 手机号码，有的可得，有的不可得
        Log.i("text", "手机IMEI号："+imei+"手机IESI号："+imsi+"手机型号："+mtype+"手机品牌："+mtyb+"手机号码"+numer);
    }
	
	private String get_IMEI(){
		  TelephonyManager mTm = (TelephonyManager)this.getSystemService(TELEPHONY_SERVICE);
	      String imei = mTm.getDeviceId();
	      return imei;
	}
	
	private String get_OS_MODEL(){
		  String mtype = android.os.Build.MODEL; // 手机型号
	      return mtype;
	}
	private String get_OS_BRAND(){
		  String mtype = android.os.Build.BRAND; // 手机品牌
	      return mtype;
	}
	private String get_PHONE_NO(){             //手机号
		TelephonyManager mTm = (TelephonyManager)this.getSystemService(TELEPHONY_SERVICE);
		  String num = mTm.getLine1Number();
	      return num;
	}
	/**
	 * .获取手机MAC地址
	 * 只有手机开启wifi才能获取到mac地址
	 */
	private String getMacAddress(){
        String result = "";
        WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        result = wifiInfo.getMacAddress();
        Log.i("text", "手机macAdd:" + result);
        return result;
    }
	/**
	 * 手机CPU信息
	 */
	private String[] getCpuInfo() {
        String str1 = "/proc/cpuinfo";
        String str2 = "";
        String[] cpuInfo = {"", ""};  //1-cpu型号  //2-cpu频率
        String[] arrayOfString;
        try {
            FileReader fr = new FileReader(str1);
            BufferedReader localBufferedReader = new BufferedReader(fr, 8192);
            str2 = localBufferedReader.readLine();
            arrayOfString = str2.split("\\s+");
            for (int i = 2; i < arrayOfString.length; i++) {
                cpuInfo[0] = cpuInfo[0] + arrayOfString[i] + " ";
            }
            str2 = localBufferedReader.readLine();
            arrayOfString = str2.split("\\s+");
            cpuInfo[1] += arrayOfString[2];
            localBufferedReader.close();
        } catch (IOException e) {
        }
        Log.i("text", "cpuinfo:" + cpuInfo[0] + " " + cpuInfo[1]);
        return cpuInfo;
    }
}