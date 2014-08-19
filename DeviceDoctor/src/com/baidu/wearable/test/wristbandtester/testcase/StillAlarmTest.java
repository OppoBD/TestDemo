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
import com.baidu.wearable.test.wristbandtester.StillAlarmTestActivity;
import com.baidu.wearable.test.wristbandtester.TestActivity.TestStatusHandler;
import com.baidu.wearable.ble.model.ClockList;

/**
 * @author fanjingde 2014-03-31
 *
 */
public class StillAlarmTest extends TestCase {

	private BlueTooth mBlueTooth=BlueTooth.getInstance();
	private final String TAG = StillAlarmTest.class.getSimpleName();
	
	Calendar calendar = Calendar.getInstance();
	int year = calendar.get(Calendar.YEAR);
	int month = calendar.get(Calendar.MONTH);
	int day = calendar.get(Calendar.DAY_OF_MONTH);
	int hour = calendar.get(Calendar.HOUR);
	int minute = calendar.get(Calendar.MINUTE);
	ClockList clockList = new ClockList();
	Clock clock = new Clock();
	
	
	public StillAlarmTest(Context context) {
		super(context,context.getResources().getStringArray(R.array.test_menu)[18], 
				StillAlarmTestActivity.class);		
	}

	/* (non-Javadoc)
	 * @see com.baidu.wearable.test.wristbandtester.testcase.TestCase#startTest()
	 */
	@Override
	public void onTestStart(AsyncTask task) {
		mBlueTooth.setStillAlarm(new BlueToothCommonListener() {
					@Override
					public void onSuccess() {
						Log.d(TAG,"StillAlarm send success");
						Log.d(TAG, "registerAlarmListReceiverListener onSuccess");
						Message msg = mStatusHandler.obtainMessage(TestStatusHandler.TEST_STATUS_CHANGED);
						msg.arg1 = StillAlarmTestActivity.STILLALARM_SET_SUCCESS;
						mStatusHandler.sendMessage(msg);
					}

					@Override
					public void onFailure() {
						Log.e(TAG,"Alarm send failure");
						Intent intent = new Intent(BluetoothLeStateMachine.ACTION_BLE_SM_CONNECT_COMMAND);
						intent.putExtra(BluetoothLeStateMachine.EXTRA_BLE_SM_CONNECT_COMMAND, BluetoothLeStateMachine.BLE_SM_CONNECT_COMMAND_FINALIZE);
						mContext.sendBroadcast(intent);
						
					}

				}, 1, 5, 1, 8, 20, true, true, true, true, true, true, true);
		
		mStatusHandler.sendEmptyMessageDelayed(TestActivity.TestStatusHandler.TEST_TIMEOUT, 3000);
	}
	
}
