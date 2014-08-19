package com.baidu.wearable.test.wristbandtester;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

public class StressTestActivity extends ListActivity implements AdapterView.OnItemClickListener {
	private static final String TAG = StressTestActivity.class.getSimpleName();
	
	protected TestManager mTestManager;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mTestManager = initTestManager();
		mTestManager.prepareStressTest();
		String[] tests = mTestManager.getTestCases();
//		for(int i = 0; i <tests.length;i++){			
//			Log.d(TAG, "tests [" + i + "] is " + tests[i]);
//		}
		//getResources().getStringArray(R.array.test_menu);
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(StressTestActivity.this, android.R.layout.simple_list_item_1, tests);
		setListAdapter(adapter);
		getListView().setOnItemClickListener(StressTestActivity.this);
		Intent intent = new Intent();
		intent.setClass(this, BluetoothService.class);
		startService(intent);
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

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
		// TODO Auto-generated method stub
		mTestManager.getTestCase((int)id).startTestActivity();
		
	}
	
	protected TestManager initTestManager(){
		return TestManager.getInstance(this);
	}
	
}
