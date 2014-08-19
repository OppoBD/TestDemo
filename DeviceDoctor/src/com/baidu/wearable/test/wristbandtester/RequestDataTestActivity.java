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

import com.baidu.wearable.ble.model.BlueToothSportData;
import com.baidu.wearable.ble.model.BlueToothSportDataSection;
import com.baidu.wifi.R;

/**
 * @author fanjingde 2014-04-02
 *
 */
public class RequestDataTestActivity extends TestActivity {

	public static final int DATA_READ_SUCCESS = 0;
	public static final int EVENT_CMD_SEND_FAIL = 1;
	private TextView mQuestDataState;
	private final String TAG = RequestDataTestActivity.class.getSimpleName();
	private List<BlueToothSportData> sportDatas = new ArrayList<BlueToothSportData>();

	/* (non-Javadoc)
	 * @see com.baidu.wearable.test.wristbandtester.TestActivity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.activity_requestdata);
		super.onCreate(savedInstanceState);
		mQuestDataState = (TextView)findViewById(R.id.requestdata_state);
		mFailButton.setEnabled(true);
		mQuestDataState.setText(R.string.bt_requestdata);
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
				int steps = 0;
				switch(msg.what){
				case TEST_STATUS_CHANGED:
				{
					switch(msg.arg1){
					case DATA_READ_SUCCESS:
						/**
						 * 读取msg.obj里面的计步数
						 */
						sportDatas = (List<BlueToothSportData>)msg.obj;
						Log.d(TAG, "sportDatas.size=" + sportDatas.size());
						for(int i=0; i<sportDatas.size(); i++) {
							BlueToothSportData sportData = sportDatas.get(i);
							for(int j=0; j<sportData.sportDatas.size(); j++) {
								BlueToothSportDataSection sportDataSection = sportData.sportDatas.get(j);		
								steps = steps+sportDataSection.step;
							}
						}
						mQuestDataState.setText("手环当前步数为：" + steps);
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
