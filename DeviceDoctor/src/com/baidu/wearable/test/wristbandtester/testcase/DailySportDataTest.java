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
import com.baidu.wearable.ble.stack.BlueTooth.BlueToothSportReceiverListener;
import com.baidu.wearable.test.wristbandtester.BindTestActivity;
import com.baidu.wifi.R;
import com.baidu.wearable.test.wristbandtester.TestActivity;
import com.baidu.wearable.test.wristbandtester.DailySportDataTestActivity;
import com.baidu.wearable.test.wristbandtester.TestActivity.TestStatusHandler;
import com.baidu.wearable.ble.model.ClockList;

/**
 * @author fanjingde 2014-03-29
 *
 */
public class DailySportDataTest extends TestCase {

	private BlueTooth mBlueTooth=BlueTooth.getInstance();
	private final String TAG = DailySportDataTest.class.getSimpleName();
	int dailyStep=2990;
	int dailyDistance=1;
	int dailyCalory=30;
		
	public DailySportDataTest(Context context) {
		super(context,context.getResources().getStringArray(R.array.test_menu)[26], 
				DailySportDataTestActivity.class);		
	}

	/* (non-Javadoc)
	 * @see com.baidu.wearable.test.wristbandtester.testcase.TestCase#startTest()
	 */
	@Override
	public void onTestStart(AsyncTask task) {
		
//		mBlueTooth
//		.registerSportReceiverListener(new BlueToothSportReceiverListener() {
//
//			@Override
//			public void onSuccess() {
//				Log.d(TAG, "registerBondReceiverListener onSuccess");
//				Message msg = mStatusHandler.obtainMessage(TestStatusHandler.TEST_STATUS_CHANGED);
//				msg.arg1 = BindTestActivity.DEVICE_BIND_SUCCESS;
//				mStatusHandler.sendMessage(msg);
//			}
//
//			@Override
//			public void onFailure() {
//				Log.d(TAG, "registerBondReceiverListener  failure");
//				//Intent bindIntent = new Intent(BluetoothState.ACTION_BLE_BIND_RESULT);
//				//bindIntent.putExtra(BluetoothState.EXTRA_BLE_BIND_RESULT, BluetoothState.BLE_BIND_ERROR);
//				
//				//mContext.sendBroadcast(bindIntent);
//				
//				//Intent intent = new Intent(BluetoothLeStateMachine.ACTION_BLE_SM_CONNECT_COMMAND);
//				//intent.putExtra(BluetoothLeStateMachine.EXTRA_BLE_SM_CONNECT_COMMAND, BluetoothLeStateMachine.BLE_SM_CONNECT_COMMAND_FINALIZE);
//				//sendBroadcast(intent);			
//			}
//
//		});
		
		
		mBlueTooth.setDailySportData(new BlueToothCommonListener() {
					@Override
					public void onSuccess() {
						Log.d(TAG,"DailySportData send success");
						Message msg = mStatusHandler.obtainMessage(TestStatusHandler.TEST_STATUS_CHANGED);
						msg.arg1 = DailySportDataTestActivity.DAILYSPORTDATA_SET_SUCCESS;
						mStatusHandler.sendMessage(msg);
					}

					@Override
					public void onFailure() {
						Log.e(TAG,"DailySportData send failure");
						Intent intent = new Intent(BluetoothLeStateMachine.ACTION_BLE_SM_CONNECT_COMMAND);
						intent.putExtra(BluetoothLeStateMachine.EXTRA_BLE_SM_CONNECT_COMMAND, BluetoothLeStateMachine.BLE_SM_CONNECT_COMMAND_FINALIZE);
						mContext.sendBroadcast(intent);
						
					}

				}, dailyStep, dailyDistance, dailyCalory);
		
		mStatusHandler.sendEmptyMessageDelayed(TestActivity.TestStatusHandler.TEST_TIMEOUT, 3000);
	}
	
}
