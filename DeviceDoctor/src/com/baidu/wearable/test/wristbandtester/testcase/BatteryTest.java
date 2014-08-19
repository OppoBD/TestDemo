/**
 * 
 */
package com.baidu.wearable.test.wristbandtester.testcase;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Message;

import com.baidu.wearable.ble.connectmanager.BluetoothState;
import com.baidu.wearable.test.wristbandtester.BindTestActivity;
import com.baidu.wifi.R;
import com.baidu.wearable.test.wristbandtester.BatteryTestActivity;
import com.baidu.wearable.test.wristbandtester.TestActivity;
import com.baidu.wearable.test.wristbandtester.TestActivity.TestStatusHandler;

/**
 * @author fanjingde 2014-6-20
 *
 */
public class BatteryTest extends TestCase {

	public static final String KEY_ROM_BATTERY = "ROM_BATTERY";
	public static int mBattery = 0;
	
	public BatteryTest(Context context) {
		super(context, context.getResources().getStringArray(R.array.test_menu)[32], BatteryTestActivity.class);
	}

	/* (non-Javadoc)
	 * @see com.baidu.wearable.test.wristbandtester.testcase.TestCase#onTestStart()
	 */
	@Override
	protected void onTestStart(AsyncTask task) {
		boolean ret = mService.readRomBattery();		
		if(!ret){
			mStatusHandler.sendEmptyMessage(TestActivity.TestStatusHandler.TEST_FAIL);
		}
//		mBattery = BluetoothState.getInstance().getBatteryLevel();
//		Message msg = mStatusHandler.obtainMessage(TestStatusHandler.TEST_STATUS_CHANGED);
//		msg.arg1 = BatteryTestActivity.DEVICE_BATTERY_SUCCESS;
//		msg.arg2 = mBattery;
//		mStatusHandler.sendMessage(msg);
	}

}
