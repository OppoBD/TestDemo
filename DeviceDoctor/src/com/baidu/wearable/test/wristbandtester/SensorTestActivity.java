/**
 * 
 */
package com.baidu.wearable.test.wristbandtester;

import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.widget.TextView;

import com.baidu.wifi.R;

/**
 * @author chenxixiong
 *
 */
public class SensorTestActivity extends TestActivity {

	private final String TAG = SensorTestActivity.class.getSimpleName();
	
	private TextView mSensorValue;
	private TextView mState;
	private static final int GSENSOR_G = 8192;
	
	/* (non-Javadoc)
	 * @see com.baidu.wearable.test.wristbandtester.TestActivity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.activity_sensor);
		super.onCreate(savedInstanceState);
		mSensorValue = (TextView)findViewById(R.id.sensor_data);
		mState = (TextView)findViewById(R.id.sensor_state);
	}
	
	/* (non-Javadoc)
	 * @see android.app.Activity#onStart()
	 */
	@Override
	protected void onStart() {
		super.onStart();
		mState.setText(R.string.sensor_requesting);
		startTest();
	}
	
	/* (non-Javadoc)
	 * @see com.baidu.wearable.test.wristbandtester.TestActivity#onTestRestart()
	 */
	@Override
	protected void onTestRestart() {
		mState.setText(R.string.sensor_requesting);
		mSensorValue.setText("");
		super.onTestRestart();
	}

	/* (non-Javadoc)
	 * @see com.baidu.wearable.test.wristbandtester.TestActivity#initListener()
	 */
	@Override
	protected TestStatusHandler initListener() {
		return new TestStatusHandler(){

			/* (non-Javadoc)
			 * @see android.os.Handler#handleMessage(android.os.Message)
			 */
			@Override
			public void handleMessage(Message msg) {
				switch(msg.what){
				case TEST_FAIL:
					mState.setText(R.string.sensor_request_fail);
					mFailButton.setEnabled(true);
					break;
				case TEST_STATUS_SENSOR_RECEIVED:
					mState.setText(R.string.sensor_request_ok);
					Bundle bundle = msg.getData();
					double x, y, z;
					x = bundle.getShortArray(TestStatusHandler.KEY_SENSOR)[0];
					y = bundle.getShortArray(TestStatusHandler.KEY_SENSOR)[1];
					z = bundle.getShortArray(TestStatusHandler.KEY_SENSOR)[2];
					Log.d(TAG,"handleMessage() x = " + x + ", y = " + y + ", z = " + z);
					mSensorValue.setText(getResources().getString(R.string.sensor_details, x/GSENSOR_G, y/GSENSOR_G, z/GSENSOR_G));
					mPassButton.setEnabled(true);
					mFailButton.setEnabled(true);
					break;
				case TEST_TIMEOUT:
					mRestartButton.setEnabled(true);
					break;
				}
			}};
	}

}
