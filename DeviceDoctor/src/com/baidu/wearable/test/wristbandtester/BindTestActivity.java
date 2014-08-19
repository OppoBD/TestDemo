/**
 * 
 */
package com.baidu.wearable.test.wristbandtester;

import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.baidu.wifi.R;

/**
 * @author fanjingde 2014-03-27
 *
 */
public class BindTestActivity extends TestActivity {

	public static final int DEVICE_BIND_SUCCESS = 0;
	public static final int DEVICE_BIND_FAIL = 1;
	private TextView mBindState;
	private final String TAG = BindTestActivity.class.getSimpleName();
	
	/* (non-Javadoc)
	 * @see com.baidu.wearable.test.wristbandtester.TestActivity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.activity_bind);
		super.onCreate(savedInstanceState);
		mBindState = (TextView)findViewById(R.id.bind_state);
		mFailButton.setEnabled(true);
		mBindState.setText(R.string.bt_binding);
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onStart()
	 */
	@Override
	protected void onStart() {
		Log.d(TAG, "start BindTestActivity.onStart()");
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
				Log.d(TAG, "start BindTestActivity.TestStatusHandler()");
				super.handleMessage(msg);
				Log.d(TAG, "msg.what=" + msg.what);
				switch(msg.what){
				case TEST_STATUS_CHANGED:
				{
					switch(msg.arg1){
					case DEVICE_BIND_SUCCESS:
						mPassButton.setEnabled(true);
						mFailButton.setEnabled(false);
						mRestartButton.setVisibility(View.INVISIBLE);
						mBindState.setText(R.string.bt_bind_success);
						break;
					case DEVICE_BIND_FAIL:
						break;
					}
					break;
				}
				case TEST_CONNECTION_FAIL:
					mBindState.setText(R.string.bt_bind_fail);
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
