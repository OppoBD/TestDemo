/**
 * 
 */
package com.baidu.wearable.test.wristbandtester.testcase;

import android.content.Context;
import android.os.AsyncTask;

import com.baidu.wifi.R;
import com.baidu.wearable.test.wristbandtester.RomVersionTestActivity;
import com.baidu.wearable.test.wristbandtester.TestActivity;

/**
 * @author chenxixiong
 *
 */
public class RomVersionTest extends TestCase {

	public static final String KEY_ROM_VERSION = "ROM_VERSION";
	public String mVersion = null;
	
	public RomVersionTest(Context context) {
		super(context, context.getResources().getStringArray(R.array.test_menu)[4], RomVersionTestActivity.class);
	}

	/* (non-Javadoc)
	 * @see com.baidu.wearable.test.wristbandtester.testcase.TestCase#onTestStart()
	 */
	@Override
	protected void onTestStart(AsyncTask task) {
		boolean ret = mService.readRomVersion();
		if(!ret){
			mStatusHandler.sendEmptyMessage(TestActivity.TestStatusHandler.TEST_FAIL);
		}
	}

}
