/**
 * 
 */
package com.baidu.wearable.test.wristbandtester;

import android.os.Bundle;
import android.os.Message;
import android.view.View;
import android.widget.TextView;

import com.baidu.wearable.ble.model.Clock;
import com.baidu.wearable.ble.model.ClockList;
import com.baidu.wifi.R;

/**
 * @author fanjingde 2014-04-02
 *
 */
public class AlarmReadTestActivity extends TestActivity {

	public static final int ALARM_READ_SUCCESS = 0;
	public static final int EVENT_CMD_SEND_FAIL = 1;
	private TextView mAlarmReadState;
	private final String TAG = AlarmReadTestActivity.class.getSimpleName();
	public	ClockList clockList;
	public	Clock clock;
	int year = 0;
	int month = 0;
	int day = 0;
	int hour = 0;
	int minute = 0;

	/* (non-Javadoc)
	 * @see com.baidu.wearable.test.wristbandtester.TestActivity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.activity_alarmread);
		super.onCreate(savedInstanceState);
		mAlarmReadState = (TextView)findViewById(R.id.alarmread_state);
		mFailButton.setEnabled(true);
		mAlarmReadState.setText(R.string.bt_readalarm);
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
					case ALARM_READ_SUCCESS:
						clockList = (ClockList)msg.obj;
						for(int i=0; i < clockList.getListSize(); i++){
							clock = clockList.getClock(i);
							mAlarmReadState.setText("闹钟:" + clock.getYear() + "年" + clock.getMonth() + "月" + clock.getDay() 
													+ "日" + clock.getHour() + ":" +clock.getMinute());							
						}
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
					mAlarmReadState.setText(R.string.bt_connection_fail);
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
