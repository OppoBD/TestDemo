package com.baidu.wearable.ble.model;

public class BlueToothSportDataSection {

	public long timestamp;
	
	public int step;
	
	public float calorie;
	
	public float distance;
	
	public void setTimestamp(long day_second,int offset) {
		timestamp = day_second + offset* 15 * 60;
	}
	
	public void setStep(int s) {
		step = s;
	}
	
	public void setCalory(float c) {
		calorie = c;
	}
	
	public void setDistance(float s) {
		distance = s;
	}
	
	public boolean belongToDay(long day_second) {
		
		if(timestamp >= day_second  &&
				(timestamp - day_second) < 24*60*60 ) {
			return true;
		} else {
			return false;
		}
		
	}

}
