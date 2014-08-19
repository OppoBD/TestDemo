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
import com.baidu.wearable.ble.model.BlueToothSportData;
import com.baidu.wearable.ble.model.BlueToothSportDataSection;
import com.baidu.wearable.ble.model.Clock;
import com.baidu.wearable.ble.stack.BlueTooth;
import com.baidu.wearable.ble.stack.BlueTooth.BlueToothAlarmListReceiverListener;
import com.baidu.wearable.ble.stack.BlueTooth.BlueToothBondReceiverListener;
import com.baidu.wearable.ble.stack.BlueTooth.BlueToothDataSyncProgressListener;
import com.baidu.wearable.ble.stack.BlueTooth.BlueToothSportReceiverListener;
import com.baidu.wearable.ble.stack.BlueTooth.BlueToothCommonListener;
import com.baidu.wearable.test.wristbandtester.BindTestActivity;
import com.baidu.wifi.R;
import com.baidu.wearable.test.wristbandtester.TestActivity;
import com.baidu.wearable.test.wristbandtester.RequestDataTestActivity;
import com.baidu.wearable.test.wristbandtester.TestActivity.TestStatusHandler;
import com.baidu.wearable.ble.model.ClockList;

/**
 * @author fanjingde 2014-04-02
 *
 */
public class RequestDataTest extends TestCase {

	private BlueTooth mBlueTooth=BlueTooth.getInstance();
	private final String TAG = RequestDataTest.class.getSimpleName();
	public List<BlueToothSportDataSection> datas;
	
	public RequestDataTest(Context context) {
		super(context,context.getResources().getStringArray(R.array.test_menu)[23], 
				RequestDataTestActivity.class);		
	}

	/* (non-Javadoc)
	 * @see com.baidu.wearable.test.wristbandtester.testcase.TestCase#startTest()
	 */
	@Override
	public void onTestStart(AsyncTask task) {
		mBlueTooth.
		registerSportReceiverListener(new BlueToothSportReceiverListener() {
			
			@Override
			public void onSuccess(List<BlueToothSportData> datas) {
				Log.d(TAG, "receive sport data success, count:" + datas.size());
				BlueToothSportData data = datas.get(0);
				Log.d(TAG, "step =" + data.sportDatas.get(0).step);
				Message msg = mStatusHandler.obtainMessage(TestStatusHandler.TEST_STATUS_CHANGED);
				msg.arg1 = RequestDataTestActivity.DATA_READ_SUCCESS;
				msg.obj = datas;
				mStatusHandler.sendMessage(msg);
			}

			@Override
			public void onFailure() {
				Log.d(TAG, "receive sport data failure");
			}
		});

		mBlueTooth.setDataSync(new BlueToothCommonListener() {
			@Override
			public void onSuccess() {
				Log.d(TAG,"setDataSync send success");
			}

			@Override
			public void onFailure() {
				Log.e(TAG,"setDataSync send failure");
				
			}

		}, 1);
		
		mStatusHandler.sendEmptyMessageDelayed(TestActivity.TestStatusHandler.TEST_TIMEOUT, 3000);
	}
	
}
