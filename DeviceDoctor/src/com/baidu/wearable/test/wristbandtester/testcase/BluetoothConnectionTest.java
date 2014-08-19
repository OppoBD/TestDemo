/**
 * 
 */
package com.baidu.wearable.test.wristbandtester.testcase;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;

import com.baidu.wearable.ble.stack.BlueTooth.BlueToothCommonListener;
import com.baidu.wearable.test.wristbandtester.BluetoothConnectionTestActivity;
import com.baidu.wearable.test.wristbandtester.BluetoothService;
import com.baidu.wifi.R;
import com.baidu.wearable.test.wristbandtester.TestActivity.TestStatusHandler;

/**
 * @author chenxixiong
 *
 */
public class BluetoothConnectionTest extends TestCase{
	
	private final String TAG = BluetoothConnectionTest.class.getSimpleName();
	
	private final int DEFAULT_DELAY = 5;//second
	private final byte[] ECHO = "abcdefg".getBytes();
	private boolean mResetBt = true;
	private int mEnableBtDelay = DEFAULT_DELAY;
	public int mRssi = 0;
	
	protected boolean mBtDiscoveryTestResult = false;
	protected boolean mBtConnectableTestResult = false;
	protected boolean mBtUsageTestResult = false;
	
	
	public BluetoothConnectionTest(Context context) {
		super(context,
			context.getResources().getStringArray(R.array.test_menu)[0],
			BluetoothConnectionTestActivity.class);
		mResetBt = PreferenceManager.getDefaultSharedPreferences(context).getBoolean(
				context.getResources().getString(R.string.key_resetbt), true);
		String delay = PreferenceManager.getDefaultSharedPreferences(context).getString(
				context.getResources().getString(R.string.key_rbt_time), String.valueOf(DEFAULT_DELAY));

		try{
			mEnableBtDelay = Integer.valueOf(delay);
		}catch(NumberFormatException e){
			e.printStackTrace();
		}
		mEnableBtDelay *= 1000;
	}
	
	/* (non-Javadoc)
	 * @see com.baidu.wearable.test.wristbandtester.TestCase#stopTest(android.content.Context)
	 */
	@Override
	public void stopTest(Context context) {
		if(mService != null){
			mService.stopLeScan();
		}
		super.stopTest(context);
	}

	/* (non-Javadoc)
	 * @see com.baidu.wearable.test.wristbandtester.testcase.TestCase#onTestPrepared()
	 */
	@Override
	protected boolean onTestPrepared() {
		mProtocolHelper.registerEchoRequestHandler(mStatusHandler);
		return true;
	}

	public void connectDevice(){
		Log.d(TAG,"connectDevice()");
		mBtDiscoveryTestResult = true;
		mService.connectDevice();
	}

	public void testCommunication() {
		Log.d(TAG,"testCommunication()");
		mBtConnectableTestResult = true;
		mProtocolHelper.enterTestMode(new BlueToothCommonListener(){

			@Override
			public void onSuccess() {
				Message msg = mStatusHandler.obtainMessage(TestStatusHandler.TEST_STATUS_CHANGED);
				msg.arg1 = BluetoothConnectionTestActivity.EVENT_ENTER_TEST_MODE;
				mStatusHandler.sendMessage(msg);
				
				mProtocolHelper.requestEcho(ECHO, new BlueToothCommonListener(){

					@Override
					public void onSuccess() {
						Log.d(TAG,"echo request send! echo = " + ECHO);
						Message msg = mStatusHandler.obtainMessage(TestStatusHandler.TEST_STATUS_CHANGED);
						msg.arg1 = BluetoothConnectionTestActivity.EVENT_ECHO_SEND;
						mStatusHandler.sendMessage(msg);
					}

					@Override
					public void onFailure() {
						Log.d(TAG,"echo request send fail!");
						mStatusHandler.sendEmptyMessage(TestStatusHandler.TEST_FAIL);
						
					}});
				
			}

			@Override
			public void onFailure() {
				mStatusHandler.sendEmptyMessage(TestStatusHandler.TEST_FAIL);
			}});
	}
	
	public void verifyEcho(byte[] data){
		Log.d(TAG,"data = " + BluetoothService.byteToHexString(data) + ",ECHO = " + BluetoothService.byteToHexString(ECHO));
		boolean result = true;
		for(int i = 0; (i < data.length) && (i < ECHO.length); i++){
			if(data[i] != ECHO[i]){
				Log.d(TAG,"data" + "[" +i+ "]" + "=" + data[i] + ",ECHO" + "[" +i+ "]" + "=" + ECHO[i] );
				result = false;
				break;
			}
		}
		
		if(result){
			mBtUsageTestResult = true;
			mStatusHandler.sendEmptyMessage(TestStatusHandler.TEST_SUCCESS);
		}else{
			mStatusHandler.sendEmptyMessage(TestStatusHandler.TEST_FAIL);
		}
			
	}

	@Override
	public void onTestStart(AsyncTask task) {
		Log.d(TAG,"onTestStart() mService = " + mService);
		mBtDiscoveryTestResult = false;
		mBtConnectableTestResult = false;
		mBtUsageTestResult = false;
		
		if(mService != null){
			if(mResetBt && resetBt()){
				startScan();
			}else if(mResetBt){
				Log.d(TAG,"resetBt fail!");
				mStatusHandler.sendEmptyMessage(TestStatusHandler.TEST_FAIL);
			}
		}			
		else
			throw new NullPointerException("mService not found!");
	}
	
	private boolean resetBt(){
		Log.d(TAG,"resetBt()");
		Message msg = mStatusHandler.obtainMessage(TestStatusHandler.TEST_STATUS_CHANGED);
		msg.arg1 = BluetoothConnectionTestActivity.EVENT_BT_RESET;
		mStatusHandler.sendMessage(msg);
		if(mService.disableBt()){
			try {
				Thread.sleep(mEnableBtDelay);
			} catch (InterruptedException e) {
				e.printStackTrace();
				return false;
			}
			return mService.enableBt();
		}
		return false;
	}

	private void startScan(){
		Log.d(TAG,"startScan()");
		Message msg = mStatusHandler.obtainMessage(TestStatusHandler.TEST_STATUS_CHANGED);
		msg.arg1 = BluetoothConnectionTestActivity.EVENT_BT_RESET_DONE;
		mStatusHandler.sendMessage(msg);
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
			return;
		}
		msg = mStatusHandler.obtainMessage(TestStatusHandler.TEST_STATUS_CHANGED);
		msg.arg1 = BluetoothConnectionTestActivity.EVENT_SCAN_START;
		mStatusHandler.sendMessage(msg);
		mService.startBtScan();
		try {
			synchronized(mStatusHandler){
				mStatusHandler.wait();
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		connectDevice();
		try {
			synchronized(mStatusHandler){
				mStatusHandler.wait();
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
//		testCommunication();去掉进入工厂测试模式
	}

}
