/**
 * 
 */
package com.baidu.wearable.test.wristbandtester.testcase;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.baidu.wearable.ble.stack.BlueTooth.BlueToothCommonListener;
import com.baidu.wifi.R;
import com.baidu.wearable.test.wristbandtester.SensorTestActivity;
import com.baidu.wearable.test.wristbandtester.TestActivity;
import com.baidu.wearable.test.wristbandtester.TestActivity.TestStatusHandler;

/**
 * @author chenxixiong
 *
 */
public class SensorTest extends TestCase {

	private final String TAG = SensorTest.class.getSimpleName();
	
	/**
	 * @param context
	 * @param name
	 * @param cls
	 */
	public SensorTest(Context context) {
		super(context,context.getResources().getStringArray(R.array.test_menu)[5], 
				SensorTestActivity.class);
	}

	/* (non-Javadoc)
	 * @see com.baidu.wearable.test.wristbandtester.testcase.TestCase#onTestPrepared()
	 */
	@Override
	protected boolean onTestPrepared() {
		mProtocolHelper.registerSensorRequestHandler(mStatusHandler);
		return true;
	}

	/* (non-Javadoc)
	 * @see com.baidu.wearable.test.wristbandtester.testcase.TestCase#onTestStart()
	 */
	@Override
	protected void onTestStart(AsyncTask task) {
		mProtocolHelper.testSensor(new BlueToothCommonListener(){

			@Override
			public void onSuccess() {
				Log.d(TAG,"request sensor send ok.");
				
			}

			@Override
			public void onFailure() {
				mStatusHandler.sendEmptyMessage(TestActivity.TestStatusHandler.TEST_FAIL);
				
			}});
		mStatusHandler.sendEmptyMessageDelayed(TestStatusHandler.TEST_TIMEOUT, 3000);
	}

}
