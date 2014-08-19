/**
 * 
 */
package com.baidu.wearable.test.wristbandtester.testcase;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Message;

import com.baidu.wearable.ble.stack.BlueTooth.BlueToothCommonListener;
import com.baidu.wearable.test.wristbandtester.LedTestActivity;
import com.baidu.wifi.R;
import com.baidu.wearable.test.wristbandtester.TestActivity;

/**
 * @author chenxixiong
 *
 */
public class LedTest extends TestCase {

	public LedTest(Context context) {
		super(context, 
				context.getResources().getStringArray(R.array.test_menu)[1], 
				LedTestActivity.class);
	}

	@Override
	public void onTestStart(AsyncTask task) {
		mProtocolHelper.testLed(new BlueToothCommonListener(){

			@Override
			public void onSuccess() {
				Message msg = mStatusHandler.obtainMessage(TestActivity.TestStatusHandler.TEST_STATUS_CHANGED);
				msg.arg1 = LedTestActivity.EVENT_CMD_SEND_SUCCESS;
				mStatusHandler.sendMessage(msg);
			}

			@Override
			public void onFailure() {
				Message msg = mStatusHandler.obtainMessage(TestActivity.TestStatusHandler.TEST_STATUS_CHANGED);
				msg.arg1 = LedTestActivity.EVENT_CMD_SEND_FAIL;
				mStatusHandler.sendMessage(msg);
			}});
		mStatusHandler.sendEmptyMessageDelayed(TestActivity.TestStatusHandler.TEST_TIMEOUT, 3000);
	}
}
