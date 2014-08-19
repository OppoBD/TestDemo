/**
 * 
 */
package com.baidu.wearable.test.wristbandtester;

import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.baidu.wearable.test.wristbandtester.testcase.BluetoothConnectionTest;
import com.baidu.wifi.R;

/**
 * @author chenxixiong
 *
 */
public class BluetoothConnectionTestActivity extends TestActivity {

	private final String TAG = BluetoothConnectionTestActivity.class.getSimpleName();
	
	public final static int EVENT_DEVICE_FOUND = 1;
	public final static int EVENT_SCAN_START = 2;
	public final static int EVENT_DEVICE_CONNECTED = 3;
	public final static int EVENT_ENTER_TEST_MODE = 4;
	public final static int EVENT_ECHO_SEND = 5;
	public final static int EVENT_ECHO_RECEIVED = 6;
	public final static int EVENT_BT_RESET = 7;
	public final static int EVENT_BT_RESET_DONE = 8;
	
	public static final String KEY_RSSI = "RSSI";
	public static final String KEY_NAME = "NAME";
	public static final String KEY_ADDRESS = "ADDRESS";
	
	private TextView mRssi;
	private TextView mBtState;
	private TextView mBtAddress;
	

	@Override
	public void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.activity_bluetooth);
		mRssi = (TextView)findViewById(R.id.rssi);
		mBtState = (TextView)findViewById(R.id.bt_state);
		mBtAddress = (TextView)findViewById(R.id.address);
		
		super.onCreate(savedInstanceState);
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onStart()
	 */
	@Override
	protected void onStart() {
		Log.d(TAG,"BluetoothConnectionTestActivity.onStart()");
		super.onStart();
		startTest();
	}
	
	@Override
	protected TestStatusHandler initListener() {
		return new TestStatusHandler() {
			
			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				Log.d(TAG, "msg.what=" + msg.what);
				switch(msg.what){
				case TEST_SUCCESS:
					mPassButton.setEnabled(true);
					mFailButton.setEnabled(false);
					mBtState.setText(getString(R.string.test_pass));
					break;
				case TEST_FAIL:
					mPassButton.setEnabled(false);
					mFailButton.setEnabled(true);
					mBtState.setText(getString(R.string.test_fail));
					break;
				case TEST_TIMEOUT:
					mBtState.setText(R.string.bt_nodevice);
					mFailButton.setEnabled(true);
					mRestartButton.setVisibility(View.VISIBLE);
					mRestartButton.setEnabled(true);
					break;
				case TEST_STATUS_ECHO_RECEIVED:
					Bundle bundle = msg.getData();
					byte[] data = bundle.getByteArray(KEY_ECHO);
					((BluetoothConnectionTest)mTestCase).verifyEcho(data);
					mBtState.setText(getString(R.string.bt_received));
					break;
				case TEST_STATUS_CHANGED:
					switch(msg.arg1){
					case EVENT_DEVICE_FOUND:
						mRssi.setText("RSSI: " + msg.getData().getInt(KEY_RSSI) + "dBm");
						mBtState.setText(getResources().getString(R.string.bt_connecting, msg.getData().getString(KEY_NAME)) );
						mBtAddress.setText("ADDRESS: " + msg.getData().getString(KEY_ADDRESS));
						((BluetoothConnectionTest)mTestCase).mRssi = msg.getData().getInt(KEY_RSSI) ;
						synchronized(this){
							this.notifyAll();
						}
						break;
					case EVENT_SCAN_START:
						mBtState.setText(getString(R.string.bt_searching));
						break;
					case EVENT_DEVICE_CONNECTED:
//						mBtState.setText(getString(R.string.bt_connected) + getString(R.string.bt_requesting));
						mBtState.setText(getString(R.string.bt_connected));
						synchronized(this){
							this.notifyAll();
						}
						mPassButton.setEnabled(true);
						mFailButton.setEnabled(false);
						mBtState.setText(getString(R.string.test_pass));
						break;
					case EVENT_ENTER_TEST_MODE:
						break;
					case EVENT_ECHO_SEND:
						mBtState.setText(getString(R.string.bt_receiving));
						break;
					case EVENT_BT_RESET:
						mBtState.setText(getString(R.string.bt_resetting));
						break;
					case EVENT_BT_RESET_DONE:
						mBtState.setText(getString(R.string.bt_prepare));
						break;
					}
					break;
				}
			}
		};
	}

	@Override
	protected void onTestRestart() {
		//mRestartButton.setVisibility(View.INVISIBLE);
		mBtState.setText(R.string.bt_prepare);
		super.onTestRestart();
	}
}
