/**
 * 
 */
package com.baidu.wearable.test.wristbandtester.testcase;

import android.content.Context;
import android.os.AsyncTask;

import com.baidu.wearable.test.wristbandtester.ButtonTestActivity;
import com.baidu.wifi.R;

/**
 * @author chenxixiong
 *
 */
public class ButtonTest extends TestCase {

	/**
	 * @param context
	 * @param name
	 * @param cls
	 */
	public ButtonTest(Context context) {
		super(context, context.getResources().getStringArray(R.array.test_menu)[12], 
				ButtonTestActivity.class);
	}

	/* (non-Javadoc)
	 * @see com.baidu.wearable.test.wristbandtester.testcase.TestCase#onTestPrepared()
	 */
	@Override
	protected boolean onTestPrepared() {
		mProtocolHelper.registerButtonEventReceivedHandler(mStatusHandler);
		return true;
	}

	/* (non-Javadoc)
	 * @see com.baidu.wearable.test.wristbandtester.testcase.TestCase#onTestStart(android.os.AsyncTask)
	 */
	@Override
	protected void onTestStart(AsyncTask task) {

	}

}
