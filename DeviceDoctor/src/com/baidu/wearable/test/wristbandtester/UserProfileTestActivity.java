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
 * @author fanjingde 2014-03-29
 *
 */
public class UserProfileTestActivity extends TestActivity {

	public static final int PROFILE_SET_SUCCESS = 0;
	public static final int EVENT_CMD_SEND_FAIL = 1;
	private TextView mProfileState;
	public static final String age = "age";
	public static final String height = "height";
	public static final String weight = "weight";
	

	/* (non-Javadoc)
	 * @see com.baidu.wearable.test.wristbandtester.TestActivity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.activity_profile);
		super.onCreate(savedInstanceState);
		mProfileState = (TextView)findViewById(R.id.profile_state);
		mFailButton.setEnabled(true);
		mProfileState.setText(R.string.bt_setprofile);
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
					case PROFILE_SET_SUCCESS:
						mProfileState.setText("Age: " + msg.getData().getInt(age)
												+ "  height: " + msg.getData().getInt(height)
												+ "  weight: " + msg.getData().getInt(weight));
//						mBtState.setText(getResources().getString(R.string.bt_connecting, msg.getData().getString(age)) );
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
					mProfileState.setText(R.string.bt_connection_fail);
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
