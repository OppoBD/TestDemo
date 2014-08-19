package com.baidu.wearable.ble.model;

import java.util.Calendar;

import android.os.Parcel;
import android.os.Parcelable;

import com.baidu.wearable.ble.util.LogUtil;

public class Clock implements Parcelable {
	
	private final static String TAG = "Clock";
	
	public final static String CLOCK_KEY_HOUR = "hour";
	
	public final static String CLOCK_KEY_MINUTE = "minute";
	
	public final static String CLOCK_KEY_ALARM_ID = "alarm_id";
	
	private long id;

	private int year;
	
	private int month;
	
	private int day;
	
	private int hour;
	
	private int minute;
	
	private int alarm_id = -1;
	
	private boolean Mon;
	
	private boolean Tue;
	
	private boolean Wed;
	
	private boolean Thu;
	
	private boolean Fri;
	
	private boolean Sat;
	
	private boolean Sun;
	
	private boolean on;
	
	private boolean dirty;
	
	private boolean bracelet;
	
	public long getId() {
		return id;
	}
	
	public void setId(long i) {
		id = i;
	}
	
	public int getAlarmId( ) {
		return alarm_id;
	}
	
	public void setAlarmId(int id ) {
		alarm_id = id;
	}
	
	public int getYear( ) {
		return year;
	}
	
	public void setYear(int y ) {
		 year = y;
	}
	
	public int getMonth( ) {
		return month;
	}
	public void setMonth(int m ) {
		month = m;
	}
	
	public int getDay( ) {
		return day;
	}
	public void setDay(int d ) {
		day = d;
	}
	public int getHour( ) {
		return hour;
	}
	public void setHour(int h ) {
		hour = h;
	}
	public int getMinute( ) {
		return minute;
	}
	public void setMinute(int m ) {
		minute = m;
	}
	public boolean isMon( ) {
		return Mon;
	}
	public  void setMon( boolean mon) {
		 Mon = mon;
	}
	
	public boolean isTue() {
		return Tue;
	}
	
	public void setTue(boolean tue ) {
		 Tue = tue;
	}
	
	public boolean isWed( ) {
		return Wed;
	}
	
	public void setWed(boolean wed ) {
		Wed = wed;
	}
	
	public boolean isThu( ) {
		return Thu;
	}
	
	public void setThu( boolean thu) {
		Thu = thu;
	}
	
	public boolean isFri( ) {
		return Fri;
	}
	public void setFri(boolean fri ) {
		Fri = fri;
	}
	
	public boolean isSat( ) {
		return Sat;
	}
	
	public void setSat(boolean sat) {
		Sat = sat;
	}
	
	public boolean isSun( ) {
		return Sun;
	}
	
	public void  setSun( boolean sun) {
		Sun = sun;
	}
	
	public boolean isOn() {
		return on;
	}
	
	public void setOn(boolean o) {
		on = o;
	}
	
	public boolean isNetDirty() {
		return dirty;
	}
	
	public void setNetDirty(boolean d) {
		dirty = d;
	}
	
	public boolean isBraceletDirty() {
		return bracelet;
	}
	
	public void setBraceletDirty(boolean b) {
		bracelet = b;
	}
	
	public boolean isEveryDay() {
		return Sun && Mon && Tue && Wed && Thu && Fri && Sat;
	}
	
	public boolean isRepeat() {
		return Sun || Mon || Tue || Wed || Thu || Fri || Sat;
	}
	
	public boolean isExpire() {
		if(!isRepeat()) {
			Calendar calendar = Calendar.getInstance();
			long currentTime = calendar.getTimeInMillis();
			
			calendar.set(year, month - 1, day, hour, minute, 0);
			long setTime = calendar.getTimeInMillis();
			
			if (currentTime >= setTime) {
				LogUtil.d(TAG, "clock is expired.");
				return true;
			} else {
				LogUtil.d(TAG, "clock is not expired.");
				return false;
			}
		}	
		LogUtil.d(TAG, "clock is not expired.");
		return false;
	}
	
	public static final Parcelable.Creator<Clock> CREATOR = new Creator<Clock>() {  
		@Override  
		public Clock createFromParcel(Parcel source) {   
			Clock clock = new Clock(); 
			clock.id = source.readLong();
			clock.year = source.readInt();
			clock.month = source.readInt();
			clock.day = source.readInt();
			clock.hour = source.readInt();
			clock.minute = source.readInt();
			clock.alarm_id = source.readInt();
			boolean[] array = new boolean[8];
			source.readBooleanArray(array);
			clock.Sun = array[0];
			clock.Mon = array[1];
			clock.Tue = array[2];
			clock.Wed = array[3];
			clock.Thu = array[4];
			clock.Fri = array[5];
			clock.Sat = array[6];
			clock.on = array[7];
			return clock;  
		}

		@Override
		public Clock[] newArray(int size) {
			return new Clock[size];
		}  
	};

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeLong(id);
		dest.writeInt(year);
		dest.writeInt(month);
		dest.writeInt(day);
		dest.writeInt(hour);
		dest.writeInt(minute);
		dest.writeInt(alarm_id);
		boolean[] array = {Sun, Mon, Tue, Wed, Thu, Fri, Sat, on};
		dest.writeBooleanArray(array);		
	}
}
