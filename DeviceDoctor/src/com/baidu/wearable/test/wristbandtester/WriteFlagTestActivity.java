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
public class WriteFlagTestActivity extends TestActivity {

	private TextView mState;

	public final static int EVENT_WRITE_FAIL = 1;
	public final static int EVENT_READING = 2;
	public final static int EVENT_READ_FAIL = 3;
	public final static int EVENT_VERIFING = 4;
	

	/* (non-Javadoc)
	 * @see com.baidu.wearable.test.wristbandtester.TestActivity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.activity_flag);
		super.onCreate(savedInstanceState);
		mState = (TextView)findViewById(R.id.flag_state);
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onStart()
	 */
	@Override
	protected void onStart() {
		super.onStart();
		if(mTestMgr.getTestResult()){
			mState.setText(R.string.flag_writing);
			startTest();
		}else{
			mState.setText(R.string.flag_write_failed);
			mFailButton.setEnabled(true);
		}
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
				case TEST_STATUS_FLAG_RECEIVED:
					if(msg.arg1 == 0){
						mState.setText(R.string.flag_write_ok);
						mPassButton.setEnabled(true);
					}else{
						mState.setText(R.string.flag_checking_failed);
						mFailButton.setEnabled(true);
					}
					break;
				case TEST_STATUS_CHANGED:{
					switch(msg.arg1){
					case EVENT_WRITE_FAIL:
						mState.setText(R.string.flag_write_failed);
						mFailButton.setEnabled(true);
						break;
					case EVENT_READING:
						mState.setText(R.string.flag_reading);
						break;
					case EVENT_READ_FAIL:
						mState.setText(R.string.flag_read_fail);
						mFailButton.setEnabled(true);
						break;
					case EVENT_VERIFING:
						mState.setText(R.string.flag_checking);
						break;
					}
					break;
				}
				
				}
			}
			
		};
	}

}
