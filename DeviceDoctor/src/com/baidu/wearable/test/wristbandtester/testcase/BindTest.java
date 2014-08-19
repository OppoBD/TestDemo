/**
 * 
 */
package com.baidu.wearable.test.wristbandtester.testcase;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Message;
import android.util.Log;

import com.baidu.wearable.ble.connectmanager.BluetoothLeStateMachine;
import com.baidu.wearable.ble.connectmanager.BluetoothState;
import com.baidu.wearable.ble.stack.BlueTooth;
import com.baidu.wearable.ble.stack.BlueTooth.BlueToothBondReceiverListener;
import com.baidu.wearable.ble.stack.BlueTooth.BlueToothCommonListener;
import com.baidu.wearable.test.wristbandtester.AlarmSetTestActivity;
import com.baidu.wearable.test.wristbandtester.BluetoothConnectionTestActivity;
import com.baidu.wifi.R;
import com.baidu.wearable.test.wristbandtester.TestActivity;
import com.baidu.wearable.test.wristbandtester.BindTestActivity;
import com.baidu.wearable.test.wristbandtester.TestActivity.TestStatusHandler;


/**
 * @author fanjingde
 *
 */
public class BindTest extends TestCase {

	private BlueTooth mBlueTooth=BlueTooth.getInstance();
	private final String TAG = BindTest.class.getSimpleName();
	public String userId="2502949117";
	public Context mContext;
	public BindTest(Context context) {
		super(context,context.getResources().getStringArray(R.array.test_menu)[13], 
				BindTestActivity.class);
	}

	/* (non-Javadoc)
	 * @see com.baidu.wearable.test.wristbandtester.testcase.TestCase#startTest()
	 */
	@Override
	public void onTestStart(AsyncTask task) {
		mBlueTooth
		.registerBondReceiverListener(new BlueToothBondReceiverListener() {

			@Override
			public void onSuccess() {
				Log.d(TAG, "registerBondReceiverListener onSuccess");
				Message msg = mStatusHandler.obtainMessage(TestStatusHandler.TEST_STATUS_CHANGED);
				msg.arg1 = BindTestActivity.DEVICE_BIND_SUCCESS;
				mStatusHandler.sendMessage(msg);
			}

			@Override
			public void onFailure() {
				Log.d(TAG, "registerBondReceiverListener  failure");
				//Intent bindIntent = new Intent(BluetoothState.ACTION_BLE_BIND_RESULT);
				//bindIntent.putExtra(BluetoothState.EXTRA_BLE_BIND_RESULT, BluetoothState.BLE_BIND_ERROR);
				
				//mContext.sendBroadcast(bindIntent);
				
				//Intent intent = new Intent(BluetoothLeStateMachine.ACTION_BLE_SM_CONNECT_COMMAND);
				//intent.putExtra(BluetoothLeStateMachine.EXTRA_BLE_SM_CONNECT_COMMAND, BluetoothLeStateMachine.BLE_SM_CONNECT_COMMAND_FINALIZE);
				//sendBroadcast(intent);			
			}

		});
		
		mBlueTooth.bind(userId, new BlueToothCommonListener() {
					@Override
					public void onSuccess() {
						Log.d(TAG,"bind send success");						
					}

					@Override
					public void onFailure() {
						Log.e(TAG,"bind send failure");
						Intent intent = new Intent(BluetoothLeStateMachine.ACTION_BLE_SM_CONNECT_COMMAND);
						intent.putExtra(BluetoothLeStateMachine.EXTRA_BLE_SM_CONNECT_COMMAND, BluetoothLeStateMachine.BLE_SM_CONNECT_COMMAND_FINALIZE);
						mContext.sendBroadcast(intent);
						
					}

				});
		
		mStatusHandler.sendEmptyMessageDelayed(TestActivity.TestStatusHandler.TEST_TIMEOUT, 3000);
	}
	
}
