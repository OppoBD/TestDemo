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
import android.os.Bundle;
import android.os.Message;
import android.util.Log;

import com.baidu.wearable.ble.connectmanager.BluetoothLeStateMachine;
import com.baidu.wearable.ble.model.Clock;
import com.baidu.wearable.ble.stack.BlueTooth;
import com.baidu.wearable.ble.stack.BlueTooth.BlueToothAlarmListReceiverListener;
import com.baidu.wearable.ble.stack.BlueTooth.BlueToothBondReceiverListener;
import com.baidu.wearable.ble.stack.BlueTooth.BlueToothCommonListener;
import com.baidu.wearable.test.wristbandtester.BindTestActivity;
import com.baidu.wearable.test.wristbandtester.BluetoothConnectionTestActivity;
import com.baidu.wifi.R;
import com.baidu.wearable.test.wristbandtester.TestActivity;
import com.baidu.wearable.test.wristbandtester.UserProfileTestActivity;
import com.baidu.wearable.test.wristbandtester.TestActivity.TestStatusHandler;
import com.baidu.wearable.ble.model.ClockList;

/**
 * @author fanjingde 2014-03-29
 *
 */
public class UserProfileTest extends TestCase {

	private BlueTooth mBlueTooth=BlueTooth.getInstance();
	private final String TAG = UserProfileTest.class.getSimpleName();
	private int age = 28;
	private int height = 175;
	private int weight = 65;
		
	public UserProfileTest(Context context) {
		super(context,context.getResources().getStringArray(R.array.test_menu)[15], 
				UserProfileTestActivity.class);		
	}

	/* (non-Javadoc)
	 * @see com.baidu.wearable.test.wristbandtester.testcase.TestCase#startTest()
	 */
	@Override
	public void onTestStart(AsyncTask task) {
		
		mBlueTooth.setUserProfile(new BlueToothCommonListener() {
					@Override
					public void onSuccess() {
						Log.d(TAG,"Userprofile send success");
						Bundle bundle = new Bundle();
				    	bundle.putInt(UserProfileTestActivity.age, age);
				    	bundle.putInt(UserProfileTestActivity.height, height);
				    	bundle.putInt(UserProfileTestActivity.weight, weight);	
						Message msg = mStatusHandler.obtainMessage(TestStatusHandler.TEST_STATUS_CHANGED);
						msg.arg1 = UserProfileTestActivity.PROFILE_SET_SUCCESS;
						msg.setData(bundle);
						mStatusHandler.sendMessage(msg);
					
					}

					@Override
					public void onFailure() {
						Log.e(TAG,"Alarm send failure");
						Intent intent = new Intent(BluetoothLeStateMachine.ACTION_BLE_SM_CONNECT_COMMAND);
						intent.putExtra(BluetoothLeStateMachine.EXTRA_BLE_SM_CONNECT_COMMAND, BluetoothLeStateMachine.BLE_SM_CONNECT_COMMAND_FINALIZE);
						mContext.sendBroadcast(intent);
						
					}

				}, true, age, height, weight);
		
		mStatusHandler.sendEmptyMessageDelayed(TestActivity.TestStatusHandler.TEST_TIMEOUT, 3000);
	}
	
}
