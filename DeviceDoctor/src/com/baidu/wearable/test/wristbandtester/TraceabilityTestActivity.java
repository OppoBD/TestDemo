/**
 * 
 */
package com.baidu.wearable.test.wristbandtester;

import android.os.Bundle;
import android.os.Message;
import android.widget.TextView;

import com.baidu.wearable.test.wristbandtester.testcase.TraceabilityTest;
import com.baidu.wifi.R;

/**
 * @author chenxixiong
 *
 */
public class TraceabilityTestActivity extends TestActivity {

	private TextView mSnState;
	private TextView mFlagState;
	private TextView mTraceState;
	
	public final static int EVENT_READ_SN_START = 1;
	public final static int EVENT_READ_FLAG_START = 2;
	public final static int EVENT_READ_SN_FAIL = 3;
	public final static int EVENT_READ_FLAG_FAIL = 4;
	
	
	/* (non-Javadoc)
	 * @see com.baidu.wearable.test.wristbandtester.TestActivity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.activity_traceability);
		super.onCreate(savedInstanceState);
		mSnState = (TextView)findViewById(R.id.sn_state);
		mFlagState = (TextView)findViewById(R.id.flag_state);
		mTraceState = (TextView)findViewById(R.id.trace_state);
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onStart()
	 */
	@Override
	protected void onStart() {
		super.onStart();
		mTraceState.setText(R.string.trace_retrieving);
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
				case TEST_STATUS_CHANGED:{
					switch(msg.arg1){
					case EVENT_READ_SN_START:
						mSnState.setText(R.string.sn_checking);
						break;
					case EVENT_READ_FLAG_START:
						mFlagState.setText(R.string.flag_reading);
						break;
					case EVENT_READ_FLAG_FAIL:
						mFlagState.setText(R.string.flag_read_fail);
						mTraceState.setText(R.string.trace_retrieving_failed);
						mFailButton.setEnabled(true);
						break;
					case EVENT_READ_SN_FAIL:
						mSnState.setText(R.string.sn_read_fail);
						mTraceState.setText(R.string.trace_retrieving_failed);
						mFailButton.setEnabled(true);
						break;
					}
					break;
				}
				case TEST_STATUS_SN_RECEIVED:
					Bundle bundle = msg.getData();
					byte[] data = bundle.getByteArray(KEY_SN);

					mSnState.setText("S/N: " + new String(data));
					((TraceabilityTest)mTestCase).startReadFlag();
					break;
				case TEST_STATUS_FLAG_RECEIVED:
					mFlagState.setText("Flag: " + ((msg.arg1 == 0) ? "true" : "false"));
					mTraceState.setText(R.string.trace_retrieving_success);
					mPassButton.setEnabled(true);
					mFailButton.setEnabled(true);
					break;
				}
			}};
	}

	
}
