/**
 * 
 */
package com.baidu.wearable.test.wristbandtester.testcase;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Message;

import com.baidu.wearable.ble.stack.BlueTooth.BlueToothCommonListener;
import com.baidu.wifi.R;
import com.baidu.wearable.test.wristbandtester.TestActivity;
import com.baidu.wearable.test.wristbandtester.TestActivity.TestStatusHandler;
import com.baidu.wearable.test.wristbandtester.TraceabilityTestActivity;

/**
 * @author chenxixiong
 *
 */
public class TraceabilityTest extends TestCase {

	/**
	 * @param context
	 * @param name
	 * @param cls
	 */
	public TraceabilityTest(Context context) {
		super(context, context.getResources().getStringArray(R.array.test_menu)[8], 
				TraceabilityTestActivity.class);
	}

	/* (non-Javadoc)
	 * @see com.baidu.wearable.test.wristbandtester.testcase.TestCase#onTestPrepared()
	 */
	@Override
	protected boolean onTestPrepared() {
		mProtocolHelper.registerFlagRequestHandler(mStatusHandler);
		mProtocolHelper.registerSnRequestHandler(mStatusHandler);
		return true;
	}

	/* (non-Javadoc)
	 * @see com.baidu.wearable.test.wristbandtester.testcase.TestCase#onTestStart()
	 */
	@Override
	protected void onTestStart(AsyncTask task) {
		startReadSn();

	}
	
	private void startReadSn(){
		Message msg = mStatusHandler.obtainMessage(TestActivity.TestStatusHandler.TEST_STATUS_CHANGED);
		msg.arg1 = TraceabilityTestActivity.EVENT_READ_SN_START;
		mStatusHandler.sendMessage(msg);
		mProtocolHelper.testReadSn(new BlueToothCommonListener(){

			@Override
			public void onSuccess() {

			}

			@Override
			public void onFailure() {
				Message msg = mStatusHandler.obtainMessage(TestActivity.TestStatusHandler.TEST_STATUS_CHANGED);
				msg.arg1 = TraceabilityTestActivity.EVENT_READ_SN_FAIL;
				mStatusHandler.sendMessage(msg);
			}});
	}
	
	public void startReadFlag(){
		
		AsyncTask.execute(new Runnable(){

			@Override
			public void run() {
				Message msg = mStatusHandler.obtainMessage(TestStatusHandler.TEST_STATUS_CHANGED);
				msg.arg1 = TraceabilityTestActivity.EVENT_READ_FLAG_START;
				mStatusHandler.sendMessage(msg);
				mProtocolHelper.testReadFlag(new BlueToothCommonListener(){

					@Override
					public void onSuccess() {}

					@Override
					public void onFailure() {
						Message msg = mStatusHandler.obtainMessage(TestStatusHandler.TEST_STATUS_CHANGED);
						msg.arg1 = TraceabilityTestActivity.EVENT_READ_FLAG_FAIL;
						mStatusHandler.sendMessage(msg);
					}
				});
			}
		});
	
	}
}
