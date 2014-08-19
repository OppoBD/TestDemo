/**
 * 
 */
package com.baidu.wearable.test.wristbandtester;

import android.os.Bundle;
import android.os.Message;
import android.view.View;
import android.widget.TextView;

import com.baidu.wifi.R;

/**
 * @author fanjingde 2014-03-28
 *
 */
public class LostTestActivity extends TestActivity {

	public static final int LOST_SET_SUCCESS = 0;
	public static final int EVENT_CMD_SEND_FAIL = 1;
	private TextView mLostState;

	/* (non-Javadoc)
	 * @see com.baidu.wearable.test.wristbandtester.TestActivity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.activity_lost);
		super.onCreate(savedInstanceState);
		mLostState = (TextView)findViewById(R.id.lost_state);
		mFailButton.setEnabled(true);
		mLostState.setText(R.string.bt_setlost);
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
				switch(msg.what){
				case TEST_STATUS_CHANGED:
				{
					switch(msg.arg1){
					case LOST_SET_SUCCESS:
						mPassButton.setEnabled(true);
						mFailButton.setEnabled(false);
						mRestartButton.setVisibility(View.INVISIBLE);
						break;
					case EVENT_CMD_SEND_FAIL:
						break;
					}
					break;
				}
				case TEST_CONNECTION_FAIL:
					mLostState.setText(R.string.bt_connection_fail);
					mFailButton.setEnabled(true);
					mRestartButton.setVisibility(View.INVISIBLE);
					break;
				case TEST_TIMEOUT:
					mRestartButton.setEnabled(true);
					break;
				}
			}
			
		};
	}

}
