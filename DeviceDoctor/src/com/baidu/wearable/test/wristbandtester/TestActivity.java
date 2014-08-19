/**
 * 
 */
package com.baidu.wearable.test.wristbandtester;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.baidu.wearable.test.wristbandtester.testcase.TestCase;
import com.baidu.wifi.R;

/**
 * @author chenxixiong
 *
 */
public abstract class TestActivity extends Activity implements OnClickListener{

	private final String TAG = TestActivity.class.getSimpleName();
	
	protected TestManager mTestMgr;
	protected TestCase mTestCase;
	private int mCaseId;
	protected TestStatusHandler mListener;
		
	/*
	 * Pass and Fail buttons.
	 */
	protected Button mPassButton;
	protected Button mFailButton;
	protected Button mRestartButton;
	
	public class TestStatusHandler extends Handler {
				
		public static final int TEST_CONNECTION_FAIL = -1;
		public static final int TEST_SUCCESS = 0;
		public static final int TEST_FAIL = 1;
		public static final int TEST_TIMEOUT = 2;
		public static final int TEST_STATUS_CHANGED = 3;
		public static final int TEST_STATUS_ECHO_RECEIVED = 4;
		public static final int TEST_STATUS_SN_RECEIVED = 5;
		public static final int TEST_STATUS_SENSOR_RECEIVED = 6;
		public static final int TEST_STATUS_VOLTAGE_RECEIVED = 7;
		public static final int TEST_STATUS_FLAG_RECEIVED = 8;
		public static final int TEST_STATUS_BUTTON_EVENT_RECEIVED = 9;
		
		public static final String KEY_SN = "SN";
		public static final String KEY_ECHO = "ECHO";
		public static final String KEY_SENSOR = "SENSOR";

	}
	
	/* (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.d(TAG,"onCreate()");
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		Intent intent = getIntent();
		if(intent != null)
			mCaseId = intent.getIntExtra(TestCase.TEST_CASE_ID,-1);

		mPassButton = (Button) findViewById(R.id.result_pass);
		mFailButton = (Button) findViewById(R.id.result_fail);
		mRestartButton = (Button) findViewById(R.id.restart);
		
		mTestMgr = TestManager.getInstance(this);
		mTestCase = mTestMgr.getTestCase(mCaseId);
		
		mListener = initListener();
		if(mListener == null)
			throw new NullPointerException();

	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onDestroy()
	 */
	@Override
	protected void onDestroy() {
		Log.d(TAG,"onDestroy()");
		stopTest(this);
		super.onDestroy();
	}

	protected void startTest(){
		mTestCase.startTest(TestActivity.this, mListener);
	}

	protected void stopTest(Context context){
		if(mListener!=null){
			mListener.removeCallbacksAndMessages(null);
			mListener= null;
		}
		if(mTestCase != null){
			mTestCase.stopTest(context);
			mTestCase = null;
		}
	}
	
	protected abstract TestStatusHandler initListener();
	
	protected void onTestRestart(){
		mPassButton.setEnabled(false);
		mFailButton.setEnabled(false);
		mRestartButton.setEnabled(false);
		if(mTestCase != null)
			mTestCase.run();
	}
	
	/* (non-Javadoc)
	 * @see android.view.View.OnClickListener#onClick(android.view.View)
	 */
	@Override
	public void onClick(View v) {
		if(v.getId() == R.id.result_pass){
			if(mTestCase != null)
				mTestCase.mTestResult = true;
		}else if(v.getId() == R.id.result_fail){
			if(mTestCase != null)
				mTestCase.mTestResult = false;
		}else if(v.getId() == R.id.restart){
			onTestRestart();
		}
		if((v.getId() == R.id.result_pass) || (v.getId() == R.id.result_fail)){
			setResult(RESULT_OK);
			mTestCase.mTestDone = true;
			finish();
			stopTest(this);//make sure this stopTest called before BluetoothService destroyed
		}
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onBackPressed()
	 */
	@Override
	public void onBackPressed() {
		setResult(RESULT_CANCELED);
		stopTest(this);//make sure this stopTest called before BluetoothService destroyed
		super.onBackPressed();
	}
	
	
	
	
}
