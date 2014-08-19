/**
 * 
 */
package com.baidu.wearable.test.wristbandtester;

import java.util.UUID;

import android.annotation.SuppressLint;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;

import com.baidu.wearable.test.wristbandtester.TestActivity.TestStatusHandler;
import com.baidu.wearable.test.wristbandtester.testcase.BatteryTest;
import com.baidu.wearable.test.wristbandtester.testcase.RomVersionTest;
import com.baidu.wifi.R;
/**
 * @author chenxixiong
 *
 */
@SuppressLint("NewApi")
public class BluetoothService extends Service{

	private final String TAG = "BluetoothService";
	
	private BluetoothAdapter mBtAdapter;
	private String mAddress;
	private BluetoothDevice mBtDevice;
	private static BluetoothGatt mBtGatt;
	
	private TestStatusHandler mListener;
	private ProtocolHelper mProtocolHelper;
	
	protected TestBinder mTestBinder = new TestBinder();
	private Handler mHandler = new Handler();
	
	private boolean mBtEnabled = false;
	private boolean mLeScanning = false;
	private static boolean mUartConnected = false;
	
	private SoundPool mBeepPool;
	private int mSoundId;
	
	
	private Runnable mStopScanRunnable = new Runnable(){

		@Override
		public void run() {
			synchronized(mBtAdapter){
				if(mBtAdapter != null){
					mBtAdapter.stopLeScan(mLeScanCallback);
				}
			}
	        mLeScanning = false;
	        if(mListener != null)
	        	mListener.sendEmptyMessage(TestStatusHandler.TEST_TIMEOUT);
		}
		
	};
	
	public class TestBinder extends Binder {
		public BluetoothService getService() {
			return BluetoothService.this;
		}
	}
	
	private final BroadcastReceiver mBtStateReceiver = new BroadcastReceiver(){

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if(BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)){
				int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1);
				Log.d(TAG,"bt state changed! state = " + state);
				switch(state){
				case BluetoothAdapter.STATE_OFF:
					synchronized(mBtAdapter){
						mBtEnabled = false;
						mBtAdapter.notify();
					}
					break;
				case BluetoothAdapter.STATE_ON:
					synchronized(mBtAdapter){
						mBtEnabled = true;
						mProtocolHelper.initStack(context);
						mBtAdapter.notify();
					}
					break;
				}
			}
			
		}};
	
	private BluetoothAdapter.LeScanCallback mLeScanCallback =
        new BluetoothAdapter.LeScanCallback() {

			@Override
			public void onLeScan(final BluetoothDevice device, final int rssi,
		            byte[] scanRecord) {
				Log.d(TAG, "find device: " + device.getName() + ": " + device.getAddress() + " rssi: " + rssi + " byte:" + (byte)rssi);
				if (isTargetDevice(device, (byte)rssi)) {

					// Found target device. Start connection.
					Log.d(TAG, "It is the target!!");
					playBeep();

					mAddress = device.getAddress();
					mBtDevice = device;
					
					mHandler.removeCallbacks(mStopScanRunnable);
					synchronized(mBtAdapter){
						mBtAdapter.stopLeScan(this);
					}
			    	mLeScanning = false;
			    	
			    	if(mListener != null){
				    	Bundle bundle = new Bundle();
				    	bundle.putInt(BluetoothConnectionTestActivity.KEY_RSSI, rssi);
				    	bundle.putString(BluetoothConnectionTestActivity.KEY_NAME, device.getName());
				    	bundle.putString(BluetoothConnectionTestActivity.KEY_ADDRESS, device.getAddress());//返回设备地址
				    	Message msg = mListener.obtainMessage(TestStatusHandler.TEST_STATUS_CHANGED);
				    	msg.arg1 = BluetoothConnectionTestActivity.EVENT_DEVICE_FOUND;
				    	msg.setData(bundle);
				    	mListener.sendMessage(msg);
			    	}
				}
			}
	};
	
	private final BluetoothGattCallback mGattCallback =
			new BluetoothGattCallback() {

				/* (non-Javadoc)
				 * @see android.bluetooth.BluetoothGattCallback#onCharacteristicChanged(android.bluetooth.BluetoothGatt, android.bluetooth.BluetoothGattCharacteristic)
				 */
				@Override
				public void onCharacteristicChanged(BluetoothGatt gatt,
						BluetoothGattCharacteristic characteristic) {
					super.onCharacteristicChanged(gatt, characteristic);
					Log.d(TAG, "onChChanged");
					if (characteristic != null) {
						byte[] data = characteristic.getValue();
						Log.d(TAG, "ch data: " + byteToHexString(data));
						Log.d(TAG, "ch data: " + new String(data));
						if(mProtocolHelper != null){
							mProtocolHelper.onDataReceived(data);
						}
					}
				}

				/* (non-Javadoc)
				 * @see android.bluetooth.BluetoothGattCallback#onCharacteristicRead(android.bluetooth.BluetoothGatt, android.bluetooth.BluetoothGattCharacteristic, int)
				 */
				@Override
				public void onCharacteristicRead(BluetoothGatt gatt,
						BluetoothGattCharacteristic characteristic, int status) {
					super.onCharacteristicRead(gatt, characteristic, status);
					String str = characteristic.getUuid().toString();
					Log.d(TAG, "onChRead status: " + status);
					if (status == BluetoothGatt.GATT_SUCCESS) {
						if(mListener != null){
							if(str.equalsIgnoreCase(TestConstants.DIS_FWRV_UUID_STR)){
								Log.d(TAG, "str = " + str);
								byte[] value = characteristic.getValue();
								Bundle bundle = new Bundle();
								bundle.putByteArray(RomVersionTest.KEY_ROM_VERSION, value);
								Log.d(TAG, "KEY_ROM_VERSION==" + value);
								Message msg = mListener.obtainMessage(TestStatusHandler.TEST_SUCCESS);
								msg.setData(bundle);
								mListener.sendMessage(msg);
							}else if(str.equalsIgnoreCase(TestConstants.BATTERY_RD_UUID_STR)){
								Log.d(TAG, "str = " + str);
								byte[] value = characteristic.getValue();
								Log.d(TAG, "KEY_ROM_BATTERY==" + value);
								int battery = 0;
							    byte bLoop;
							    for (int i = 0; i < value.length; i++) {
							        bLoop = value[i];
							        battery += (bLoop & 0xFF) << (8 * i);
							    }
							    Log.d(TAG, "KEY_ROM_BATTERY==" + battery);
								Bundle bundle = new Bundle();
								bundle.putInt(BatteryTest.KEY_ROM_BATTERY, battery);
								Message msg = mListener.obtainMessage(TestStatusHandler.TEST_SUCCESS);
								msg.setData(bundle);
								mListener.sendMessage(msg);								
							}
						}
					}else{
						mListener.sendEmptyMessage(TestStatusHandler.TEST_FAIL);
					}
				}

				/* (non-Javadoc)
				 * @see android.bluetooth.BluetoothGattCallback#onCharacteristicWrite(android.bluetooth.BluetoothGatt, android.bluetooth.BluetoothGattCharacteristic, int)
				 */
				@Override
				public void onCharacteristicWrite(BluetoothGatt gatt,
						BluetoothGattCharacteristic characteristic, int status) {
					super.onCharacteristicWrite(gatt, characteristic, status);
					Log.d(TAG, "onChWrite status: " + status);
					if (status == BluetoothGatt.GATT_SUCCESS) {
						if(mProtocolHelper != null){
							mProtocolHelper.sendWriteResult(0);
						}
					} else {
						if(mProtocolHelper != null){
							mProtocolHelper.sendWriteResult(-1);
						}
					}
				}

				/* (non-Javadoc)
				 * @see android.bluetooth.BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)
				 */
				@Override
				public void onConnectionStateChange(BluetoothGatt gatt,
						int status, int newState) {
					super.onConnectionStateChange(gatt, status, newState);
					Log.d(TAG, "onConnectionStateChange newState: " + newState + " status: " + status);
		            if (newState == BluetoothProfile.STATE_CONNECTED) {
		            	boolean ret = gatt.discoverServices();
		                Log.d(TAG, "Connected to GATT server.");
		                Log.d(TAG, "Attempting to start service discovery:" + ret);
		            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
			            Log.d(TAG, "Disconnected from GATT server.");
			            mUartConnected = false;
			            mBtGatt = null;
			            if(mListener != null){
			            	mListener.sendEmptyMessage(TestStatusHandler.TEST_CONNECTION_FAIL);
			            }
		            }
				}

				/* (non-Javadoc)
				 * @see android.bluetooth.BluetoothGattCallback#onDescriptorWrite(android.bluetooth.BluetoothGatt, android.bluetooth.BluetoothGattDescriptor, int)
				 */
				@Override
				public void onDescriptorWrite(BluetoothGatt gatt,
						BluetoothGattDescriptor descriptor, int status) {
					Log.d(TAG, "onDescriptorWrite() " + " status: " + status);
					super.onDescriptorWrite(gatt, descriptor, status);
					if (status == BluetoothGatt.GATT_SUCCESS) {
						mUartConnected = true;
						if(mListener != null){
							Message msg = mListener.obtainMessage(TestStatusHandler.TEST_STATUS_CHANGED);
							msg.arg1 = BluetoothConnectionTestActivity.EVENT_DEVICE_CONNECTED;
							mListener.sendMessage(msg);
						}
					}
				}

				/* (non-Javadoc)
				 * @see android.bluetooth.BluetoothGattCallback#onReadRemoteRssi(android.bluetooth.BluetoothGatt, int, int)
				 */
				@Override
				public void onReadRemoteRssi(BluetoothGatt gatt, int rssi,
						int status) {
					super.onReadRemoteRssi(gatt, rssi, status);
					if(mListener != null){
						if(status == BluetoothGatt.GATT_SUCCESS){
							Message msg = mListener.obtainMessage(TestStatusHandler.TEST_SUCCESS, rssi, 0);
							mListener.sendMessage(msg);
						}else{
							mListener.sendEmptyMessage(TestStatusHandler.TEST_FAIL);
						}
					}
				}

				/* (non-Javadoc)
				 * @see android.bluetooth.BluetoothGattCallback#onServicesDiscovered(android.bluetooth.BluetoothGatt, int)
				 */
				@Override
				public void onServicesDiscovered(BluetoothGatt gatt, int status) {
					super.onServicesDiscovered(gatt, status);
					Log.d(TAG, "onServiceDiscovered status: " + status);
		            if (status == BluetoothGatt.GATT_SUCCESS) {
		            	// For debug
		            	for (BluetoothGattService gs : gatt.getServices()) {
		            		Log.d(TAG, "service: " + gs.getUuid().toString());
		            		if (gs.getUuid().toString().equals(TestConstants.UART_UUID_STR)
		            				|| gs.getUuid().toString().equals(TestConstants.DIS_UUID_STR)) {
		            			for (BluetoothGattCharacteristic ch : gs.getCharacteristics()) {
		            				Log.d(TAG, "ch: " + ch.getUuid().toString());
		            				for (BluetoothGattDescriptor desc : ch.getDescriptors()) {
		            					Log.d(TAG, "desc: " + desc.getUuid().toString() + " " + desc.getPermissions());
		            				}
		            			}
									
		            		}
		            	}
		            	
		            	UUID uuid = UUID.fromString(TestConstants.UART_UUID_STR);
		            	BluetoothGattService uartService = gatt.getService(uuid);
		            	if (uartService != null) {
		            		// Register notification.
		            		BluetoothGattCharacteristic readCh = uartService.getCharacteristic(UUID.fromString(TestConstants.UART_READ_UUID_STR));
		            		boolean ret = gatt.setCharacteristicNotification(readCh, true);
		            		Log.d(TAG, "setChNotification read ret " + ret);
		            		
		            		if (ret) {
	            	   			BluetoothGattDescriptor descriptor = readCh.getDescriptor(
		            					UUID.fromString(TestConstants.CLIENT_CHARACTERISTIC_CONFIG));
		            			descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
		            			ret = mBtGatt.writeDescriptor(descriptor);
		            			Log.d(TAG, "write ENABLE_NOTIFICATION_VALUE return " + ret);
		                    }
		            	}
		            	
		            }
				}
				
	};
	
	private boolean isTargetDevice(BluetoothDevice device, int rssi) {

		String targetName = PreferenceManager.getDefaultSharedPreferences(this)
				.getString(getResources().getString(R.string.key_dev_name), null);
		String rssiStr = PreferenceManager.getDefaultSharedPreferences(this)
				.getString(getResources().getString(R.string.key_rssi), "0");
		int tRssi;
		try {
			tRssi = Integer.parseInt(rssiStr);
		} catch (Exception e) {
			tRssi = 0;
		}
		Log.d(TAG, "tRssi: " + tRssi + " rssi: " + rssi);
		
		// Check RSSI first.
		if (tRssi != 0) {
			return tRssi <= rssi;
		}
		
		// Then check target name.
		if (targetName == null) {
			return true;
		}
		
		if (targetName.equals(device.getName())) {
			return true;
		}
		
		return false;
	}
	
	/* (non-Javadoc)
	 * @see android.app.Service#onCreate()
	 */
	@Override
	public void onCreate() {
		super.onCreate();
		final BluetoothManager bluetoothManager =
		        (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
		mBtAdapter = bluetoothManager.getAdapter();
		mBeepPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
		mSoundId = mBeepPool.load(this, R.raw.found_hint , 1);
		IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
		registerReceiver(mBtStateReceiver, filter);
		
		mProtocolHelper = new ProtocolHelper(this){

			@Override
			public int sendData(byte[] contents) {
				if(mBtGatt != null && mUartConnected){
					Log.d(TAG, "sendData mBtGatt: " + (mBtGatt==null?"null":"gatt") );
					Log.d(TAG, "this: " + this);
					
					BluetoothGattService service = mBtGatt.getService(
							UUID.fromString(TestConstants.UART_UUID_STR));
					if (service != null) {
						BluetoothGattCharacteristic ch = service.getCharacteristic(
								UUID.fromString(TestConstants.UART_WRITE_UUID_STR));
						if (ch != null) {
							if (ch.setValue(contents)) {
								Log.d(TAG, "content: " + byteToHexString(contents));
								Log.d(TAG, "content: " + new String(contents));
								if (mBtGatt.writeCharacteristic(ch)) {
									Log.d(TAG, "writeCh return true, waiting for success");
									return 0;
								}
							}
						}
					}
					return -1;
					
				}else{
					Log.d(TAG,"Device disconnected!" + "mBtGatt = " + mBtGatt + ", mUartConnected = " + mUartConnected);
					if(mListener != null)
						mListener.sendEmptyMessage(TestStatusHandler.TEST_CONNECTION_FAIL);
					return -1;
				}
			}
			
		};
	}

	@Override
	public IBinder onBind(Intent arg0) {
		Log.d(TAG,"onBind");
		return mTestBinder;
	}
	
	/* (non-Javadoc)
	 * @see android.app.Service#onUnbind(android.content.Intent)
	 */
	@Override
	public boolean onUnbind(Intent intent) {
		mListener = null;
		return super.onUnbind(intent);
	}

	/* (non-Javadoc)
	 * @see android.app.Service#onDestroy()
	 */
	@Override
	public void onDestroy() {
		Log.d(TAG,"onDestroy()");
		unregisterReceiver(mBtStateReceiver);
		synchronized(mBtAdapter){
			mBtAdapter = null;
		}
		if(mBtGatt != null){
			mBtGatt.disconnect();
		}
		mBtGatt = null;
		mBtDevice = null;
		mBeepPool = null;
		mProtocolHelper.clear();
		mProtocolHelper = null;
		mListener = null;
		super.onDestroy();
	}

	public void startBtScan(){
		Log.d(TAG,"startBtScan()");
		synchronized(mBtAdapter){
			mBtAdapter.startLeScan(mLeScanCallback);	
		}
        mLeScanning = true;
        mHandler.postDelayed(mStopScanRunnable, 30000);
	}
	
	public void stopLeScan(){
		synchronized(mBtAdapter){
			if(mLeScanning && mBtAdapter != null){
				mBtAdapter.stopLeScan(mLeScanCallback);
			}
		}
	}

	private void playBeep(){
		AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		int curVol = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
		int maxVol = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
		float vol = (float) curVol / maxVol;
		mBeepPool.play(mSoundId, vol, vol, 0, 0, 1);
	}
	
	public void registerListener(TestStatusHandler listener){
		mListener = listener;
	}

	public void connectDevice() {
		mBtGatt = mBtDevice.connectGatt(this, false, mGattCallback);
	}
	
	public static String byteToHexString(byte[] data) {
		String hex = "0123456789ABCDEF";
		StringBuilder sb = new StringBuilder();
		for (int i=0; i<data.length; i++) {
			sb.append(hex.charAt((data[i]>>4) & 0xf));
			sb.append(hex.charAt(data[i] & 0xf));
		}
		return sb.toString();
	}

	public ProtocolHelper getProtocolHelper() {
		return mProtocolHelper;
	}

	public boolean isBtConnected() {
		return (mBtGatt != null) && mUartConnected;
	}
	
	public boolean readRomVersion(){
		if (mBtGatt != null) {
			BluetoothGattService gs = mBtGatt.getService(UUID.fromString(TestConstants.DIS_UUID_STR));
			if (gs != null) {
				BluetoothGattCharacteristic ch = gs.getCharacteristic(UUID.fromString(TestConstants.DIS_FWRV_UUID_STR));
				if (ch != null) {
					if (mBtGatt.readCharacteristic(ch)) {
						return true;
					}
				}
			}
		}
		return false;
	}
	
	//获取battery
	public boolean readRomBattery(){		
		if (mBtGatt != null) {
			BluetoothGattService gs = mBtGatt.getService(UUID.fromString(TestConstants.BATTERY_UUID_STR));
			Log.d(TAG,"readRomBattery-2");
			if (gs != null) {
				BluetoothGattCharacteristic ch = gs.getCharacteristic(UUID.fromString(TestConstants.BATTERY_RD_UUID_STR));
				Log.d(TAG,"readRomBattery-3");
				if (ch != null) {
					if (mBtGatt.readCharacteristic(ch)) {
						Log.d(TAG,"readRomBattery-4");
						return true;
					}
				}
			}
		}
		return false;
//		mBluetoothStateMachine = BluetoothLeStateMachine.getInstance(getApplicationContext());
//		if(null == mBluetoothStateMachine) {
//			Log.d(TAG,"readRomBattery-1");
//			return false;
//		}
//		
//		final BluetoothState bs = BluetoothState.getInstance();
////		bs.setBatteryLevel(80);
//		mBluetoothStateMachine
//		.readBatterLevel(new BluetoothLeStateMachine.ReadBatteryLevelCallback() {
//
//			@Override
//			public void onFinish(int status, int value) {
//				Log.d(TAG, "readBatterLevel onFinish statue:"
//						+ status + " value:" + value);
//				bs.setBatteryLevel(value);
//			}
//
//		});
//		
//		Log.d(TAG,"ddddd+++");
//		int power = bs.getBatteryLevel();
//		Log.d(TAG,"power="+power);
//		if(power>0){
//			return true;
//		}else {
//			return false;
//		}		
	}
	
	public void readRssi() {
		if (mBtGatt != null) {
			mBtGatt.readRemoteRssi();
		}
	}
	
	public boolean disableBt(){
		Log.d(TAG,"disableBt()");
		synchronized(mBtAdapter){
			mBtEnabled = mBtAdapter.isEnabled();
			if(!mBtEnabled)
				return true;;
			mBtAdapter.disable();
			mProtocolHelper.finalizeStack();
			int timeout = 0;
			while(mBtEnabled){
				try {
					mBtAdapter.wait(1000);
					timeout++;
					if(timeout > 10)
						break;
				} catch (InterruptedException e) {
					e.printStackTrace();
					return false;
				}
			}
			if(timeout>10){
				Log.d(TAG,"Disable BT fail!");
				return false;
			}else{
				return true;
			}
		}
		
	}
	
	
	public boolean enableBt(){
		Log.d(TAG,"enableBt()");
		synchronized(mBtAdapter){
			mBtEnabled = false;
			mBtAdapter.enable();
			int timeout = 0;
			while(!mBtEnabled){
				try {
					mBtAdapter.wait(1000);
					timeout++;
					if(timeout > 3)
						break;
				} catch (InterruptedException e) {
					e.printStackTrace();
					return false;
				}
			}
			if(timeout>3){
				Log.d(TAG,"Disable BT fail!");
				return false;
			}else{
				return true;
			}
		}
		
	}

	public String getAddr() {
		return mAddress;
	}
}
