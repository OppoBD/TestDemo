package com.baidu.wearable.ble.model;

import java.util.ArrayList;
import java.util.List;

import com.baidu.wearable.ble.util.LogUtil;


public class ClockList {

	private final static String TAG = "ClockList";
	
	public List<Clock> clocks = new ArrayList<Clock>();
	
	public int getListSize() {
		return clocks.size();
	}
	
	public Clock getClock(int index) {
		return clocks.get(index);
	}
	
	public void setClocks(List<Clock> clocks) {
		this.clocks = clocks;
	}
	
	public List<Clock> getClocks() {
		return clocks;
	}
	
	public void addClock(Clock clock) {
		clocks.add(clock);
	}
	
	public void init() {
		LogUtil.v(TAG,"init");
		if(null == clocks) {
			clocks = new ArrayList<Clock>();
		}
		
	}

}
