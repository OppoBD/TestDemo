/**
 * 
 */
package com.baidu.wearable.test.wristbandtester.testcase;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Message;
import android.util.Log;

import com.baidu.wearable.ble.stack.BlueTooth.BlueToothCommonListener;
import com.baidu.wifi.R;
import com.baidu.wearable.test.wristbandtester.TestActivity.TestStatusHandler;
import com.baidu.wearable.test.wristbandtester.WriteFlagTestActivity;

/**
 * @author chenxixiong
 *
 */
public class WriteFlagTest extends TestCase {

	private final String TAG = WriteFlagTest.class.getSimpleName();
	
	/**
	 * @param context
	 * @param name
	 * @param cls
	 */
	public WriteFlagTest(Context context) {
		super(context,context.getResources().getStringArray(R.array.test_menu)[7], 
				WriteFlagTestActivity.class);
	}

	/* (non-Javadoc)
	 * @see com.baidu.wearable.test.wristbandtester.testcase.TestCase#onTestPrepared()
	 */
	@Override
	protected boolean onTestPrepared() {
		mProtocolHelper.registerFlagRequestHandler(mStatusHandler);
		return true;
	}

	/* (non-Javadoc)
	 * @see com.baidu.wearable.test.wristbandtester.testcase.TestCase#onTestStart()
	 */
	@Override
	protected void onTestStart(AsyncTask task) {
		mProtocolHelper.testWriteFlag(new BlueToothCommonListener(){

			@Override
			public void onSuccess() {
				Log.d(TAG,"send write flag success.");
				startReadFlag();
			}

			@Override
			public void onFailure() {
				Message msg = mStatusHandler.obtainMessage(TestStatusHandler.TEST_STATUS_CHANGED);
				msg.arg1 = WriteFlagTestActivity.EVENT_WRITE_FAIL;
				mStatusHandler.sendMessage(msg);
			}} );
	}
	
	private void startReadFlag(){
		AsyncTask.execute(new Runnable(){

			@Override
			public void run() {
				Message msg = mStatusHandler.obtainMessage(TestStatusHandler.TEST_STATUS_CHANGED);
				msg.arg1 = WriteFlagTestActivity.EVENT_READING;
				mStatusHandler.sendMessage(msg);
				mProtocolHelper.testReadFlag(new BlueToothCommonListener(){

					@Override
					public void onSuccess() {
						Log.d(TAG,"send read flag success.");
						Message msg = mStatusHandler.obtainMessage(TestStatusHandler.TEST_STATUS_CHANGED);
						msg.arg1 = WriteFlagTestActivity.EVENT_VERIFING;
						mStatusHandler.sendMessage(msg);
					}

					@Override
					public void onFailure() {
						Message msg = mStatusHandler.obtainMessage(TestStatusHandler.TEST_STATUS_CHANGED);
						msg.arg1 = WriteFlagTestActivity.EVENT_READ_FAIL;
						mStatusHandler.sendMessage(msg);
					}
				});

				
				
			}
		});
		
	}
}
