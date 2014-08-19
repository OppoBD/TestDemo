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
import com.baidu.wearable.test.wristbandtester.BindTestActivity;
import com.baidu.wifi.R;
import com.baidu.wearable.test.wristbandtester.TestActivity;
import com.baidu.wearable.test.wristbandtester.AlarmSetTestActivity;
import com.baidu.wearable.test.wristbandtester.TestActivity.TestStatusHandler;
import com.baidu.wearable.ble.model.ClockList;

/**
 * @author fanjingde 2014-03-28
 *
 */
public class AlarmSetTest extends TestCase {

	private BlueTooth mBlueTooth=BlueTooth.getInstance();
	private final String TAG = AlarmSetTest.class.getSimpleName();
	
	public AlarmSetTest(Context context) {
		super(context,context.getResources().getStringArray(R.array.test_menu)[16], 
				AlarmSetTestActivity.class);		
	}

	/* (non-Javadoc)
	 * @see com.baidu.wearable.test.wristbandtester.testcase.TestCase#startTest()
	 */
	@Override
	public void onTestStart(AsyncTask task) {
		
		Calendar calendar = Calendar.getInstance();
		int year = calendar.get(Calendar.YEAR);
		int month = calendar.get(Calendar.MONTH);
		int day = calendar.get(Calendar.DAY_OF_MONTH);
		int hour = calendar.get(Calendar.HOUR_OF_DAY);
		int minute = calendar.get(Calendar.MINUTE);
		ClockList clockList = new ClockList();				
		Clock clock = new Clock();		
		clock.setYear(year);
		clock.setMonth(month+1);
		clock.setDay(day);
		if((minute+2) >= 60){
			clock.setHour(hour+1);
			clock.setMinute(minute-58);
		} else {
			clock.setHour(hour);
			clock.setMinute(minute+2);
		}
		Log.d(TAG, "Year="+ clock.getYear() +" month="+ clock.getMonth() +" day="+ clock.getDay() +"hour=" +
		clock.getHour() + "minute=" + clock.getMinute());
		clock.setOn(true);
		clockList.addClock(clock);
		
		mBlueTooth.setAlarmList(clockList, new BlueToothCommonListener() {
					@Override
					public void onSuccess() {
						Log.d(TAG,"Alarm send success");						
						Message msg = mStatusHandler.obtainMessage(TestStatusHandler.TEST_STATUS_CHANGED);
						msg.arg1 = AlarmSetTestActivity.ALARM_SET_SUCCESS;
						mStatusHandler.sendMessage(msg);
					}

					@Override
					public void onFailure() {
						Log.e(TAG,"Alarm send failure");
						Intent intent = new Intent(BluetoothLeStateMachine.ACTION_BLE_SM_CONNECT_COMMAND);
						intent.putExtra(BluetoothLeStateMachine.EXTRA_BLE_SM_CONNECT_COMMAND, BluetoothLeStateMachine.BLE_SM_CONNECT_COMMAND_FINALIZE);
						mContext.sendBroadcast(intent);
						
					}

				});
		
		mStatusHandler.sendEmptyMessageDelayed(TestActivity.TestStatusHandler.TEST_TIMEOUT, 3000);
	}
	
}
