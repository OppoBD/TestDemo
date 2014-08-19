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
public class ButtonTestActivity extends TestActivity {
	
	private TextView mInfo;
	private TextView mStatus;
	
	private boolean mShortClickEventReceived = false;
	private boolean mLongClickEventReceived = false;
	
	private final int BUTTON_TEST_SHORT_PRESS = 0x02;
	private final int BUTTON_TEST_LONG_PRESS = 0x03;
		
	/* (non-Javadoc)
	 * @see com.baidu.wearable.test.wristbandtester.TestActivity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.activity_button);
		super.onCreate(savedInstanceState);

		mInfo = (TextView)findViewById(R.id.info);
		mStatus = (TextView)findViewById(R.id.status);
		
		mFailButton.setEnabled(true);
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
				case TEST_STATUS_BUTTON_EVENT_RECEIVED:
				{
					if(msg.arg2 == BUTTON_TEST_SHORT_PRESS){//short press
						if(!mShortClickEventReceived){
							mStatus.append("\r\n"+getString(R.string.button_short_click_event_received, msg.arg1));
							mShortClickEventReceived = true;
							mInfo.setText(R.string.plz_long_click_the_button_for_3_secs);
						}
					}else if(msg.arg2 == BUTTON_TEST_LONG_PRESS){//long press
						if(mShortClickEventReceived && !mLongClickEventReceived){
							mStatus.append("\r\n"+getString(R.string.button_long_click_event_received, msg.arg1));
							mLongClickEventReceived = true;
						}
					}

					if(mShortClickEventReceived && mLongClickEventReceived ){
						mInfo.setText(R.string.button_test_passed);
						mPassButton.setEnabled(true);
					}
					break;
				}
				case TEST_TIMEOUT:
					mFailButton.setEnabled(true);
					break;
				case TEST_CONNECTION_FAIL:
					mInfo.setText(R.string.bt_connection_fail);
					mPassButton.setEnabled(false);
				}
			}};
	}

}
