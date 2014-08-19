/**
 * 
 */
package com.baidu.wearable.test.wristbandtester;

import java.util.ArrayList;

import android.content.Context;
import android.os.SystemClock;

import com.baidu.wearable.test.wristbandtester.testcase.AlarmReadTest;
import com.baidu.wearable.test.wristbandtester.testcase.AlarmSetTest;
import com.baidu.wearable.test.wristbandtester.testcase.AlarmStressTest;
import com.baidu.wearable.test.wristbandtester.testcase.BatteryTest;
import com.baidu.wearable.test.wristbandtester.testcase.BindTest;
import com.baidu.wearable.test.wristbandtester.testcase.BluetoothConnectionTest;
import com.baidu.wearable.test.wristbandtester.testcase.ControlTest;
import com.baidu.wearable.test.wristbandtester.testcase.DailySportDataTest;
import com.baidu.wearable.test.wristbandtester.testcase.LRHandTest;
import com.baidu.wearable.test.wristbandtester.testcase.LostTest;
//import com.baidu.wearable.test.wristbandtester.testcase.OtaTest;
import com.baidu.wearable.test.wristbandtester.testcase.PhoneAnswerTest;
import com.baidu.wearable.test.wristbandtester.testcase.PhoneCommingStress;
import com.baidu.wearable.test.wristbandtester.testcase.PhoneCommingTest;
//import com.baidu.wearable.test.wristbandtester.testcase.PhoneDenyTest;
import com.baidu.wearable.test.wristbandtester.testcase.RequestDataTest;
import com.baidu.wearable.test.wristbandtester.testcase.RomVersionTest;
import com.baidu.wearable.test.wristbandtester.testcase.RssiTest;
//import com.baidu.wearable.test.wristbandtester.testcase.SnTest;
import com.baidu.wearable.test.wristbandtester.testcase.SportTargetTest;
import com.baidu.wearable.test.wristbandtester.testcase.StillAlarmTest;
import com.baidu.wearable.test.wristbandtester.testcase.TestCase;
import com.baidu.wearable.test.wristbandtester.testcase.TimeTest;
import com.baidu.wearable.test.wristbandtester.testcase.UserProfileTest;
import com.baidu.wifi.R;


/**
 * @author chenxixiong
 *
 */
public class TestManager{
	
	protected final ArrayList<TestCase> mTestCases = new ArrayList<TestCase>();
	protected Context mContext; 
	private long mStartTime;
	private long mStopTime;
	
	protected static TestManager mInstance;
	protected TestManager(Context context){
		mContext = context;
	}
	
	public static TestManager getInstance(Context context){
		if(mInstance == null)
			mInstance = new TestManager(context);
		return mInstance;
	}
	
	public static void finalizeSelf(){
		if(mInstance != null){
			mInstance.mContext = null;
			mInstance = null;
		}
	}
	
	/**
	 * 
	 * @return All test cases' name
	 */
	public String[] getTestCases(){
		synchronized(mTestCases){
			String[] names = new String[mTestCases.size()];
			for(int i = 0; i < mTestCases.size(); i++){
				names[i] = mTestCases.get(i).mName;
			}

			return names;
		}
	}
	
	public TestCase getTestCase(int id){
		synchronized(mTestCases){
			if(mTestCases.size() > id)
				return mTestCases.get(id);
			else
				return null;
		}
	}

	public void prepareManualTest() {
		synchronized(mTestCases){
			clear();
			generateTestCases(false);
		}
		
	}

	public void prepareAutoTest() {
		synchronized(mTestCases){
			clear();
			generateTestCases(true);
		}
		
	}
		
	public void clear() {
		mStartTime = 0;
		mStopTime = 0;
		synchronized(mTestCases){
			mTestCases.clear();
			TestCase.mCaseCount = 0;
		}
	}

	public boolean getTestResult() {
		boolean result = true;
		for(TestCase cs:mTestCases){
			if(cs.mTestDone){
				result &= cs.mTestResult;
				if(!result)
					break;
			}
		}
		return result;
	}

	public int getCasesSize() {
		return mTestCases.size();
	}

	public void notifyAutoTestStart(){
		mStartTime = SystemClock.uptimeMillis(); 
	}

	public void notifyAutoTestStop(){
		mStopTime = SystemClock.uptimeMillis(); 
	}
	
	public long getAutoTestStartTime(){
		return mStartTime;
	}

	public long getAutoTestStopTime(){
		return mStopTime;
	}
	
	protected void generateTestCases(boolean auto){
		int vendor = mContext.getResources().getInteger(R.integer.config_vendor);
		switch(vendor){
		case TestConstants.VENDOR_CODE_T1000:
			if(auto){
				mTestCases.add(new BluetoothConnectionTest(mContext));
//				mTestCases.add(new LedTest(mContext));
//				mTestCases.add(new VibratorTest(mContext));
//				mTestCases.add(new SnTest(mContext));
//				mTestCases.add(new ChargeTest(mContext));
//				mTestCases.add(new RomVersionTest(mContext));
//				mTestCases.add(new SensorTest(mContext));
//				mTestCases.add(new WriteFlagTest(mContext));
//				mTestCases.add(new MormancyTest(mContext));
				mTestCases.add(new BindTest(mContext));//添加bind
				mTestCases.add(new TimeTest(mContext));//添加Time
				mTestCases.add(new AlarmSetTest(mContext));//添加闹钟	
				mTestCases.add(new LostTest(mContext));//添加lost
				mTestCases.add(new UserProfileTest(mContext));//添加profile
				mTestCases.add(new SportTargetTest(mContext));//添加SportTarget
//				mTestCases.add(new DailySportDataTest(mContext));//添加DailySportData
//				mTestCases.add(new RequestDataTest(mContext));//添加RequestData
//				mTestCases.add(new LRHandTest(mContext));//添加Left or right hand
				mTestCases.add(new PhoneCommingTest(mContext));//添加phonecoming
				mTestCases.add(new PhoneAnswerTest(mContext));//添加phoneanswer
//				mTestCases.add(new PhoneDenyTest(mContext));//添加phonedeny
//				mTestCases.add(new AlarmReadTest(mContext));//添加AlarmRead
//				mTestCases.add(new StillAlarmTest(mContext));//添加stillalarm
//				mTestCases.add(new RequestSleepData(mContext));//添加RequestSleepData
			}else{
				mTestCases.add(new BluetoothConnectionTest(mContext));
//				mTestCases.add(new LedTest(mContext));
//				mTestCases.add(new VibratorTest(mContext));
//				mTestCases.add(new SnTest(mContext));
				mTestCases.add(new RomVersionTest(mContext));
				mTestCases.add(new RssiTest(mContext));
//				mTestCases.add(new ChargeTest(mContext));
//				mTestCases.add(new SensorTest(mContext));
//				mTestCases.add(new TraceabilityTest(mContext));
//				mTestCases.add(new WriteFlagTest(mContext));
//				mTestCases.add(new MormancyTest(mContext));
//				mTestCases.add(new OtaTest(mContext));
				mTestCases.add(new BindTest(mContext));//添加bind
				mTestCases.add(new TimeTest(mContext));//添加Time
				mTestCases.add(new AlarmSetTest(mContext));//添加闹钟	
				mTestCases.add(new LostTest(mContext));//添加lost
				mTestCases.add(new UserProfileTest(mContext));//添加profile
				mTestCases.add(new SportTargetTest(mContext));//添加SportTarget
				mTestCases.add(new DailySportDataTest(mContext));//添加DailySportData
				mTestCases.add(new RequestDataTest(mContext));//添加RequestData
//				mTestCases.add(new LRHandTest(mContext));//添加Left or right hand
				mTestCases.add(new PhoneCommingTest(mContext));//添加phonecoming
				mTestCases.add(new PhoneAnswerTest(mContext));//添加phoneanswer
//				mTestCases.add(new PhoneDenyTest(mContext));//添加phonedeny
				mTestCases.add(new AlarmReadTest(mContext));//添加AlarmRead
				mTestCases.add(new StillAlarmTest(mContext));//添加stillalarm
//				mTestCases.add(new RequestSleepData(mContext));//添加RequestSleepData
				mTestCases.add(new ControlTest(mContext));//添加ControlCamera
				mTestCases.add(new BatteryTest(mContext));//添加BatteryTest
			}
			break;
		}
	}
	
	/**
	 * 准备stress测试的case
	 */
	public void prepareStressTest() {
		synchronized(mTestCases){
			clear();
			generateStressTestCases();
		}
		
	}
	protected void generateStressTestCases(){
		int vendor = mContext.getResources().getInteger(R.integer.config_vendor);
		switch(vendor){
		case TestConstants.VENDOR_CODE_T1000:
			{
				mTestCases.add(new BluetoothConnectionTest(mContext));
				mTestCases.add(new BindTest(mContext));//添加bind
				mTestCases.add(new TimeTest(mContext));//添加Time
				mTestCases.add(new PhoneCommingStress(mContext));//添加phonecomingStress
				mTestCases.add(new AlarmStressTest(mContext));//添加AlarmStress
			}
			break;
		}
	}
}
