package com.baidu.wearable.ble.stack;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import android.os.Handler;
import android.util.Log;

import com.baidu.wearable.ble.model.BlueToothSleepData;
import com.baidu.wearable.ble.model.BlueToothSleepDataSection;
import com.baidu.wearable.ble.model.BlueToothSportData;
import com.baidu.wearable.ble.model.BlueToothSportDataSection;
import com.baidu.wearable.ble.model.ClockList;
import com.baidu.wearable.ble.util.LogUtil;
import com.baidu.wearable.ble.util.TimeUtil;

public class BlueTooth {
   
	private final static String TAG = "BlueTooth";
	
	public final static int SUCCESS = 0;
	
	private final long TIMESTAMP_OF_2011 = 977616000;

	class SendMeta {
		int seqId;
		BlueToothCommonListener listener;
	}  
   
	
	private List<SendMeta> mSendMetas = new ArrayList<SendMeta>();


	private static BlueTooth mInstance;
	
	private Handler mHandler;

	static {
		System.loadLibrary("jni-bluetooth");
		
		classInitNative();
	}
	
	private static BlueToothConnectResetListener mBlueToothConnectResetListener;
   
	private static BlueToothSportReceiverListener mBlueToothSportReceiverListener;

	private static BlueToothSleepReceiverListener mBlueToothSleepReceiverListener;
	
	private static BlueToothSleepSettingReceiverListener mBlueToothSleepSettingReceiverListener;
	
	private static BlueToothDataSyncProgressListener mBlueToothDataSyncProgressListener;

	private static BlueToothBondReceiverListener mBlueToothBondReceiverListener;
	
	private static BlueToothLoginReceiverListener mBlueToothLoginReceiverListener;
	
	private static BlueToothAlarmListReceiverListener mBlueToothAlarmListReceiverListener;
	
	private static BlueToothTestModeReceiverListener mBlueToothTestModeResponseReceiverListener;
	
	private static BlueToothOTAEnterOtaModeReceiverListener mBlueToothOTAEnterOtaModeResponseReceiverListener;
	
	private static BlueToothRemoteControlReceiverListener mBlueToothRemoteControlReceiverListener;

	private BlueTooth() {
		
	}

	public static BlueTooth getInstance() {
		if (mInstance == null) {
			synchronized (BlueTooth.class) {
				if (mInstance == null) {
					mInstance = new BlueTooth();
					mInstance.init();
				}
			}
		}

		return mInstance;
	}
	
	public int init() {
		mHandler = new Handler();
		initBleStackTimer();
		
		return initNative();
		
	}

	public native static void classInitNative();
	    
	public native int initNative();
	
	public native int initBleStackNative();
	public native int finalizeBleStackNative();
	
	private native int otaEnterOTAModeNative();
	
	private native int bindNative(byte[] utf8Id);
	private native int loginNative(byte[] utf8Id);
	private native int setTimeNative(int year,int month,int day,int hour,int minute,int second);
	private native int setAlarmListNative(ClockList alarmList);
	private native int getAlarmListNative();
	private native int setSportTargetNative(int sport_target);
	private native int setUserProfileNative(boolean isMale,int age,int height,int weight);
	private native int setLinklostNative(int alert_level);
	private native int setStillAlarmNative(int enable,int steps,int minutes,int start_hour,int end_hour,boolean mon,
		boolean tue,boolean wed,boolean thu,boolean fri,boolean sat,boolean sun);
	
	private native int setLeftOrRightHandNative(boolean isLeft);
	private native int setOperationSystemNative(int os, int reserved);
	
	private native int phoneCommingNative();
	private native int phoneAnswerNative();
	private native int phoneDenyNative();
	private native int requestDataNative();
	private native int setDataSyncNative(int enable);
	
	private native int setDailySportDataNative(int dailyStep,int dailyDistance,int dailyCalory);
	 
	private native int testEchoRequestNative(byte[] data);
	private native int testChargeRequestNative();
	private native int testLedOnRequestNative(int have_mode,int mode);
	
	private native int testVibrateRequestNative();
	private native int testWriteSnRequestNative(byte[] sn);
	private native int testReadSnRequestNative();
	private native int testWriteFlagRequestNative(byte flag);
	private native int testReadFlagRequestNative();
    private native int testReadSensorRequestNative();
    private native int testEnterTestModeRequestNative();
    private native int testExitTestModeRequestNative();
    
    private native int testSetMotorBurnInRequestNative(boolean enable);
    
    private native int testSetLedBurnInRequestNative(boolean enable);
    
    
    private native int remoteControlCameraStateNative(int state);
	
	private native void bleStackTimeFireNative();
	
	
	Runnable mBleStackTimerRunnable = new Runnable() {
	    @Override 
	    public void run() {
	      bleStackTimeFire();
	      mHandler.postDelayed(mBleStackTimerRunnable, 1000);
	    }
	};
	    
	public void initBleStackTimer() {
		mBleStackTimerRunnable.run(); 
	}

	public void onSendCallback(int statusCode, long seqId) {

		LogUtil.d(TAG, "onSendCallback statusCode:" + statusCode + ", seqId:"
				+ seqId);
		LogUtil.d(TAG, "mSendMetas count:" + mSendMetas.size());
		
		
		int index = -1;
		for (int i=0; i<mSendMetas.size(); i++) {
			if (mSendMetas.get(i).seqId == seqId) {
				index = i;
				break;
			}
		}
		if (index != -1) {
			SendMeta sendMeta = mSendMetas.get(index);
			if (statusCode == SUCCESS) {
				sendMeta.listener.onSuccess();
			} else {
				sendMeta.listener.onFailure();
			}
			mSendMetas.remove(index);
		}
	}
	
	public List<BlueToothSportData> hanleSportDataDate(List<BlueToothSportData> sportDatas) {
		
		List<BlueToothSportData> newSportDatas = new ArrayList<BlueToothSportData>();
		for(int i=0; i<sportDatas.size(); i++) {
			BlueToothSportData sportData = sportDatas.get(i);
			
			if(sportData.timestamp_second < TIMESTAMP_OF_2011) {
				continue;
			}
			
			BlueToothSportData newSportData = new BlueToothSportData();
			newSportData.init();
			newSportData.date = sportData.date;
			newSportData.timestamp_second = sportData.timestamp_second;
			LogUtil.v(TAG," hanleSportDataDate date:" + newSportData.date + " timestamp:" + newSportData.timestamp_second);
			
			newSportDatas.add(newSportData);
		}
		
		for(int i=0; i<sportDatas.size(); i++) {
			BlueToothSportData sportData = sportDatas.get(i);
			if(sportData.timestamp_second < TIMESTAMP_OF_2011) {
				continue;
			}
			for(int j=0; j<sportData.sportDatas.size(); j++) {
				BlueToothSportDataSection sportDataSection = sportData.sportDatas.get(j);		
					boolean find_day = false;
					for(int k=0; k<newSportDatas.size(); k++) {
						if(sportDataSection.belongToDay(newSportDatas.get(k).timestamp_second)) {
							find_day = true;
							newSportDatas.get(k).addSection(sportDataSection);
							break;
						}
					}
					
					if(!find_day) {
						LogUtil.w(TAG, "find SportDataSection not belong to  SportData,SportDataSection timestamp:" + sportDataSection.timestamp 
								+ ", SportData timestamp_second:" + sportData.timestamp_second);
						BlueToothSportData newSportData = new BlueToothSportData();
						newSportData.init();
						newSportData.timestamp_second = sportDataSection.timestamp - sportDataSection.timestamp%(24*60*60);
						newSportData.date = TimeUtil.getDate(newSportData.timestamp_second*1000);
						
						newSportDatas.add(newSportData);
						
						sportDataSection.timestamp = sportDataSection.timestamp;
						newSportData.addSection(sportDataSection);			
					}
			}
		}
		 
		
		return newSportDatas;
	}
	
	
public List<BlueToothSleepData> hanleSleepDataDate(List<BlueToothSleepData> sleepDatas) {
	
		assert(sleepDatas.size() == 1);
		
		long origin_day_timestamp_second = sleepDatas.get(0).timestamp_second;
		
		List<BlueToothSleepData> newSleepDatas = new ArrayList<BlueToothSleepData>();
		for(int i=0; i<sleepDatas.size(); i++) {
			BlueToothSleepData sleepData = sleepDatas.get(i);
			if(sleepData.timestamp_second < TIMESTAMP_OF_2011) {
				continue;
			}
			
			BlueToothSleepData newSleepData = new BlueToothSleepData();
			newSleepData.init();
			newSleepData.date = sleepData.date;
			newSleepData.timestamp_second = sleepData.timestamp_second;
			LogUtil.v(TAG," hanleSleepDataDate date:" + newSleepData.date + " timestamp:" + newSleepData.timestamp_second);
			
			newSleepDatas.add(newSleepData);
		}
		
		for(int i=0; i<sleepDatas.size(); i++) {
			BlueToothSleepData sleepData = sleepDatas.get(i);
			if(sleepData.timestamp_second < TIMESTAMP_OF_2011) {
				continue;
			}
			for(int j=0; j<sleepData.sleepDatas.size(); j++) {
				BlueToothSleepDataSection sleepDataSection = sleepData.sleepDatas.get(j);		
					boolean find_day = false;
					for(int k=0; k<newSleepDatas.size(); k++) {
						if(sleepDataSection.minute*60 + origin_day_timestamp_second >=  newSleepDatas.get(k).timestamp_second 
								&& sleepDataSection.minute*60 + origin_day_timestamp_second <  newSleepDatas.get(k).timestamp_second + 24*60*60) {
							find_day = true;
							
							if(sleepDataSection.minute >= 24*60) {
								sleepDataSection.minute = sleepDataSection.minute%(24*60);
							}
							newSleepDatas.get(k).addSection(sleepDataSection);
							break;
						}
					}
					
					if(!find_day) {
						LogUtil.w(TAG, "find SleepDataSection not belong to  any day timestamp:" + sleepDataSection.minute);
						BlueToothSleepData newSleepData = new BlueToothSleepData();
						newSleepData.init();
						newSleepData.timestamp_second = origin_day_timestamp_second + sleepDataSection.minute*60 - (sleepDataSection.minute*60)%(24*60*60);
						newSleepData.date = TimeUtil.getDate(newSleepData.timestamp_second*1000);
						
						newSleepDatas.add(newSleepData);
						
						sleepDataSection.minute = sleepDataSection.minute%(24*60);
						newSleepData.addSection(sleepDataSection);			
					}
			}
		}
		
		
		return newSleepDatas;
	}

	public void resetBleConnect() {
		if(null != mBlueToothConnectResetListener)  {
			mBlueToothConnectResetListener.onReset();
		}
	}
	
	public void onReceiveSportData(BlueToothSportData sport) {
		List<BlueToothSportData> sportDatas = new ArrayList<BlueToothSportData>();
		sportDatas.add(sport);
		
		
		LogUtil.v(TAG,"date:" + sport.date + " timestamp:" + sport.timestamp_second);
		
		if (null != mBlueToothSportReceiverListener) {
			mBlueToothSportReceiverListener
					.onSuccess(hanleSportDataDate(sportDatas));
		}
		
	}  
	
	public void onReceiveSleepData(BlueToothSleepData sleep) {
		List<BlueToothSleepData> sleepDatas = new ArrayList<BlueToothSleepData>();
		sleepDatas.add(sleep);
		LogUtil.d(TAG,"onReceiveSleepData");
		if (null != mBlueToothSleepReceiverListener) {
			mBlueToothSleepReceiverListener
					.onSuccess(hanleSleepDataDate(sleepDatas));
		}
		
	}
	
	public void onReceiveSleepSettingData(BlueToothSleepData sleep) {
		List<BlueToothSleepData> sleepDatas = new ArrayList<BlueToothSleepData>();
		sleepDatas.add(sleep);
		LogUtil.d(TAG,"onReceiveSleepSettingData");
		if (null != mBlueToothSleepSettingReceiverListener) {
			mBlueToothSleepSettingReceiverListener
					.onSuccess(hanleSleepDataDate(sleepDatas));
		}
		
	}
	
	public void onReceiveMoreSportData() {
		requestData(new BlueToothCommonListener() {
			@Override
			public void onSuccess() {
				LogUtil.d(TAG, "request data send success");
			}

			@Override
			public void onFailure() {
				LogUtil.d(TAG, "request data send failure");
			}
			
		});
		
	}
	
	public void onReceiveSportDataSyncStart() {
		if (null != mBlueToothDataSyncProgressListener){
			mBlueToothDataSyncProgressListener.onSyncStart();
		}
		
	}
	
	public void onReceiveSportDataSyncEnd() {
		if (null != mBlueToothDataSyncProgressListener){
			mBlueToothDataSyncProgressListener.onSyncEnd();
		}
		
	}
	
	
	public void onReceiveBindResponse(int status_code) {
		if (null != mBlueToothBondReceiverListener){
			if(0 == status_code) {
				mBlueToothBondReceiverListener.onSuccess();
				
			} else {
				mBlueToothBondReceiverListener.onFailure();
			}	
		}
		
	}

	public void onReceiveLoginResponse(int status_code) {
		if (null != mBlueToothLoginReceiverListener){
			if(0 == status_code) {
				mBlueToothLoginReceiverListener.onSuccess();
				
			} else {
				mBlueToothLoginReceiverListener.onFailure();
			}	
		}
		
	}
	
	public void onReceiveAlarmList(ClockList alarmList) {
		if (null != mBlueToothAlarmListReceiverListener){
				mBlueToothAlarmListReceiverListener.onSuccess(alarmList);
		}
		
	}
	
	public void onReceiveTestModeEchoResponse(byte[] data) {
		if (null != mBlueToothTestModeResponseReceiverListener) {
			mBlueToothTestModeResponseReceiverListener
					.onTestEchoResponse(data);
		}
		
	}
	
	public void onReceiveTestModeChargeReadResponse(short voltage) {
		if (null != mBlueToothTestModeResponseReceiverListener) {
			mBlueToothTestModeResponseReceiverListener
					.onTestChargeReadResponse(voltage);
		}
		
	}
	
	public void onReceiveTestModeSnReadResponse(byte[] sn) {
		if (null != mBlueToothTestModeResponseReceiverListener) {
			mBlueToothTestModeResponseReceiverListener
					.onTestSnReadResponse(sn);
		}
		
	}
	 
	public void onReceiveTestModeFlagReadResponse(byte flag) {
		if (null != mBlueToothTestModeResponseReceiverListener) {
			mBlueToothTestModeResponseReceiverListener
					.onTestFlagReadResponse(flag);
		}
		
	}
	
	public void onReceiveTestModeSensorReadResponse(short x_axis, short y_axis, short z_axis) {
		if (null != mBlueToothTestModeResponseReceiverListener) {
			mBlueToothTestModeResponseReceiverListener
					.onTestSensorReadResponse(x_axis,y_axis,z_axis);
		}
		
	}
	
	public void onReceiveTestButton(int code, int button_id, long timestamp) {
		if (null != mBlueToothTestModeResponseReceiverListener) {
			mBlueToothTestModeResponseReceiverListener
					.onTestButton(code,button_id,timestamp);
		}
		
	}
	
	public void onReceiveOTAEnterOTAModeResponse(byte status_code,byte error_code) {
		if (null != mBlueToothOTAEnterOtaModeResponseReceiverListener) {
			mBlueToothOTAEnterOtaModeResponseReceiverListener
					.onEnterOtaModeResponse(status_code,error_code);
		}
		
	}
	
	
	public void onReceiveRemoteControlCameraTakePicture() {
		if (null != mBlueToothRemoteControlReceiverListener) {
			mBlueToothRemoteControlReceiverListener
					.onCameraTakePicture();
		}
		
	}
	
	
	public void onReceiveRemoteControlSingleClick() {
		if (null != mBlueToothRemoteControlReceiverListener) {
			mBlueToothRemoteControlReceiverListener
					.onSingleClick();
		}
		
	}
	
	public void onReceiveRemoteControlDoubleClick() {
		if (null != mBlueToothRemoteControlReceiverListener) {
			mBlueToothRemoteControlReceiverListener
					.onDoubleClick();
		}
		
	}
	
	
	public interface BlueToothConnectResetListener {

		void onReset();

	}
	
	
	public interface BlueToothCommonListener {

		void onSuccess();

		void onFailure();

	}

	public interface BlueToothSportReceiverListener {

		void onSuccess(List<BlueToothSportData> data);

		void onFailure();

	}

	public interface BlueToothSleepReceiverListener {

		void onSuccess(List<BlueToothSleepData> data);

		void onFailure();

	}
	
	public interface BlueToothSleepSettingReceiverListener {

		void onSuccess(List<BlueToothSleepData> data);

		void onFailure();

	}
	
	public interface BlueToothDataSyncProgressListener {

		void onSyncStart();

		void onSyncEnd();

	}
	
	

	public interface BlueToothBondReceiverListener {

		void onSuccess();

		void onFailure();

	}
	
	public interface BlueToothLoginReceiverListener {

		void onSuccess();

		void onFailure();

	}
	
	
	public interface BlueToothAlarmListReceiverListener {

		void onSuccess(ClockList alarmList);
	}
	
	public interface BlueToothTestModeReceiverListener {

		void onTestEchoResponse(byte[] data);
		void onTestChargeReadResponse(short voltage);
		void onTestSnReadResponse(byte[] sn);
		void onTestFlagReadResponse(byte flag);
		void onTestSensorReadResponse(short x_axis, short y_axis, short z_axis);
		void onTestButton(int code, int button_id, long timestamp);
	}	
	
	public interface BlueToothOTAEnterOtaModeReceiverListener {
		void onEnterOtaModeResponse(byte status_code,byte error_code);
	}
	
	public interface BlueToothRemoteControlReceiverListener {
		void onCameraTakePicture();
		void onSingleClick();
		void onDoubleClick();
	}
	
	public void registerResetBleConnect(
			BlueToothConnectResetListener listener) {
		mBlueToothConnectResetListener = listener;
	}

	public void registerSportReceiverListener(
			BlueToothSportReceiverListener listener) {
		mBlueToothSportReceiverListener = listener;
	}

	public void registerSleepReceiverListener(
			BlueToothSleepReceiverListener listener) {
		mBlueToothSleepReceiverListener = listener;
	}

	public void registerSleepSettingReceiverListener(
			BlueToothSleepSettingReceiverListener listener) {
		mBlueToothSleepSettingReceiverListener = listener;
	}
	
	public void registerDataSyncProgressListener(
			BlueToothDataSyncProgressListener listener) {
		mBlueToothDataSyncProgressListener = listener;
	}

	public void registerBondReceiverListener(
			BlueToothBondReceiverListener listener) {
		mBlueToothBondReceiverListener = listener;
	}
	
	public void registerLoginReceiverListener(
			BlueToothLoginReceiverListener listener) {
		mBlueToothLoginReceiverListener = listener;
	}
	 
	public void registerAlarmListReceiverListener(
			BlueToothAlarmListReceiverListener listener) {
		mBlueToothAlarmListReceiverListener = listener;
	}
	
	public void registerTestModeResponseReceiverListener(
			BlueToothTestModeReceiverListener listener) {
		mBlueToothTestModeResponseReceiverListener = listener;
	}
	
	public void registerOTAEnterOtaModeResponseReceiverListener(
			BlueToothOTAEnterOtaModeReceiverListener listener) {
		mBlueToothOTAEnterOtaModeResponseReceiverListener = listener;
	}
	
	public void registerRemoteControlReceiverListener(
			BlueToothRemoteControlReceiverListener listener) {
		mBlueToothRemoteControlReceiverListener = listener;
	}
	
	
	public void bleStackTimeFire() {
		bleStackTimeFireNative();
		 
	}
	
	public void otaEnterOTAMode(BlueToothCommonListener listener) {

		int seqId = otaEnterOTAModeNative();

		LogUtil.d(TAG, "otaEnterOTAMode seqId:" + seqId);

		if(seqId >= 0) {
			SendMeta sendMeta = new SendMeta();
			sendMeta.seqId = seqId;
			sendMeta.listener = listener;

			mSendMetas.add(sendMeta);
		} else {
			listener.onFailure();
		}
	}
  
	public void bind(String userId, BlueToothCommonListener listener) {

		byte[] utf8_id;
		try {
			utf8_id = userId.getBytes("UTF-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			LogUtil.d(TAG, "bind error");
			if(null != listener) {
				listener.onFailure();
			}
			return;
		}
		int seqId = bindNative(utf8_id);

		LogUtil.d(TAG, "bind seqId:" + seqId);
		
		if(seqId >= 0) {
			SendMeta sendMeta = new SendMeta();
			sendMeta.seqId = seqId;
			sendMeta.listener = listener;

			mSendMetas.add(sendMeta);
		} else {
			listener.onFailure();
		}

		
	}
	
	public void login(String userId, BlueToothCommonListener listener,int counter) {

		byte[] utf8_id = null;
		try {
			if(counter == 0){
				utf8_id = userId.getBytes("UTF-8");
			}else if(counter == 1){
				utf8_id = getByteArray(32,Long.valueOf(userId));
			}
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			LogUtil.d(TAG, "login error");
			if(null != listener) {
				listener.onFailure();
			}
			
			return;
		}
				
		int seqId = loginNative(utf8_id);

		LogUtil.d(TAG, "login seqId:" + seqId);

		if(seqId >= 0) {
			SendMeta sendMeta = new SendMeta();
			sendMeta.seqId = seqId;
			sendMeta.listener = listener;

			mSendMetas.add(sendMeta);
		} else {
			listener.onFailure();
		}

	}
	
	private  byte[] getByteArray(int arrayLength, long num) {
		byte[] result = new byte[arrayLength];

		int length = arrayLength;
		if (arrayLength > 4) {
			length = 4;
		}

		for (int i = length; i > 0; i--) {
			result[--arrayLength] = (byte) (num >> (8 * (length - i)));
		}
		return result;
	}

	public void setTime(BlueToothCommonListener listener) {

		Calendar calendar = Calendar.getInstance();

		int year = calendar.get(Calendar.YEAR);
		int month = calendar.get(Calendar.MONTH) + 1;
		int day = calendar.get(Calendar.DAY_OF_MONTH);

		int hour = calendar.get(Calendar.HOUR_OF_DAY);
		int minute = calendar.get(Calendar.MINUTE);
		int second = calendar.get(Calendar.SECOND);
		
		LogUtil.d(TAG, "setTime year:" + year + " month:" + month + " day:" + day + " hour:" + hour + " minute:" + minute + " second:" + second);

		int seqId = setTimeNative(year,month,day,hour,minute,second);

		LogUtil.d(TAG, "setTime seqId:" + seqId);

		if(seqId >= 0) {
			SendMeta sendMeta = new SendMeta();
			sendMeta.seqId = seqId;
			sendMeta.listener = listener;

			mSendMetas.add(sendMeta);
		} else {
			listener.onFailure();
		}
	}
	
	public void setAlarmList(ClockList alarmList,BlueToothCommonListener listener) {

		int seqId = setAlarmListNative(alarmList);

		LogUtil.d(TAG, "setAlarmList seqId:" + seqId);

		if(seqId >= 0) {
			SendMeta sendMeta = new SendMeta();
			sendMeta.seqId = seqId;
			sendMeta.listener = listener;

			mSendMetas.add(sendMeta);
		} else {
			listener.onFailure();
		}
	}
	
	public void getAlarmList(BlueToothCommonListener listener) {

		int seqId = getAlarmListNative();

		LogUtil.d(TAG, "getAlarmList seqId:" + seqId);

		if(seqId >= 0) {
			SendMeta sendMeta = new SendMeta();
			sendMeta.seqId = seqId;
			sendMeta.listener = listener;

			mSendMetas.add(sendMeta);
		} else {
			listener.onFailure();
		}
	}
	
	public void setSportTarget(BlueToothCommonListener listener,int sport_target) {

		int seqId = setSportTargetNative(sport_target);

		LogUtil.d(TAG, "setSportTarget seqId:" + seqId);

		if(seqId >= 0) {
			SendMeta sendMeta = new SendMeta();
			sendMeta.seqId = seqId;
			sendMeta.listener = listener;

			mSendMetas.add(sendMeta);
		} else {
			listener.onFailure();
		}
	}
	
	public void setUserProfile(BlueToothCommonListener listener,boolean isMale,int age,int height,int weight) {

		int seqId = setUserProfileNative(isMale,age,height,weight);

		LogUtil.d(TAG, "setUserProfile seqId:" + seqId);

		if(seqId >= 0) {
			SendMeta sendMeta = new SendMeta();
			sendMeta.seqId = seqId;
			sendMeta.listener = listener;

			mSendMetas.add(sendMeta);
		} else {
			listener.onFailure();
		}
	}
		
	public void setLinklost(BlueToothCommonListener listener,int alert_level) {

		int seqId = setLinklostNative(alert_level);

		LogUtil.d(TAG, "setLinklost seqId:" + seqId);

		if(seqId >= 0) {
			SendMeta sendMeta = new SendMeta();
			sendMeta.seqId = seqId;
			sendMeta.listener = listener;

			mSendMetas.add(sendMeta);
		} else {
			listener.onFailure();
		}
	}
	
	public void setStillAlarm(BlueToothCommonListener listener,int enable,int steps, int minutes,int start_hour, int end_hour,boolean mon,
			boolean tue, boolean wed, boolean thu, boolean fri, boolean sat, boolean sun) {
		
		int seqId = setStillAlarmNative( enable, steps, minutes, start_hour, end_hour, mon,
				 tue, wed, thu, fri, sat, sun);

		LogUtil.d(TAG, "setStillAlarm seqId:" + seqId);

		if(seqId >= 0) {
			SendMeta sendMeta = new SendMeta();
			sendMeta.seqId = seqId;
			sendMeta.listener = listener;

			mSendMetas.add(sendMeta);
		} else {
			listener.onFailure();
		}
	}
	
	
	 public void setLeftOrRightHand(BlueToothCommonListener listener, boolean isLeft) {
			
			int seqId = setLeftOrRightHandNative(isLeft);

			LogUtil.d(TAG, "isLeft seqId:" + seqId);

			if(seqId >= 0) {
				SendMeta sendMeta = new SendMeta();
				sendMeta.seqId = seqId;
				sendMeta.listener = listener;

				mSendMetas.add(sendMeta);
			} else {
				listener.onFailure();
			}

		}
	
	 public void setOperationSystem(BlueToothCommonListener listener, int os, int reserved) {
			
	        int seqId = setOperationSystemNative(os, reserved);

			LogUtil.d(TAG, "setOS seqId:" + seqId);

			if(seqId >= 0) {
				SendMeta sendMeta = new SendMeta();
				sendMeta.seqId = seqId;
				sendMeta.listener = listener;

				mSendMetas.add(sendMeta);
			} else {
				listener.onFailure();
			}

		}

	public void phoneComming(BlueToothCommonListener listener) {
		
		int seqId = phoneCommingNative();
		
		LogUtil.d(TAG, "phoneComming seqId:" + seqId);

		if(seqId >= 0) {
			SendMeta sendMeta = new SendMeta();
			sendMeta.seqId = seqId;
			sendMeta.listener = listener;

			mSendMetas.add(sendMeta);
		} else {
			listener.onFailure();
		}
	}
	
	public void phoneAnswer(BlueToothCommonListener listener) {
		
		int seqId = phoneAnswerNative();
		
		LogUtil.d(TAG, "phoneAnswer seqId:" + seqId);

		if(seqId >= 0) {
			SendMeta sendMeta = new SendMeta();
			sendMeta.seqId = seqId;
			sendMeta.listener = listener;

			mSendMetas.add(sendMeta);
		} else {
			listener.onFailure();
		}
	}
	
	public void phoneDeny(BlueToothCommonListener listener) {

		int seqId = phoneDenyNative();
		
		LogUtil.d(TAG, "phoneDeny seqId:" + seqId);

		if(seqId >= 0) {
			SendMeta sendMeta = new SendMeta();
			sendMeta.seqId = seqId;
			sendMeta.listener = listener;

			mSendMetas.add(sendMeta);
		} else {
			listener.onFailure();
		}
	}

	public void requestData(BlueToothCommonListener listener) {

		int seqId = requestDataNative();

		LogUtil.d(TAG, "requestData seqId:" + seqId);

		if(seqId >= 0) {
			SendMeta sendMeta = new SendMeta();
			sendMeta.seqId = seqId;
			sendMeta.listener = listener;

			mSendMetas.add(sendMeta);
		} else {
			listener.onFailure();
		}
	}
	
	public void setDataSync(BlueToothCommonListener listener,int enable) {

		int seqId = setDataSyncNative(enable);

		LogUtil.d(TAG, "setDataSync seqId:" + seqId);

		if(seqId >= 0) {
			SendMeta sendMeta = new SendMeta();
			sendMeta.seqId = seqId;
			sendMeta.listener = listener;

			mSendMetas.add(sendMeta);
		} else {
			listener.onFailure();
		}
	}
	
	
	public void setDailySportData(BlueToothCommonListener listener,int dailyStep,int dailyDistance,int dailyCalory) {

		int seqId = setDailySportDataNative(dailyStep,dailyDistance,dailyCalory);

		LogUtil.d(TAG, "setDailySportData seqId:" + seqId);

		if(seqId >= 0) {
			SendMeta sendMeta = new SendMeta();
			sendMeta.seqId = seqId;
			sendMeta.listener = listener;

			mSendMetas.add(sendMeta);
		} else {
			listener.onFailure();
		}
	}
	
	public void testEchoRequest(byte[] data, BlueToothCommonListener listener) {
				
		int seqId = testEchoRequestNative(data);

		LogUtil.d(TAG, "testEchoRequestNative seqId:" + seqId);

		if(seqId >= 0) {
			SendMeta sendMeta = new SendMeta();
			sendMeta.seqId = seqId;
			sendMeta.listener = listener;

			mSendMetas.add(sendMeta);
		} else {
			listener.onFailure();
		}

	}
	
	
	public void testChargeRequest(BlueToothCommonListener listener) {
		
		int seqId = testChargeRequestNative();

		LogUtil.d(TAG, "testChargeRequest seqId:" + seqId);

		if(seqId >= 0) {
			SendMeta sendMeta = new SendMeta();
			sendMeta.seqId = seqId;
			sendMeta.listener = listener;

			mSendMetas.add(sendMeta);
		} else {
			listener.onFailure();
		}

	}
	
    
	public void testLedOnRequest(BlueToothCommonListener listener) {
		
		int seqId = testLedOnRequestNative(0,0);

		LogUtil.d(TAG, "testLedOnRequest seqId:" + seqId);

		if(seqId >= 0) {
			SendMeta sendMeta = new SendMeta();
			sendMeta.seqId = seqId;
			sendMeta.listener = listener;

			mSendMetas.add(sendMeta);
		} else {
			listener.onFailure();
		}

	}
	
    public void testLedOnRequest(BlueToothCommonListener listener,int mode) {
		
		int seqId = testLedOnRequestNative(1,mode);

		LogUtil.d(TAG, "testLedOnRequest seqId:" + seqId);

		if(seqId >= 0) {
			SendMeta sendMeta = new SendMeta();
			sendMeta.seqId = seqId;
			sendMeta.listener = listener;

			mSendMetas.add(sendMeta);
		} else {
			listener.onFailure();
		}

	}
	
	public void testVibrateRequest(BlueToothCommonListener listener) {
		
		int seqId = testVibrateRequestNative();

		LogUtil.d(TAG, "testVibrateRequest seqId:" + seqId);

		if(seqId >= 0) {
			SendMeta sendMeta = new SendMeta();
			sendMeta.seqId = seqId;
			sendMeta.listener = listener;

			mSendMetas.add(sendMeta);
		} else {
			listener.onFailure();
		}

	}
	public void testWriteSnRequest(byte[] sn,BlueToothCommonListener listener) {
		
		int seqId = testWriteSnRequestNative(sn);

		LogUtil.d(TAG, "testVibrateRequest seqId:" + seqId);

		if(seqId >= 0) {
			SendMeta sendMeta = new SendMeta();
			sendMeta.seqId = seqId;
			sendMeta.listener = listener;

			mSendMetas.add(sendMeta);
		} else {
			listener.onFailure();
		}

	}
	
	public void testReadSnRequest(BlueToothCommonListener listener) {
		
		int seqId = testReadSnRequestNative();

		LogUtil.d(TAG, "testVibrateRequest seqId:" + seqId);

		if(seqId >= 0) {
			SendMeta sendMeta = new SendMeta();
			sendMeta.seqId = seqId;
			sendMeta.listener = listener;

			mSendMetas.add(sendMeta);
		} else {
			listener.onFailure();
		}

	} 
	
	public void testWriteFlagRequest(byte flag,BlueToothCommonListener listener) {
		
		int seqId = testWriteFlagRequestNative(flag);

		LogUtil.d(TAG, "testWriteFlagRequest seqId:" + seqId);

		if(seqId >= 0) {
			SendMeta sendMeta = new SendMeta();
			sendMeta.seqId = seqId;
			sendMeta.listener = listener;

			mSendMetas.add(sendMeta);
		} else {
			listener.onFailure();
		}

	}
	
	
	public void testReadFlagRequest(BlueToothCommonListener listener) {
		
		int seqId = testReadFlagRequestNative();

		LogUtil.d(TAG, "testReadFlagRequest seqId:" + seqId);

		if(seqId >= 0) {
			SendMeta sendMeta = new SendMeta();
			sendMeta.seqId = seqId;
			sendMeta.listener = listener;

			mSendMetas.add(sendMeta);
		} else {
			listener.onFailure();
		}

	}
	
    public void testReadSensorRequest(BlueToothCommonListener listener) {
		
		int seqId = testReadSensorRequestNative();

		LogUtil.d(TAG, "testReadSensorRequest seqId:" + seqId);

		if(seqId >= 0) {
			SendMeta sendMeta = new SendMeta();
			sendMeta.seqId = seqId;
			sendMeta.listener = listener;

			mSendMetas.add(sendMeta);
		} else {
			listener.onFailure();
		}

	}
    
    public void testEnterTestModeRequest(BlueToothCommonListener listener) {
		
		int seqId = testEnterTestModeRequestNative();

		LogUtil.d(TAG, "testEnterTestModeRequest seqId:" + seqId);

		if(seqId >= 0) {
			SendMeta sendMeta = new SendMeta();
			sendMeta.seqId = seqId;
			sendMeta.listener = listener;

			mSendMetas.add(sendMeta);
		} else {
			listener.onFailure();
		}

	}
    
    public void testExitTestModeRequest(BlueToothCommonListener listener) {
		
		int seqId = testExitTestModeRequestNative();

		LogUtil.d(TAG, "testExitTestModeRequest seqId:" + seqId);

		if(seqId >= 0) {
			SendMeta sendMeta = new SendMeta();
			sendMeta.seqId = seqId;
			sendMeta.listener = listener;

			mSendMetas.add(sendMeta);
		} else {
			listener.onFailure();
		}

	}
    
    
    public void testSetMotorBurnInRequest(BlueToothCommonListener listener, boolean enable) {
		
		int seqId = testSetMotorBurnInRequestNative(enable);

		LogUtil.d(TAG, "testSetMotorBurnInRequest seqId:" + seqId);

		if(seqId >= 0) {
			SendMeta sendMeta = new SendMeta();
			sendMeta.seqId = seqId;
			sendMeta.listener = listener;

			mSendMetas.add(sendMeta);
		} else {
			listener.onFailure();
		}

	}
    
    
    public void testSetLedBurnInRequest(BlueToothCommonListener listener, boolean enable) {
		
		int seqId = testSetLedBurnInRequestNative(enable);

		LogUtil.d(TAG, "testSetLedBurnInRequest seqId:" + seqId);

		if(seqId >= 0) {
			SendMeta sendMeta = new SendMeta();
			sendMeta.seqId = seqId;
			sendMeta.listener = listener;

			mSendMetas.add(sendMeta);
		} else {
			listener.onFailure();
		}

	}
    
    //state: 0:  camera app in front 1: camera app in background
    public void remoteControlCameraState(BlueToothCommonListener listener,int state) {

		int seqId = remoteControlCameraStateNative(state);

		LogUtil.d(TAG, "remoteControlCameraState seqId:" + seqId);

		if(seqId >= 0) {
			SendMeta sendMeta = new SendMeta();
			sendMeta.seqId = seqId;
			sendMeta.listener = listener;

			mSendMetas.add(sendMeta);
		} else {
			listener.onFailure();
		}
	}
    
}
