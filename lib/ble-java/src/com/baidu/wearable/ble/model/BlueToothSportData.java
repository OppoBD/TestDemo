package com.baidu.wearable.ble.model;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import com.baidu.wearable.ble.util.LogUtil;
import com.baidu.wearable.ble.util.TimeUtil;

public class BlueToothSportData {

	private final static String TAG = "BlueToothSportData";
	public String date;
	
	public long timestamp_second;
	
	public void setDate(int year,int month,int day) {
		LogUtil.v(TAG,"setDate year:" + year + " month:" + month + " day:" + day );
		Calendar calendar = Calendar.getInstance();
		//Calendar的月份是从0开始的，year和day从1开始
		calendar.set(year, month -1, day, 0, 0, 0);
		long timestamp = calendar.getTimeInMillis();
		date = TimeUtil.getDate(timestamp);
		timestamp_second = (timestamp/1000);
	}
	
	public long getSecond() {
		return timestamp_second;
	}
	
	public void addSection(BlueToothSportDataSection section) {
		if(null == sportDatas) {
			sportDatas = new ArrayList<BlueToothSportDataSection>();
		}
		sportDatas.add(section);
		
	}
	public List<BlueToothSportDataSection> sportDatas = null;
	
	public void init() {
		LogUtil.v(TAG,"init");
		if(null == sportDatas) {
			LogUtil.v(TAG,"sportDatas is null");
			sportDatas = new ArrayList<BlueToothSportDataSection>();
		}
		
	}

}
