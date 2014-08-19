/**
 * 
 */
package com.baidu.wearable.test.wristbandtester;

import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.widget.TextView;

import com.baidu.wearable.test.wristbandtester.testcase.BatteryTest;
import com.baidu.wifi.R;

/**
 * @author fanjingde 2014-6-20
 *
 */
public class BatteryTestActivity extends TestActivity {
	
	private TextView mState;
	public static final int DEVICE_BATTERY_SUCCESS = 0;
	private final String TAG = BindTestActivity.class.getSimpleName();
	
	/* (non-Javadoc)
	 * @see com.baidu.wearable.test.wristbandtester.TestActivity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.activity_battery);
		super.onCreate(savedInstanceState);
		mState = (TextView)findViewById(R.id.battery_state); 
		mState.setText(R.string.battery_retriving);
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onStart()
	 */
	@Override
	protected void onStart() {
		super.onStart();
		startTest();
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
				super.handleMessage(msg);
				Log.d(TAG, "msg.what=" + msg.what);
				switch(msg.what){
				case TEST_FAIL:
					mState.setText(R.string.rom_get_failed);
					mFailButton.setEnabled(true);
					break;
				case TEST_SUCCESS:
					Bundle bundle = msg.getData();
					int battery=bundle.getInt(BatteryTest.KEY_ROM_BATTERY);
					mState.setText("Battery:"+battery);
					((BatteryTest)mTestCase).mBattery = battery;
					mPassButton.setEnabled(true);
					break;
				}
			}
			
		};
	}

}

