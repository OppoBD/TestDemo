package com.baidu.wearable.test.wristbandtester;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.baidu.wifi.R;

public class MainActivity extends Activity {
	private static final String TAG = MainActivity.class.getSimpleName();

	private BluetoothAdapter mBtAdapter;
	private Button mAutoButton;
	private Button mManuButton;
	private Button mStressButton;
	
	private BroadcastReceiver mReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
				int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,
						BluetoothAdapter.STATE_OFF);
				Log.d(TAG, "ACTION_STATE_CHANGED " + state);
				if (state == BluetoothAdapter.STATE_ON) {
					Toast.makeText(context, "Bluetooth Enabled", Toast.LENGTH_SHORT).show();
					mAutoButton.setEnabled(true);
					mManuButton.setEnabled(true);
					mStressButton.setEnabled(true);
				} else if (state == BluetoothAdapter.STATE_OFF) {
					Toast.makeText(context, "Bluetooth Disabled", Toast.LENGTH_SHORT).show();
					mAutoButton.setEnabled(false);
					mManuButton.setEnabled(false);
					mStressButton.setEnabled(false);
				}
			}
		}
	};
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
	
		mAutoButton = (Button) findViewById(R.id.test_auto);
//		mAutoButton.setVisibility(View.INVISIBLE);//设置auto菜单不可见，如果去掉此行，auto便可见
		mManuButton = (Button) findViewById(R.id.test_manual);
		mStressButton = (Button) findViewById(R.id.test_stress);
		
		IntentFilter filter = new IntentFilter();
		filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
		registerReceiver(mReceiver, filter);
		
		mBtAdapter = BluetoothAdapter.getDefaultAdapter();
		if (mBtAdapter == null) {
			finish();
			return;
		}
	}
	
	@Override
	public void onStart() {
		super.onStart();
		if (!mBtAdapter.isEnabled()) {
			mAutoButton.setEnabled(false);
		    mManuButton.setEnabled(false);
		    mStressButton.setEnabled(false);
			mBtAdapter.enable();
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.settings, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		
		if (id == R.id.settings) {
			Intent intent = new Intent(this, Settings.class);
			startActivity(intent);
			return true;
		}

		return false;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		unregisterReceiver(mReceiver);
	}

	public void onClick(View view) {
		if (view.getId() == R.id.test_auto) {
			startAutoTest();
		}else if(view.getId() == R.id.test_manual){
			startManualTest();
		}else if(view.getId() == R.id.test_stress){
			startStressTest();
		}
	}
	
	protected void startAutoTest(){
		Intent intent = new Intent(this, AutoTestActivity.class);
		startActivity(intent);
	}
	
	protected void startManualTest() {
		Intent intent = new Intent(this, ManualTestActivity.class);
		startActivity(intent);
	}
	
	protected void startStressTest() {
		Intent intent = new Intent(this, StressTestActivity.class);
		startActivity(intent);
	}
}
