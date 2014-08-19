/**
 * 
 */
package com.baidu.wearable.test.wristbandtester;

import android.os.Bundle;
import android.os.Message;
import android.widget.TextView;

import com.baidu.wifi.R;

/**
 * @author chenxixiong
 *
 */
public class RssiTestActivity extends TestActivity {

	private TextView mRssiStatus;
	
	/* (non-Javadoc)
	 * @see com.baidu.wearable.test.wristbandtester.TestActivity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.activity_rssi);
		super.onCreate(savedInstanceState);
		mRssiStatus = (TextView)findViewById(R.id.rssi);
		
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
				switch(msg.what){
				case TEST_SUCCESS:
					mRssiStatus.setText("RSSI: "+msg.arg1);
					mPassButton.setEnabled(true);
					break;
				}
			}};
	}

}
