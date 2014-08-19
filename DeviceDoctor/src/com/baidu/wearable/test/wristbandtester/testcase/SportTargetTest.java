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
import com.baidu.wearable.test.wristbandtester.SportTargetTestActivity;
import com.baidu.wearable.test.wristbandtester.TestActivity.TestStatusHandler;
import com.baidu.wearable.ble.model.ClockList;

/**
 * @author fanjingde 2014-03-28
 *
 */
public class SportTargetTest extends TestCase {

	private BlueTooth mBlueTooth=BlueTooth.getInstance();
	private final String TAG = LostTest.class.getSimpleName();
	int sport_target=3000;
		
	public SportTargetTest(Context context) {
		super(context,context.getResources().getStringArray(R.array.test_menu)[25], 
				SportTargetTestActivity.class);		
	}

	/* (non-Javadoc)
	 * @see com.baidu.wearable.test.wristbandtester.testcase.TestCase#startTest()
	 */
	@Override
	public void onTestStart(AsyncTask task) {
//		mBlueTooth.setDataSync(new BlueToothCommonListener() {
//			@Override
//			public void onSuccess() {
//				Log.d(TAG,"setDataSync send success");
//			}
//
//			@Override
//			public void onFailure() {
//				Log.e(TAG,"setDataSync send failure");
//				
//			}
//
//		}, 1);
		
		mBlueTooth.setSportTarget(new BlueToothCommonListener() {
					@Override
					public void onSuccess() {
						Log.d(TAG,"SportTarget send success");
						Message msg = mStatusHandler.obtainMessage(TestStatusHandler.TEST_STATUS_CHANGED);
						msg.arg1 = SportTargetTestActivity.SPORTTARGET_SET_SUCCESS;
						mStatusHandler.sendMessage(msg);
					}

					@Override
					public void onFailure() {
						Log.e(TAG,"SportTarget send failure");
						Intent intent = new Intent(BluetoothLeStateMachine.ACTION_BLE_SM_CONNECT_COMMAND);
						intent.putExtra(BluetoothLeStateMachine.EXTRA_BLE_SM_CONNECT_COMMAND, BluetoothLeStateMachine.BLE_SM_CONNECT_COMMAND_FINALIZE);
						mContext.sendBroadcast(intent);
						
					}

				}, sport_target);
		
		mStatusHandler.sendEmptyMessageDelayed(TestActivity.TestStatusHandler.TEST_TIMEOUT, 3000);
	}
	
}
