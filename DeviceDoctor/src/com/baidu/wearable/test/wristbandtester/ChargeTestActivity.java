/**
 * 
 */
package com.baidu.wearable.test.wristbandtester;

import android.os.Bundle;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.baidu.wearable.test.wristbandtester.testcase.ChargeTest;
import com.baidu.wifi.R;

/**
 * @author chenxixiong
 *
 */
public class ChargeTestActivity extends TestActivity {

	private TextView mChargeState;
	private Button mChargeStart;
	
	/* (non-Javadoc)
	 * @see com.baidu.wearable.test.wristbandtester.TestActivity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.activity_charge);
		super.onCreate(savedInstanceState);
		mChargeState = (TextView)findViewById(R.id.charge_state);
		mChargeStart = (Button)findViewById(R.id.charge_start);
		
	}
	
	/* (non-Javadoc)
	 * @see com.baidu.wearable.test.wristbandtester.TestActivity#onClick(android.view.View)
	 */
	@Override
	public void onClick(View v) {
		if(v.getId() == R.id.charge_start){
			mChargeState.setText(R.string.charge_requesting);
			mChargeStart.setEnabled(false);
			startTest();
		}else{
			super.onClick(v);
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
				case TEST_FAIL:
					mChargeState.setText(R.string.charge_request_fail);
					mFailButton.setEnabled(true);
					break;
				case TEST_SUCCESS:
					mChargeState.setText(getString(R.string.charge_detail,((double)msg.arg1)/1000) + "\nVoltage OK\n\nRemove Charger!!!" );
					((ChargeTest)mTestCase).mVoltage = msg.arg1;
					mPassButton.setEnabled(true);
					break;
				}
			}};
	}

}
