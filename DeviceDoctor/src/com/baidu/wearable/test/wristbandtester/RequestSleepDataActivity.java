/**
 * 
 */
package com.baidu.wearable.test.wristbandtester;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.baidu.wearable.ble.model.BlueToothSleepData;
import com.baidu.wearable.ble.model.BlueToothSleepDataSection;
import com.baidu.wifi.R;

/**
 * @author fanjingde 2014-04-08
 *
 */
public class RequestSleepDataActivity extends TestActivity {

	public static final int SLEEP_READ_SUCCESS = 0;
	public static final int EVENT_CMD_SEND_FAIL = 1;
	private TextView mQuestDataState;
	private final String TAG = RequestDataTestActivity.class.getSimpleName();
	private List<BlueToothSleepData> sleepDatas = new ArrayList<BlueToothSleepData>();

	/* (non-Javadoc)
	 * @see com.baidu.wearable.test.wristbandtester.TestActivity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.activity_requestsleep);
		super.onCreate(savedInstanceState);
		mQuestDataState = (TextView)findViewById(R.id.requestslepp_state);
		mFailButton.setEnabled(true);
		mQuestDataState.setText(R.string.bt_requestsleep);
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
				int minutes = 0;
				switch(msg.what){
				case TEST_STATUS_CHANGED:
				{
					switch(msg.arg1){
					case SLEEP_READ_SUCCESS:
						/**
						 * 读取msg.obj里面的计步数
						 */
						sleepDatas = (List<BlueToothSleepData>)msg.obj;
						Log.d(TAG, "SleepDatas.size=" + sleepDatas.size());
						for(int i=0; i<sleepDatas.size(); i++) {
							BlueToothSleepData sleepData = sleepDatas.get(i);
							for(int j=0; j<sleepData.sleepDatas.size(); j++) {
								BlueToothSleepDataSection sleepDataSection = sleepData.sleepDatas.get(j);		
								minutes = minutes + sleepDataSection.minute;
							}
						}
						mQuestDataState.setText("手环当前睡眠数据为：" + minutes);
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
					mQuestDataState.setText(R.string.bt_connection_fail);
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
