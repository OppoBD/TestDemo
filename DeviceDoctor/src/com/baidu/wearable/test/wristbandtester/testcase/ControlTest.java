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
import com.baidu.wearable.ble.stack.BlueTooth.BlueToothRemoteControlReceiverListener;
import com.baidu.wearable.test.wristbandtester.AlarmSetTestActivity;
import com.baidu.wearable.test.wristbandtester.BindTestActivity;
import com.baidu.wifi.R;
import com.baidu.wearable.test.wristbandtester.TestActivity;
import com.baidu.wearable.test.wristbandtester.ControlTestActivity;
import com.baidu.wearable.test.wristbandtester.TestActivity.TestStatusHandler;
import com.baidu.wearable.ble.model.ClockList;

/**
 * @author fanjingde 2014-04-18
 *
 */
public class ControlTest extends TestCase {

	private BlueTooth mBlueTooth=BlueTooth.getInstance();
	private final String TAG = ControlTest.class.getSimpleName();
	
	
	public ControlTest(Context context) {
		super(context,context.getResources().getStringArray(R.array.test_menu)[31], 
				ControlTestActivity.class);		
	}

	/* (non-Javadoc)
	 * @see com.baidu.wearable.test.wristbandtester.testcase.TestCase#startTest()
	 */
	@Override
	public void onTestStart(AsyncTask task) {
		
//		mBlueTooth
//		.registerRemoteControlReceiverListener(new BlueToothRemoteControlReceiverListener() {
//
//			@Override
//			public void onCameraTakePicture() {
//				Log.d(TAG, "onCameraTakePicture:");
//				Message msg = mStatusHandler.obtainMessage(TestStatusHandler.TEST_STATUS_CHANGED);
//				msg.arg1 = ControlCameraTestActivity.CAMERA_CONTROL_SUCCESS;
//				mStatusHandler.sendMessage(msg);
//			}
//			
//			@Override
//			public void onSingleClick() {
//				Log.d(TAG, "onCameraTakePicture:");
//			}
//			
//			@Override
//			public void onDoubleClick() {
//				Log.d(TAG, "onCameraTakePicture:");
//			}
//
//		});
		
		
		mBlueTooth.remoteControlCameraState(new BlueToothCommonListener() {
				@Override
				public void onSuccess() {
					Log.d(TAG,"Control camera send success");
					Message msg = mStatusHandler.obtainMessage(TestStatusHandler.TEST_STATUS_CHANGED);
					msg.arg1 = ControlTestActivity.CAMERA_CONTROL_SUCCESS;
					mStatusHandler.sendMessage(msg);
				}
	
				@Override
				public void onFailure() {
					Log.e(TAG,"Control camera send failure");
					
				}
		}, 1);
		
		mStatusHandler.sendEmptyMessageDelayed(TestActivity.TestStatusHandler.TEST_TIMEOUT, 3000);
	}
	
}
