/**
 * 
 */
package com.baidu.wearable.test.wristbandtester.testcase;

import java.util.ArrayList;
import java.util.List;
import java.util.Calendar;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Message;
import android.util.Log;

import com.baidu.wearable.ble.connectmanager.BluetoothLeStateMachine;
import com.baidu.wearable.ble.model.Clock;
import com.baidu.wearable.ble.stack.BlueTooth;
import com.baidu.wearable.ble.stack.BlueTooth.BlueToothAlarmListReceiverListener;
import com.baidu.wearable.ble.stack.BlueTooth.BlueToothBondReceiverListener;
import com.baidu.wearable.ble.stack.BlueTooth.BlueToothCommonListener;
import com.baidu.wearable.test.wristbandtester.AlarmSetTestActivity;
import com.baidu.wearable.test.wristbandtester.BindTestActivity;
import com.baidu.wearable.test.wristbandtester.TestActivity;
import com.baidu.wearable.test.wristbandtester.AlarmReadTestActivity;
import com.baidu.wearable.test.wristbandtester.TestActivity.TestStatusHandler;
import com.baidu.wearable.ble.model.ClockList;
import com.baidu.wifi.R;

/**
 * @author fanjingde 2014-04-02
 *
 */
public class AlarmReadTest extends TestCase {

	private BlueTooth mBlueTooth=BlueTooth.getInstance();
	private final String TAG = AlarmReadTest.class.getSimpleName();
	
	
	public AlarmReadTest(Context context) {
		super(context,context.getResources().getStringArray(R.array.test_menu)[27], 
				AlarmReadTestActivity.class);		
	}

	/* (non-Javadoc)
	 * @see com.baidu.wearable.test.wristbandtester.testcase.TestCase#startTest()
	 */
	@Override
	public void onTestStart(AsyncTask task) {
		
		mBlueTooth
		.registerAlarmListReceiverListener(new BlueToothAlarmListReceiverListener() {

			@Override
			public void onSuccess(ClockList alarmList) {
				Log.d(TAG, "Read alarm success, count:" + alarmList.getListSize());
				Log.d(TAG, "registerAlarmListReceiverListener onSuccess");
				Message msg = mStatusHandler.obtainMessage(TestStatusHandler.TEST_STATUS_CHANGED);
				msg.arg1 = AlarmReadTestActivity.ALARM_READ_SUCCESS;
				msg.obj = alarmList;
				mStatusHandler.sendMessage(msg);
			}

		});
		
		
		mBlueTooth.getAlarmList(new BlueToothCommonListener() {
				@Override
				public void onSuccess() {
					Log.d(TAG,"GetAlarm send success");
				}
	
				@Override
				public void onFailure() {
					Log.e(TAG,"GetAlarmsend failure");
					
				}
		});
		
		mStatusHandler.sendEmptyMessageDelayed(TestActivity.TestStatusHandler.TEST_TIMEOUT, 3000);
	}
	
}
