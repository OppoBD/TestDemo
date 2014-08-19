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
 * @author fanjingde 2014-04-18
 *
 */
public class ControlTestActivity extends TestActivity {

	public static final int CAMERA_CONTROL_SUCCESS = 0;
	public static final int EVENT_CMD_SEND_FAIL = 1;
	private TextView mControlCameraState;
	private final String TAG = ControlTestActivity.class.getSimpleName();
	

	/* (non-Javadoc)
	 * @see com.baidu.wearable.test.wristbandtester.TestActivity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.activity_controlcamera);
		super.onCreate(savedInstanceState);
		mControlCameraState = (TextView)findViewById(R.id.camera_state);
		mFailButton.setEnabled(true);
		mControlCameraState.setText(R.string.bt_controlcamera);
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
					case CAMERA_CONTROL_SUCCESS:
						mControlCameraState.setText("Contor camera successful");
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
					mControlCameraState.setText(R.string.bt_connection_fail);
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
