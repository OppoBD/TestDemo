/**
 * 
 */
package com.baidu.wearable.test.wristbandtester.testcase;

import android.content.Context;
import android.os.AsyncTask;

import com.baidu.wifi.R;
import com.baidu.wearable.test.wristbandtester.RssiTestActivity;

/**
 * @author chenxixiong
 *
 */
public class RssiTest extends TestCase {

	public RssiTest(Context context) {
		super(context,
				context.getResources().getStringArray(R.array.test_menu)[11], 
				RssiTestActivity.class);
	}

	/* (non-Javadoc)
	 * @see com.baidu.wearable.test.wristbandtester.testcase.TestCase#onTestStart()
	 */
	@Override
	protected void onTestStart(AsyncTask task) {
		mStatusHandler.post(new Runnable(){

			@Override
			public void run() {
				mService.readRssi();
				mStatusHandler.postDelayed(this, 1000);
			}});
		
	}

}
