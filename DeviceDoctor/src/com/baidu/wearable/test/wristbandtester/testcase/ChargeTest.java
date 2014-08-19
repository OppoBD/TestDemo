/**
 * 
 */
package com.baidu.wearable.test.wristbandtester.testcase;

import android.content.Context;
import android.os.AsyncTask;

import com.baidu.wearable.ble.stack.BlueTooth.BlueToothCommonListener;
import com.baidu.wearable.test.wristbandtester.ChargeTestActivity;
import com.baidu.wifi.R;
import com.baidu.wearable.test.wristbandtester.TestActivity.TestStatusHandler;

/**
 * @author chenxixiong
 *
 */
public class ChargeTest extends TestCase {

	public int mVoltage = 0;
	
	public ChargeTest(Context context) {
		super(context,
				context.getResources().getStringArray(R.array.test_menu)[6], 
				ChargeTestActivity.class);
	}

	/* (non-Javadoc)
	 * @see com.baidu.wearable.test.wristbandtester.testcase.TestCase#onTestPrepared()
	 */
	@Override
	protected boolean onTestPrepared() {
		mProtocolHelper.registerVoltageRequestHandler(mStatusHandler);
		return true;
	}

	/* (non-Javadoc)
	 * @see com.baidu.wearable.test.wristbandtester.testcase.TestCase#onTestStart()
	 */
	@Override
	protected void onTestStart(AsyncTask task) {
		mVoltage = 0;
		mProtocolHelper.testCharge(new BlueToothCommonListener(){

			@Override
			public void onSuccess() {
			}

			@Override
			public void onFailure() {
				mStatusHandler.sendEmptyMessage(TestStatusHandler.TEST_FAIL);
			}});

	}


}
