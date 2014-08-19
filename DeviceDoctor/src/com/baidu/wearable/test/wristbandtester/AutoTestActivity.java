/**
 * 
 */
package com.baidu.wearable.test.wristbandtester;

import java.io.FileNotFoundException;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.BasicMeta.BasicNetworkUpload;
import com.baidu.wearable.test.wristbandtester.testcase.TestCase;
import com.baidu.wifi.R;
import com.baidu.wifi.demo.MainActivity;
import com.baidu.wifi.demo.Utils;
import com.baidu.wifi.demo.WifiActivity;

/**
 * @author chenxixiong
 *
 */
public class AutoTestActivity extends Activity implements OnClickListener{
	private static final String TAG = AutoTestActivity.class.getSimpleName();
	
	protected TestManager mTestManager;
	private int mTestingCaseId;
	public int passCount = 0;
		
//	private TextView mTestDetail;
//	private TextView mStatus;
	private TextView mScore;
	private TextView mScorePercent;
	private Button mRestartYes;
	private Button mRestartNo;
	
	private TextView mBluetooth;
	private TextView mBluetoothResult;	
	private TextView mBind;
	private TextView mBindResult;
	private TextView mTimeSet;
	private TextView mTimeSetResult;
	private TextView mAlarmSet;
	private TextView mAlarmSetResult;
	private TextView mLost;
	private TextView mLostResult;
	private TextView mProfile;
	private TextView mProfileResult;
	private TextView mTarget;
	private TextView mTargetResult;
	private TextView mPhoneComming;
	private TextView mPhoneCommingResult;
	private TextView mPhoneAnswer;
	private TextView mPhoneAnswerResult;
	
	//手环检测结果上传
	public  String NOW_CONNECTED_STATUS = "now_connect_status";	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//HUAWEI D2-0082 TCL S960T		
		if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.JELLY_BEAN_MR1
				&& !android.os.Build.MODEL.equalsIgnoreCase("TCL S960T")) {
			Log.d(TAG, "SDK_INT==" + Build.VERSION.SDK_INT);
			Toast toast=Toast.makeText(getApplicationContext(), "该设备不支持蓝牙BLE功能", Toast.LENGTH_SHORT);   
			//显示toast信息   
			toast.show();
		}else {
			setContentView(R.layout.activity_result);		
			mScore = (TextView)findViewById(R.id.text_shouhua_score);
			mScorePercent = (TextView)findViewById(R.id.text_shouhua_percent);
			
			mBluetooth = (TextView)findViewById(R.id.text_ble_connect);
			mBluetoothResult = (TextView)findViewById(R.id.check_connect);
			
			mBind = (TextView)findViewById(R.id.text_bind_result);
			mBindResult = (TextView)findViewById(R.id.check_bind);
				
			mTimeSet = (TextView)findViewById(R.id.text_timeset_result);
			mTimeSetResult = (TextView)findViewById(R.id.check_timeset);
		
			mAlarmSet = (TextView)findViewById(R.id.text_alarmset_result);
			mAlarmSetResult = (TextView)findViewById(R.id.check_alarmset);
				
			mLost = (TextView)findViewById(R.id.text_lostset_result);
			mLostResult = (TextView)findViewById(R.id.check_lostset);
				
			mProfile = (TextView)findViewById(R.id.text_profileset_result);
			mProfileResult = (TextView)findViewById(R.id.check_profileset);
				
			mTarget = (TextView)findViewById(R.id.text_targetset_result);
			mTargetResult = (TextView)findViewById(R.id.check_targetset);
				
			mPhoneComming = (TextView)findViewById(R.id.text_phonecoming_result);
			mPhoneCommingResult = (TextView)findViewById(R.id.check_phonecoming);
				
			mPhoneAnswer = (TextView)findViewById(R.id.text_phoneanswer_result);
			mPhoneAnswerResult = (TextView)findViewById(R.id.check_phoneanswer);
				
			mRestartYes = (Button)findViewById(R.id.restart_yes);
			mRestartNo = (Button)findViewById(R.id.restart_no);
				
			mRestartYes.setOnClickListener(this);
			mRestartNo.setOnClickListener(this);
					
			Intent intent = new Intent();
			intent.setClass(this, BluetoothService.class);
			startService(intent);
			
			mTestingCaseId = -1;
			mTestManager = initTestManager();
			mTestManager.prepareAutoTest();
	    }
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onActivityResult(int, int, android.content.Intent)
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		setResultView();
		if(resultCode == RESULT_OK){
			startNextTest();
		}else if(resultCode == RESULT_CANCELED){
			mRestartYes.setEnabled(true);
			mRestartNo.setEnabled(true);			
		}
		if(passCount > 9) passCount = 9; 
		mScore.setText(R.string.shouhuan_score);
		mScorePercent.setText(passCount + "/9");							
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onResume()
	 */
	@Override
	protected void onResume() {
		super.onResume();
		if(mTestingCaseId == -1){
//			mTestDetail.setText("");
			mTestManager.notifyAutoTestStart();
			startNextTest();
		}
	}

	@Override
	public void onDestroy() {
		Log.d(TAG,"onDestroy()");
		super.onDestroy();
		if (mTestManager != null) {
			mTestManager.clear();
		}
		Intent intent = new Intent();
		intent.setClass(this, BluetoothService.class);
		stopService(intent);
	}
	
	protected TestManager initTestManager(){
		return TestManager.getInstance(this);
	}
	
	protected TestReport createTestReport(){
		return new TestReport(){

			@Override
			public void printReport(TestManager testMgr)
					throws FileNotFoundException {
				// TODO Auto-generated method stub
				
			}};
	}
	
	private void startNextTest(){
		if(++mTestingCaseId < TestCase.mCaseCount){
			mTestManager.getTestCase(mTestingCaseId).startTestActivityForResult(this);
		}else{	
			mTestManager.notifyAutoTestStop();
//			mStatus.setText(R.string.report_printing);
			printReport();
		}
	}
	
	private void printReport(){
		AsyncTask.execute(new Runnable(){

			@Override
			public void run() {
				try {
					TestReport report = createTestReport();
					report.printReport(mTestManager);
				} catch (FileNotFoundException e) {
//					mStatus.setText(R.string.report_print_fail);
					e.printStackTrace();
				}
				runOnUiThread(new Runnable(){

					@Override
					public void run() {
						Intent intent = new Intent();
						intent.setClass(getApplicationContext(), BluetoothService.class);
						stopService(intent);
//						mStatus.setText(R.string.restart_question);
						mRestartYes.setEnabled(true);
						mRestartNo.setEnabled(true);
					}});
			}});
	}
	
	private void setResultView(){
		TestCase test = mTestManager.getTestCase(mTestingCaseId);
		String result = test.getResultForShow();
		Context context = getApplicationContext();
//		mTestDetail.append(test.mName + ":" + test.getResultForShow() + "\n");
		if(test.mName.equalsIgnoreCase("Bluetooth")){
			mBluetoothResult.setText(result + "\n");
			if(result.equalsIgnoreCase("PASS")) {
				mBluetoothResult.setTextColor(0xff00ff00);
				passCount++;
			}
			Utils.saveString(context, NOW_CONNECTED_STATUS, result);
		}else if(test.mName.equalsIgnoreCase("Bind")){
			mBindResult.setText(result + "\n");
			if(result.equalsIgnoreCase("PASS")) {
				mBindResult.setTextColor(0xff00ff00);
				passCount++;
			}
			if(result.equalsIgnoreCase("PASS")) passCount++;
		}else if(test.mName.equalsIgnoreCase("Time")){
			mTimeSetResult.setText(result + "\n");
			if(result.equalsIgnoreCase("PASS")) {
				mTimeSetResult.setTextColor(0xff00ff00);
				passCount++;
			}
			if(result.equalsIgnoreCase("PASS")) passCount++;
		}else if(test.mName.equalsIgnoreCase("AlarmSet")){
			mAlarmSetResult.setText(result + "\n");
			if(result.equalsIgnoreCase("PASS")) {
				mAlarmSetResult.setTextColor(0xff00ff00);
				passCount++;
			}
		}else if(test.mName.equalsIgnoreCase("Lost")){
			mLostResult.setText(result + "\n");
			if(result.equalsIgnoreCase("PASS")) {
				mLostResult.setTextColor(0xff00ff00);
				passCount++;
			}
		}else if(test.mName.equalsIgnoreCase("UserProfile")){
			mProfileResult.setText(result + "\n");
			if(result.equalsIgnoreCase("PASS")) {
				mProfileResult.setTextColor(0xff00ff00);
				passCount++;
			}
		}else if(test.mName.equalsIgnoreCase("SportTarget")){
			mTargetResult.setText(result + "\n");
			if(result.equalsIgnoreCase("PASS")) {
				mTargetResult.setTextColor(0xff00ff00);
				passCount++;
			}
		}else if(test.mName.equalsIgnoreCase("PhoneComming")){
			mPhoneCommingResult.setText(result + "\n");
			if(result.equalsIgnoreCase("PASS")) {
				mPhoneCommingResult.setTextColor(0xff00ff00);
				passCount++;
			}
		}else if(test.mName.equalsIgnoreCase("PhoneAnswer")){
			mPhoneAnswerResult.setText(result + "\n");
			if(result.equalsIgnoreCase("PASS")) {
				mPhoneAnswerResult.setTextColor(0xff00ff00);
				passCount++;
			}
		}		
	}

	@Override
	public void onClick(View v) {
		if(v.getId() == R.id.restart_yes){
			Intent intent = new Intent();
			intent.setClass(this, BluetoothService.class);
			startService(intent);
			
			mRestartYes.setEnabled(false);
			mRestartNo.setEnabled(false);
			mTestingCaseId = -1;
			mTestManager.prepareAutoTest();
			passCount = 0;
//			mTestDetail.setText("");
			startNextTest();
		}else if(v.getId() == R.id.restart_no){
//			finish();
			shouhuan_finish();
		}
		
	}
	
	private void shouhuan_finish() {
		// Transport.upload_network(getApplicationContext());
		new BasicNetworkUpload().upload_shouhuan(getApplicationContext());
		Intent intent = new Intent(AutoTestActivity.this, MainActivity.class);
		startActivity(intent);

	}
	
}
