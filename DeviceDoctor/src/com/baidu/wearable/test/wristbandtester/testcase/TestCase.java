package com.baidu.wearable.test.wristbandtester.testcase;

import java.io.PrintWriter;
import java.text.DecimalFormat;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.IBinder;
import android.util.Log;

import com.baidu.wearable.test.wristbandtester.BluetoothService;
import com.baidu.wearable.test.wristbandtester.ProtocolHelper;
import com.baidu.wearable.test.wristbandtester.TestActivity.TestStatusHandler;

public abstract class TestCase implements ServiceConnection{

	private final String TAG = TestCase.class.getSimpleName();
	
	protected Context mContext;
	public int mCaseId;
	public String mName;
	public boolean mTestResult = false;
	public boolean mTestDone = false;
	public static int mCaseCount = 0;
	public static final String TEST_CASE_ID = "ID";
	private boolean mReady = false;

	protected BluetoothService mService;
	protected TestStatusHandler mStatusHandler;
	protected Class mActivityClass;
	protected ProtocolHelper mProtocolHelper;
	
	protected AsyncTask mTask;
	
	class TestTask extends AsyncTask{
		
		@Override
		protected Object doInBackground(Object... params) {
			Log.d(TAG,"doInBackground");
			while(!mReady){
				try {
					Thread.sleep(100);
					if(isCancelled()){
						return null;
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
					return null;
				}
			}
			if(mCaseId != 0 && !mService.isBtConnected()){
				mStatusHandler.sendEmptyMessage(TestStatusHandler.TEST_CONNECTION_FAIL);
			}
			onTestStart(this);
			return null;
		}
	}
	
	public TestCase(Context context, String name, Class cls){
		mContext = context;
		mCaseId = mCaseCount++;
		mName = name;
		mActivityClass = cls;
	}
	
	public final void startTest(Context context, TestStatusHandler handler){
		Log.d(TAG,"TestCase.startTest()");
		mReady = false;
		mStatusHandler = handler;	
		Intent intent = new Intent();
		intent.setClass(context, BluetoothService.class);
		context.bindService(intent, this, 0);
		run();
	}
	
	public void stopTest(Context context){
		Log.d(TAG,"stopTest()");
		if(mTask != null){
			mTask.cancel(true);
		}
		if(mService != null){
			Log.d(TAG,"unbindService()");
			context.unbindService(this);
			mProtocolHelper = null;
		}
	}
	
	@Override
	public final void onServiceConnected(ComponentName arg0, IBinder arg1) {
		Log.d(TAG,"onServiceConnected()");
		mService = ((BluetoothService.TestBinder)arg1).getService();
		mService.registerListener(mStatusHandler);
		mProtocolHelper = mService.getProtocolHelper();
		mReady = onTestPrepared();
	}

	@Override
	public final void onServiceDisconnected(ComponentName arg0) {
		mService = null;
		mReady = false;
	}

	public final void startTestActivity(){
		Intent intent = new Intent(mContext, mActivityClass);
		intent.putExtra(TestCase.TEST_CASE_ID, mCaseId);
		intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
		mContext.startActivity(intent);
	}
	
	public final void startTestActivityForResult(Activity activity){
		Intent intent = new Intent(mContext, mActivityClass);
		intent.putExtra(TestCase.TEST_CASE_ID, mCaseId);
		intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
		activity.startActivityForResult(intent, mCaseId);
	}

	public final void run(){
		mTestResult = false;
		mTestDone = false;

		//mTask.cancel(true);
		if(mTask != null){
			mTask.cancel(true);
		}
		mTask = new TestTask();
		mTask.execute();
	}
	
	protected boolean onTestPrepared(){
		return true;
	}
	
	/**
	 * this method will be called in a AsyncTask
	 * @param task TODO
	 */
	protected abstract void onTestStart(AsyncTask task);

	public String getResultForShow() {
		return mTestResult ? "PASS" : "FAIL";
	}

	public int printReport(PrintWriter pw, int index, DecimalFormat format){
		return index;
	};
	
}
