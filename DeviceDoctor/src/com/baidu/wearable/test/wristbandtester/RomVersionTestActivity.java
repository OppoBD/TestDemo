/**
 * 
 */
package com.baidu.wearable.test.wristbandtester;

import android.os.Bundle;
import android.os.Message;
import android.widget.TextView;

import com.baidu.wearable.test.wristbandtester.testcase.RomVersionTest;
import com.baidu.wifi.R;

/**
 * @author chenxixiong
 *
 */
public class RomVersionTestActivity extends TestActivity {

	private TextView mState;
	
	/* (non-Javadoc)
	 * @see com.baidu.wearable.test.wristbandtester.TestActivity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.activity_rom);
		super.onCreate(savedInstanceState);
		mState = (TextView)findViewById(R.id.rom_state); 
		mState.setText(R.string.rom_retriving);
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
				case TEST_FAIL:
					mState.setText(R.string.rom_get_failed);
					mFailButton.setEnabled(true);
					break;
				case TEST_SUCCESS:
					Bundle bundle = msg.getData();
					byte[] version = bundle.getByteArray(RomVersionTest.KEY_ROM_VERSION);
					mState.setText(new String(version));
					((RomVersionTest)mTestCase).mVersion = new String(version);
					mPassButton.setEnabled(true);
					break;
				}
			}
			
		};
	}

}

