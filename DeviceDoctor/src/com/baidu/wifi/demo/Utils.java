package com.baidu.wifi.demo;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import com.baidu.book.BookList_Construct;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.preference.PreferenceManager;

public class Utils {
    public static final String TAG = "WifiActivity";
    public static final String BDUSS = "bduss";
    public static  String UID = "uid";
    public static  String UNAME = "uname";
//    public static  long connect2Net_speed = 0;
//    public static  long ping_router_apeed = 0;
	public final static int ERROR_INT_DEFAULT = -1;
	public final static String ERROR_STRING_DEFAULT = "error";
	public final static int PING_AVG_BASE = 100; 
	public final static int PING_MDEV_BASE = 50;
	public final static float NETWORK_SPEED_BASE = (float) 0.6;
//	public final static int PING_AVG_BASE = 30;
	
	
	public final static String NEWLINE = "<br>";
	public final static String PASS_HEAD = "<font color='green'>";
	public final static String END = "</font>";
	public final static String FAIL_HEAD = "<font color='red'>";
	
	public static String PING_PACKAGE_LOSS_RATE = "PING_PACKAGE_LOSS_RATE";
	public static String PING_RTT_TIME = "PING_RTT_TIME";
	
	public static String DNS_hijacking = "www.123456qwerty.com";
//	public static String DNS_hijacking = "www.hao123.com";
    
    public static final String EXTRA_MESSAGE = "message";

    public static String logStringCache = "";
    
    public static int CHECK_PASS_NUM = 0;
    public static int CHECK_FAIL_NUM = 0;
    
    public static Map<String, String> PROCESSED_DATA=new HashMap<String, String>();
    
    public static BookList_Construct  booklist= new BookList_Construct();

    public static String getString(Context context,String key) {
        SharedPreferences sp = PreferenceManager
                .getDefaultSharedPreferences(context);
        return sp.getString(key, Utils.ERROR_STRING_DEFAULT);
    }

    public static void saveString(Context context, String key,String value) {
        SharedPreferences sp = PreferenceManager
                .getDefaultSharedPreferences(context);
     
        Editor editor = sp.edit();
        editor.putString(key, value);
        editor.commit();
    }
    
    public static int getInteger(Context context,String key) {
        SharedPreferences sp = PreferenceManager
                .getDefaultSharedPreferences(context);
        return sp.getInt(key, ERROR_INT_DEFAULT);
    }

    public static void saveInteger(Context context, String key,int value) {
        SharedPreferences sp = PreferenceManager
                .getDefaultSharedPreferences(context);
     
        Editor editor = sp.edit();
        editor.putInt(key, value);
        editor.commit();
    }
    

	public static HashSet<String> getSet(Context context,String key) {
		
        SharedPreferences sp = PreferenceManager
                .getDefaultSharedPreferences(context);
        HashSet<String> set = new HashSet<String>(); 
        set = (HashSet<String>)sp.getStringSet(key, null);
        return set;
    }

    public static void saveSet(Context context, String key,HashSet<String> set) {
    	
        SharedPreferences sp = PreferenceManager
                .getDefaultSharedPreferences(context);
     
        Editor editor = sp.edit();
        editor.putStringSet(key, set);
        editor.commit();
    }
    
    public static void clearSP(Context context, String key) {
    	
        SharedPreferences sp = PreferenceManager
                .getDefaultSharedPreferences(context);
     
        Editor editor = sp.edit();
        editor.remove(key);
        editor.commit();
    }
    
    
    public static String getNowTime(){
		Calendar calendar = Calendar.getInstance();
		int year = calendar.get(Calendar.YEAR);
		int month = calendar.get(Calendar.MONTH);
		int day = calendar.get(Calendar.DAY_OF_MONTH);
		int hour = calendar.get(Calendar.HOUR);
		int miniute = calendar.get(Calendar.MINUTE);
		int second = calendar.get(Calendar.SECOND);
		return year+"-"+month+"-"+day+" "+hour+":"+miniute+":"+second;
    }
    
    public static String makePassReturn(String fun_name,String res){
    		CHECK_PASS_NUM ++;
    		PROCESSED_DATA.put(fun_name, "1");	// insert fun_name and the responding data
    	return PASS_HEAD+res+END;
    }
    public static String makeFailReturn(String fun_name,String res){
		CHECK_FAIL_NUM ++;
		PROCESSED_DATA.put(fun_name, "0");	// insert fun_name and the responding data
	return FAIL_HEAD+res+END;
    }
    
    public static String makePassReturn(String fun_name,String res,int weight){
		CHECK_PASS_NUM += weight;
	return res;
    }
    public static String makeFailReturn(String fun_name,String res,int weight){
    		CHECK_FAIL_NUM += weight;
    	return res;
    }
    
    public static String makeNormalReturn(String res){
		
	return res;
}
    
  //不能全是相同的数字或者字母（如：000000、111111、aaaaaa） 全部相同返回true
    public static boolean equalStr(String numOrStr){
    boolean flag = true;
    char str = numOrStr.charAt(0);
    for (int i = 0; i < numOrStr.length(); i++) {
    if (str != numOrStr.charAt(i)) {
    flag = false;
    break;
    }
    }
    return flag;
    }
    //不能是连续的数字--递增（如：123456、12345678）连续数字返回true
    public static boolean isOrderNumeric_Increase(String numOrStr){
    boolean flag = true;//如果全是连续数字返回true
    boolean isNumeric = true;//如果全是数字返回true
    for (int i = 0; i < numOrStr.length(); i++) {
    if (!Character.isDigit(numOrStr.charAt(i))) {
    isNumeric = false;
    break;
    }
    }
    if (isNumeric) {//如果全是数字则执行是否连续数字判断
    for (int i = 0; i < numOrStr.length(); i++) {
    if (i > 0) {//判断如123456
    int num = Integer.parseInt(numOrStr.charAt(i)+"");
    int num_ = Integer.parseInt(numOrStr.charAt(i-1)+"")+1;
    if (num != num_) {
    flag = false;
    break;
    }
    }
    }
    } else {
    flag = false;
    }
    return flag;
    }
    //不能是连续的数字--递减（如：987654、876543）连续数字返回true
    public static boolean isOrderNumeric_Inverte (String numOrStr){
    boolean flag = true;//如果全是连续数字返回true
    boolean isNumeric = true;//如果全是数字返回true
    for (int i = 0; i < numOrStr.length(); i++) {
    if (!Character.isDigit(numOrStr.charAt(i))) {
    isNumeric = false;
    break;
    }
    }
    if (isNumeric) {//如果全是数字则执行是否连续数字判断
    for (int i = 0; i < numOrStr.length(); i++) {
    if (i > 0) {//判断如654321
    int num = Integer.parseInt(numOrStr.charAt(i)+"");
    int num_ = Integer.parseInt(numOrStr.charAt(i-1)+"")-1;
    if (num != num_) {
    flag = false;
    break;
    }
    }
    }
    } else {
    flag = false;
    }
    return flag;
    }

}
