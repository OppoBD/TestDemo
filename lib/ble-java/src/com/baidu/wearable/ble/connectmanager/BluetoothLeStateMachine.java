package com.baidu.wearable.ble.connectmanager;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
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
import android.os.Bundle;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.support.v4.content.LocalBroadcastManager;

import com.baidu.wearable.ble.stack.HealthStackL0JNITransprot;
import com.baidu.wearable.ble.stack.IBlueToothSend;
import com.baidu.wearable.ble.util.LogUtil;

 
@SuppressLint("NewApi")
public class BluetoothLeStateMachine extends StateMachine implements IBlueToothSend {
	    
	private static final String UART_DEVICE_NAME = "Boom Band";   
	 
	private static final String TAG = BluetoothLeStateMachine.class.getSimpleName();
	
	public static final String ACTION_BLE_SM_CONNECT_STATE = "action.wearable.ble.statemachine.connect.state";
	
	public static final String EXTRA_BLE_SM_CONNECT_STATE = "extra.wearable.ble.statemachine.connect.state";
	public static final String EXTRA_BLE_SM_CONNECT_STATE_DEVICE_ADDRESS = "extra.wearable.ble.statemachine.connect.state.device.address";
	public static final String EXTRA_BLE_SM_CONNECT_STATE_DEVICE_NAME = "extra.wearable.ble.statemachine.connect.state.device.name";
	
	public static final int BLE_SM_CONNECT_STATE_INVALID = -1; //invalid state
	public static final int BLE_SM_CONNECT_STATE_DISCONNECTED = 0; //
	public static final int BLE_SM_CONNECT_STATE_WAIT_RETRY = BLE_SM_CONNECT_STATE_DISCONNECTED + 1;
	public static final int BLE_SM_CONNECT_STATE_SCANNING = BLE_SM_CONNECT_STATE_WAIT_RETRY + 1;
	public static final int BLE_SM_CONNECT_STATE_CONNECTING = BLE_SM_CONNECT_STATE_SCANNING + 1; //in connect device process
	public static final int BLE_SM_CONNECT_STATE_CONNECTED = BLE_SM_CONNECT_STATE_CONNECTING + 1; //success connect the device, in bluetooth level, not stack level
	
	
	public static final String ACTION_BLE_SM_CONNECT_COMMAND = "action.wearable.ble.statemachine.connect.command";
	public static final String EXTRA_BLE_SM_CONNECT_COMMAND = "extra.wearable.ble.statemachine.connect.command";
	public static final String EXTRA_BLE_SM_CONNECT_COMMAND_DEVICE_ADDRESS = "extra.wearable.ble.statemachine.connect.command.device.address";
	public static final String EXTRA_BLE_SM_CONNECT_COMMAND_RETRY_COUNT = "extra.wearable.ble.statemachine.connect.command.retry.count";

	
	public static final int BLE_SM_CONNECT_COMMAND_INVALID = -1; //this is invalid value
	public static final int BLE_SM_CONNECT_COMMAND_START_SCAN = 0; //start new scan of device
	public static final int BLE_SM_CONNECT_COMMAND_FINALIZE = BLE_SM_CONNECT_COMMAND_START_SCAN + 1; //finalize the state machine, this command will reset the state machine into idle state
	public static final int BLE_SM_CONNECT_COMMAND_START_CONNECT = BLE_SM_CONNECT_COMMAND_FINALIZE + 1; //start new connect the specific device, which address is EXTRA_BLE_SM_CONNECT_COMMAND_DEVICE_ADDRESS
	
	public static final int BLE_SM_CONNECT_COMMAND_RETRY_COUNT_INVALID = -2; 
	public static final int BLE_SM_CONNECT_COMMAND_RETRY_COUNT_INFINITE = -1; 
	
	
	public static final String ACTION_BLE_SM_ALARM = "action.wearable.ble.statemachine.alarm";
	
	private final int ALARM_REPEAT_INTERVAL = 5*60*1000;
	
	private final int READ_BATTERY_LEVEL_TIMEOUT = 4*1000;
	private final int READ_DEVICE_INFORMATION_TIMEOUT = 4*1000;
	
	private final int SEND_UART_TIMEOUT = 4*1000;
	
	private static final int MSG_FINALIZE = 0;
	private static final int MSG_STATE_TIMEOUT =  MSG_FINALIZE + 1;
	private static final int MSG_READ_BATTERY_LEVEL_TIMEOUNT =  MSG_STATE_TIMEOUT + 1;
	private static final int MSG_READ_DEVICE_INFORMATION_TIMEOUNT =  MSG_READ_BATTERY_LEVEL_TIMEOUNT + 1;
	private static final int MSG_SEND_UART_TIMEOUNT =  MSG_READ_DEVICE_INFORMATION_TIMEOUNT + 1;
	private static final int MSG_RECEIVE_INTENT = MSG_SEND_UART_TIMEOUNT + 1;
	private static final int MSG_DEVICE_FIND = MSG_RECEIVE_INTENT + 1;
	private static final int MSG_DEVICE_CONNECTED = MSG_DEVICE_FIND + 1;
	private static final int MSG_DEVICE_DISCONNECTED = MSG_DEVICE_CONNECTED + 1;
	private static final int MSG_SERVICE_DISCOVERED_SUCCESS = MSG_DEVICE_DISCONNECTED + 1;
	private static final int MSG_SERVICE_DISCOVERED_ERROR = MSG_SERVICE_DISCOVERED_SUCCESS + 1;
	private static final int MSG_UART_RO_SET_NOTIFY_SUCCESS = MSG_SERVICE_DISCOVERED_ERROR + 1;
	private static final int MSG_UART_RO_SET_NOTIFY_ERROR = MSG_UART_RO_SET_NOTIFY_SUCCESS + 1;
	
	
	private static final String KEY_DEVIC_FIND_DEVICE = "key_device_find_device";
	private static final String KEY_DEVIC_FIND_RSSI = "key_device_find_rssi";
	
	// The UART UUIDs.
	private static final String UART_UUID_STR = "6e400001-b5a3-f393-e0a9-e50e24dcca9e";
	private static final String UART_WR_UUID_STR = "6e400002-b5a3-f393-e0a9-e50e24dcca9e";
	private static final String UART_NO_UUID_STR = "6e400003-b5a3-f393-e0a9-e50e24dcca9e";
	private static final String CLIENT_CHARACTERISTIC_CONFIG = "00002902-0000-1000-8000-00805f9b34fb";
	 
	// The Battery UUIDs.
	private static final String BATTERY_UUID_STR = "0000180f-0000-1000-8000-00805f9b34fb";
	private static final String BATTERY_RD_UUID_STR = "00002a19-0000-1000-8000-00805f9b34fb";
	
	// The Device Information UUIDs.
	private static final String DEVICE_INFORMATION_UUID_STR = "0000180a-0000-1000-8000-00805f9b34fb";
	private static final String MANUFACTURER_NAME_UUID_STR = "00002a29-0000-1000-8000-00805f9b34fb";
	private static final String MODEL_NUMBER_UUID_STR = "00002a24-0000-1000-8000-00805f9b34fb";
	private static final String HARDWARE_REVISION_UUID_STR = "00002a27-0000-1000-8000-00805f9b34fb";
	private static final String SOFTWARE_REVISION_UUID_STR = "00002a26-0000-1000-8000-00805f9b34fb";
		 
	/**
	 * The single instance.
	 */
	private static BluetoothLeStateMachine sInstance;
	
	/**
	 * Name of the single instance.
	 */
	private static final String NAME = "BLE StateMachine";
	
	/**
	 * Component context using this state machine.
	 */
	private Context mContext;
	
	private BluetoothAdapter mBtAdapter;
	
	/**
	 * The target UART device.
	 */
	
	private Receiver mReceiver;
	
	/**
	 * The bluetooth gatt object.
	 */
	
	private HealthStackL0JNITransprot mHealthStackL0JNITransprot;
	
	/**
	 * Battery state.
	 */
	
	private ReadBatteryLevelCallback mReadBatteryLevelCallback = null;
	
	
	/**
	 * device information
	 */
	private ReadDeviceInformationCallback mReadManufactoryNameCallback = null;  
	private ReadDeviceInformationCallback mReadModelNumberCallback = null; 
	private ReadDeviceInformationCallback mReadHWRevisionCallback = null; 
	private ReadDeviceInformationCallback mReadSWRevisionCallback = null; 
	
	
	private boolean mUartBeenSending = false;
	
	
	private boolean mHaveBeenFinalize = false;
	
	
	/**
	 * States of the machine.
	 */
	private IdleState mIdleState;
	private ScanState mScanState;
	private BindState mBindState;
	private ConnectState mConnectState;
	private ConfigState mConfigState;
	private OKState mOKState;
	
	private int mScanRetryCount = BLE_SM_CONNECT_COMMAND_RETRY_COUNT_INVALID;
	private int mConnectRetryCount = BLE_SM_CONNECT_COMMAND_RETRY_COUNT_INVALID;
	private int mConnectRetryTime = 2000;
	private String mConnectDeviceAddress = null;
	
	private boolean mNeedRefreshGatt = false;
	
	
	private final int NEED_RESET_BLE_ERROR_COUNT = 10;
	
	private int mErrorCount = 0;
	
	private boolean mIsInOKState = false;
	
	private PowerManager.WakeLock mWakeLock;
	
	private LocalBroadcastManager mLocalBroadcastManager;
	
	class Receiver extends BroadcastReceiver {

		Context mContext;

		public Receiver(Context context) {
			mContext = context;
			mLocalBroadcastManager = LocalBroadcastManager.getInstance(mContext);
		}

		public void registerAction(String action) {
			IntentFilter filter = new IntentFilter();
			filter.addAction(action);
			mLocalBroadcastManager.registerReceiver(this, filter);
		}
 
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			LogUtil.d(TAG, "onReceive action:" + action);
			if (action.equals(BluetoothLeStateMachine.ACTION_BLE_SM_CONNECT_COMMAND)) {
				
				mScanRetryCount = BLE_SM_CONNECT_COMMAND_RETRY_COUNT_INVALID;
				mConnectRetryCount = BLE_SM_CONNECT_COMMAND_RETRY_COUNT_INVALID;
				mConnectRetryTime = 2000; 
				mConnectDeviceAddress = null;
				mNeedRefreshGatt = false; 
				
				releaseWakeLock();
				
				sendMessage(MSG_RECEIVE_INTENT,intent);
			} else if(action.equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)) {
				LogUtil.d(TAG, "bond state:" + intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, -1));
				sendMessage(MSG_RECEIVE_INTENT,intent);
			} else if(action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)){
				LogUtil.d(TAG, " state:" + intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1));
				sendMessage(MSG_RECEIVE_INTENT,intent);
				assert(false);
			} else if (action.equals(ACTION_BLE_SM_ALARM)){
				if(mConnectRetryCount > 0 || mConnectRetryCount == BLE_SM_CONNECT_COMMAND_RETRY_COUNT_INFINITE) {
					if(!mIsInOKState) {
						LogUtil.d(TAG, "current state is not ok state, take wakelock");
						acquireWakeLock();
					}
					
				}
			} else {
				LogUtil.e(TAG, "not support action:" + action);
				assert(false);		
			}
		}
	}
	 
	
	private BluetoothDevice getBluetoothDevice(String address) {
		BluetoothDevice bd = null;
		try {
			bd = mBtAdapter.getRemoteDevice(address);
		} catch (IllegalArgumentException  e) {
			// TODO: handle exception
			LogUtil.e(TAG, "address is invalid ");
		}
		return bd;
	}
	
	private boolean refreshGatt(BluetoothGatt gatt) {
		Method refreshMethod;
		Boolean returnValue = false;
		
		LogUtil.d(TAG, "refreshGatt");
		try {
			refreshMethod = BluetoothGatt.class.getMethod("refresh");
			returnValue = (Boolean) refreshMethod.invoke(gatt);
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		LogUtil.d(TAG, "refresh ret:" + returnValue);
		
		return returnValue;
	}
	
	private boolean removeBond(BluetoothDevice device) {
		Method removeBondMethod;
		Boolean returnValue = false;
		
		LogUtil.d(TAG, "removeBond");
		try {
			removeBondMethod = BluetoothDevice.class.getMethod("removeBond");
			returnValue = (Boolean) removeBondMethod.invoke(device);
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		LogUtil.d(TAG, "removeBond ret:" + returnValue);
		
		return returnValue;
	}
	
	private void acquireWakeLock() {
		LogUtil.d(TAG, "acquireWakeLock");
		if(null == mWakeLock) {
		    PowerManager pm = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
		    int wakeFlags;
		    wakeFlags = PowerManager.PARTIAL_WAKE_LOCK;
		    mWakeLock = pm.newWakeLock(wakeFlags, TAG);
		    mWakeLock.acquire();
		}
	}
	
	private void releaseWakeLock() {
		LogUtil.d(TAG, "releaseWakeLock");
		if(null != mWakeLock) {
			mWakeLock.release();
		    mWakeLock = null;
		}
	}
	
	private void startAlarm(){
		LogUtil.d(TAG, "startAlarm");
		Intent alarmIntent = new Intent(ACTION_BLE_SM_ALARM);
		PendingIntent sender = PendingIntent.getBroadcast(mContext,
				0, alarmIntent, 0);
		AlarmManager am = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
		am.cancel(sender);
		am.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + ALARM_REPEAT_INTERVAL,
				ALARM_REPEAT_INTERVAL, sender);
	}
	
	private BluetoothGattCallback mBtGattCallback = new BluetoothGattCallback() {
		@Override
		public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
			LogUtil.d(TAG, "onConnectionStateChanged status: " + status + " newState: " + newState);
			
			if(mHaveBeenFinalize) {
				LogUtil.d(TAG, "have been finalize ");
				return;
			}
			if (newState == BluetoothProfile.STATE_CONNECTED) {
				sendMessage(MSG_DEVICE_CONNECTED,gatt);
			} else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
				sendMessage(MSG_DEVICE_DISCONNECTED);
				
				if(null != mReadBatteryLevelCallback) {
					ReadBatteryLevelCallback callback = mReadBatteryLevelCallback;
					mReadBatteryLevelCallback = null;
					callback.onFinish(-1,BluetoothState.BATTERY_LEVEL_UNKNOWN);
				}
				
				handleReadDeviceInformationError();
				
			}
		}
		
		@Override
		public void onServicesDiscovered(BluetoothGatt gatt, int status) {
			LogUtil.d(TAG, "onServiceDiscovered status: " + status);
			
			if(mHaveBeenFinalize) {
				LogUtil.d(TAG, "have been finalize ");
				return;
			}
			
			if (status == BluetoothGatt.GATT_SUCCESS) {
				sendMessage(MSG_SERVICE_DISCOVERED_SUCCESS);
			} else {
				sendMessage(MSG_SERVICE_DISCOVERED_ERROR);
			}
		}
		
		@Override
		public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
			LogUtil.d(TAG, "onDescriptorWrite descriptor:" + descriptor.getUuid().toString() + " in charatoristic:" + descriptor.getCharacteristic().getUuid().toString());
			
			if(mHaveBeenFinalize) {
				LogUtil.d(TAG, "have been finalize ");
				return;
			}
			
			String uuidStr = descriptor.getCharacteristic().getUuid().toString();
			if (UART_NO_UUID_STR.equals(uuidStr)) {
				LogUtil.d(TAG, "write uart ro charactor notify status:" + status);
				if (status == BluetoothGatt.GATT_SUCCESS) {
					sendMessage(MSG_UART_RO_SET_NOTIFY_SUCCESS);
				} else {
					sendMessage(MSG_UART_RO_SET_NOTIFY_ERROR);
				}
			} else {
				LogUtil.e(TAG, "should not write to this charactoristic: " + uuidStr);
				assert(false);
			}
		}
		
		@Override
		public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic ch, int status) {
			LogUtil.d(TAG, " onCharacteristicWrite status: " + status);
			
			if(mHaveBeenFinalize) {
				LogUtil.d(TAG, "have been finalize ");
				return;
			}
			
			String uuidStr = ch.getUuid().toString();
			if (UART_WR_UUID_STR.equals(uuidStr)) {
				mUartBeenSending = false;
				removeMessages(MSG_SEND_UART_TIMEOUNT);
				if (status == BluetoothGatt.GATT_SUCCESS) {
					mHealthStackL0JNITransprot.sendWriteResult(0);
				} else {
					mHealthStackL0JNITransprot.sendWriteResult(-1);
				}
			} else {
				LogUtil.e(TAG, " onCharacteristicWrite not support charactoristic: " + uuidStr);
				assert(false);
			}
		}
		
		@Override
		public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic ch) {
			byte[] data = ch.getValue();
			int length = data==null ? 0 : data.length;
			LogUtil.d(TAG," onCharacteristicChanged " + convertToHexString(data));
			
			if(mHaveBeenFinalize) {
				LogUtil.d(TAG, "have been finalize ");
				return;
			}
			String uuidStr = ch.getUuid().toString();
			if (UART_NO_UUID_STR.equals(uuidStr)) {
				mHealthStackL0JNITransprot.sendReadResult(data, (char)length);
			} else  {
				LogUtil.e(TAG, " onCharacteristicChanged not support charactoristic: " + uuidStr);
				assert(false);
			}
		}
		
		@Override
		public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic ch, int status) {
			LogUtil.d(TAG, " onCharacteristicRead status: " + status);
			
			if(mHaveBeenFinalize) {
				LogUtil.d(TAG, "have been finalize ");
				return;
			}

			if (ch.getService().getUuid().toString().equals(BATTERY_UUID_STR)) {
				if (status == BluetoothGatt.GATT_SUCCESS) {
					if (BATTERY_RD_UUID_STR.equals(ch.getUuid().toString())) {

						byte[] data = ch.getValue();
						if (data != null && data.length > 0) {
							LogUtil.d(TAG, "new power: " + data[0]);
							if(null != mReadBatteryLevelCallback) {
								ReadBatteryLevelCallback callback = mReadBatteryLevelCallback;
								mReadBatteryLevelCallback = null;
								callback.onFinish(0,data[0]);
							}
						}

					} else {
						LogUtil.e(TAG, " not support characteristic : "
								+ ch.getUuid().toString());
						assert (false);
					}
				} else {
					if(null != mReadBatteryLevelCallback) {
						ReadBatteryLevelCallback callback = mReadBatteryLevelCallback;
						mReadBatteryLevelCallback = null;
						callback.onFinish(-1,BluetoothState.BATTERY_LEVEL_UNKNOWN);
					}
				}
				removeMessages(MSG_READ_BATTERY_LEVEL_TIMEOUNT);
			} else if (ch.getService().getUuid().toString()
					.equals(DEVICE_INFORMATION_UUID_STR)) {
				LogUtil.d(TAG, " charactoristic  : " + ch.getUuid().toString());

				if (status == BluetoothGatt.GATT_SUCCESS) {
					if(null != mReadManufactoryNameCallback && ch.getUuid().toString().equals(MANUFACTURER_NAME_UUID_STR)) {
						ReadDeviceInformationCallback callback = mReadManufactoryNameCallback;
						mReadManufactoryNameCallback = null;
						
						LogUtil.d(TAG, "read device information: " + new String(ch.getValue()));
						
						callback.onFinish(0,
								new String(ch.getValue())); 
					} else if(null != mReadModelNumberCallback && ch.getUuid().toString().equals(MODEL_NUMBER_UUID_STR)) {
						ReadDeviceInformationCallback callback = mReadModelNumberCallback;
						mReadModelNumberCallback = null;
						
						LogUtil.d(TAG, "read device information: " + new String(ch.getValue()));
						
						callback.onFinish(0,
								new String(ch.getValue())); 
					} else if(null != mReadHWRevisionCallback && ch.getUuid().toString().equals(HARDWARE_REVISION_UUID_STR)) {
						ReadDeviceInformationCallback callback = mReadHWRevisionCallback;
						mReadHWRevisionCallback = null;
						
						LogUtil.d(TAG, "read device information: " + new String(ch.getValue()));
						
						callback.onFinish(0,
								new String(ch.getValue())); 
					} else if(null != mReadSWRevisionCallback && ch.getUuid().toString().equals(SOFTWARE_REVISION_UUID_STR)) {
						ReadDeviceInformationCallback callback = mReadSWRevisionCallback;
						mReadSWRevisionCallback = null;
						
						LogUtil.d(TAG, "read device information: " + new String(ch.getValue()));
						
						callback.onFinish(0,
								new String(ch.getValue())); 
					} else {
						LogUtil.e(TAG,
								" no read device information exist while on  onCharacteristicRead ");
						assert (false);
					}
				} else {
					
					if(null != mReadManufactoryNameCallback && ch.getUuid().toString().equals(MANUFACTURER_NAME_UUID_STR)) {
						ReadDeviceInformationCallback callback = mReadManufactoryNameCallback;
						mReadManufactoryNameCallback = null;
						
						callback.onFinish(-1, null);
					} else if(null != mReadModelNumberCallback && ch.getUuid().toString().equals(MODEL_NUMBER_UUID_STR)) {
						ReadDeviceInformationCallback callback = mReadModelNumberCallback;
						mReadModelNumberCallback = null;
						
						callback.onFinish(-1, null); 
					} else if(null != mReadHWRevisionCallback && ch.getUuid().toString().equals(HARDWARE_REVISION_UUID_STR)) {
						ReadDeviceInformationCallback callback = mReadHWRevisionCallback;
						mReadHWRevisionCallback = null;
						
						callback.onFinish(-1, null);
					} else if(null != mReadSWRevisionCallback && ch.getUuid().toString().equals(SOFTWARE_REVISION_UUID_STR)) {
						ReadDeviceInformationCallback callback = mReadSWRevisionCallback;
						mReadSWRevisionCallback = null;
						
						callback.onFinish(-1, null);
					} else {
						LogUtil.e(TAG,
								" no read device information exist while on  onCharacteristicRead ");
						assert (false);
					}
				}
				removeMessages(MSG_READ_DEVICE_INFORMATION_TIMEOUNT);

			} else {
				LogUtil.e(TAG, " not support service : "
						+ ch.getService().getUuid().toString());
				assert (false);
			}
		
		}
	};
	
	private void broadcastConnectState(int state) {
		LogUtil.d(TAG, " broadcastConnectState state: " + state);
		Intent connectStateIntent = new Intent(
				ACTION_BLE_SM_CONNECT_STATE);
		connectStateIntent.putExtra(EXTRA_BLE_SM_CONNECT_STATE, state);
		mLocalBroadcastManager.sendBroadcast(connectStateIntent);
	}
	 
	private void broadcastConnectState(int state,String deviceAddress,String deviceName) {
		LogUtil.d(TAG, " broadcastConnectState state: " + state + " deviceAddress: " + deviceAddress + " deviceName:" + deviceName);
		Intent connectStateIntent = new Intent(
				ACTION_BLE_SM_CONNECT_STATE);
		connectStateIntent.putExtra(EXTRA_BLE_SM_CONNECT_STATE, state);
		connectStateIntent.putExtra(EXTRA_BLE_SM_CONNECT_STATE_DEVICE_ADDRESS, deviceAddress);
		connectStateIntent.putExtra(EXTRA_BLE_SM_CONNECT_STATE_DEVICE_NAME, deviceName);
		mLocalBroadcastManager.sendBroadcast(connectStateIntent);
	}
	 
	
	private abstract class BleBaseState extends State {
		@Override
		public void enter() {
			super.enter();
		}
		
		private boolean processIntent(Intent intent) {
			if(intent.getAction().equals(ACTION_BLE_SM_CONNECT_COMMAND)) {	
				LogUtil.d(TAG,"BleBaseState processIntent ACTION_BLE_SM_CONNECT_COMMAND command:" + intent.getIntExtra(EXTRA_BLE_SM_CONNECT_COMMAND,BLE_SM_CONNECT_COMMAND_INVALID) );
				if(intent.getIntExtra(EXTRA_BLE_SM_CONNECT_COMMAND,BLE_SM_CONNECT_COMMAND_INVALID) == BLE_SM_CONNECT_COMMAND_FINALIZE) {
					LogUtil.d(TAG,"receive BLE_SM_CONNECT_COMMAND_FINALIZE command");
					
					mConnectDeviceAddress = null;
					mConnectRetryCount = BLE_SM_CONNECT_COMMAND_RETRY_COUNT_INVALID;
					mScanRetryCount = BLE_SM_CONNECT_COMMAND_RETRY_COUNT_INVALID;
					mConnectRetryTime = 2000;
					
					if(getCurrentState() != mIdleState) {
						onFinalize();
						transitionTo(mIdleState);
					}
					return true;
				}
				
			} 
			
			return false;
		}
		
		@Override
		public boolean processMessage(Message msg) {
			LogUtil.d(TAG,"BleBaseState processMessage what:" + msg.what);
			switch (msg.what) {
			case MSG_RECEIVE_INTENT:
				Intent intent = (Intent)msg.obj;
				if(processIntent(intent)) {
					return HANDLED;
				}
				break;
			case MSG_FINALIZE:
				LogUtil.d(TAG,"receive  MSG_FINALIZE");
				mConnectDeviceAddress = null;
				mConnectRetryCount = BLE_SM_CONNECT_COMMAND_RETRY_COUNT_INVALID;
				mScanRetryCount = BLE_SM_CONNECT_COMMAND_RETRY_COUNT_INVALID;
				mConnectRetryTime = 2000;
				
				if(getCurrentState() != mIdleState) {
					onFinalize();
					transitionTo(mIdleState);
				}
				return HANDLED;
				
			}
			return NOT_HANDLED;
		}
		@Override
		public void exit() {
			super.exit();
		}
		
		protected abstract void onError();
		
		protected abstract void onFinalize();
	}
	
	private abstract class BleConnectBaseState extends BleBaseState {
		protected BluetoothDevice mDevice = null;
		protected BluetoothGatt mGatt = null;
		
		@Override
		public void enter() {
			LogUtil.e(TAG, "enter BleConnectBaseState");
			super.enter();
			
			if(null == mDevice || null == mGatt) {
				LogUtil.e(TAG, "mDevice or mGatt is null");
				assert(false);
				transitionTo(mIdleState);
			}
		
		}
		
		@Override
		public boolean processMessage(Message msg) {
			boolean ret = NOT_HANDLED;
			
			LogUtil.d(TAG,"BleConnectBaseState processMessage what:" + msg.what);
			
			switch (msg.what) {
			case MSG_DEVICE_DISCONNECTED:
				LogUtil.d(TAG, "BleConnectBaseState MSG_DEVICE_DISCONNECTED message received");
				onDisconnected();
				//refreshGatt(mGatt);
				mGatt.disconnect();
				mGatt.close();
				mGatt = null;
				transitionTo(mIdleState);
				ret = HANDLED;
				break;
			}
			
			return ret|super.processMessage(msg);
		}
		
		@Override
		public void exit() {
			LogUtil.d(TAG, "exit BleConnectBaseState");
			
			super.exit();
			
			mDevice = null;
			mGatt = null;
		}
		
		protected void onError() {
			LogUtil.d(TAG, "onError BleConnectBaseState");
			
		}
		
		public void setDevice(BluetoothDevice device) {
			LogUtil.d(TAG, "BleConnectBaseState setDevice address:" + device.getAddress());
			mDevice = device;		
		}
		
		public void setGatt(BluetoothGatt gatt) {
			LogUtil.d(TAG, "BleConnectBaseState setGatt ");
			mGatt = gatt;		
		}
		
		@Override
		protected void onFinalize() {
			LogUtil.d(TAG, "onFinalize BleConnectBaseState");
			if(null != mGatt) {
				LogUtil.d(TAG, "disconnect gatt");
				removeBond(mDevice);
				refreshGatt(mGatt);
				mGatt.disconnect();
				mGatt.close();
				mGatt = null;
			}
		}
		
		abstract  void onDisconnected();
	}
	
	private class IdleState extends BleBaseState {
		private final int RETRY_SCAN_TIME = 2*1000; //in second
		private final int RETRY_CONNECT_TIME_MAX = 15*1000; //in second
		private final int RETRY_CONNECT_TIME_ADD = 2*1000; //in second
		
		private Timer mRetryTimer = null;
		
		class RetryTimeTask extends TimerTask {
	        @Override
	        public void run() {
	            LogUtil.d(TAG, "RetryTimeTask");
	            
	            sendMessage(MSG_STATE_TIMEOUT);             
	        }
	    }
		
		private void onTimeout() {
			
			 LogUtil.d(TAG, "mScanRetryCount:" + mScanRetryCount + " mConnectRetryCount:" + mConnectRetryCount);
			 
			 if(mScanRetryCount > 0 || mScanRetryCount == BLE_SM_CONNECT_COMMAND_RETRY_COUNT_INFINITE) {
				 
				   if(!BluetoothAdapter.getDefaultAdapter().isEnabled()) {
						LogUtil.d(TAG,"ble is disabled, wait it enable");
						
						mRetryTimer = new Timer();
						mRetryTimer.schedule(new RetryTimeTask(),
								RETRY_SCAN_TIME);
						
						return;
					}
				   
				 	if(mScanRetryCount > 0) {
	            		mScanRetryCount --;
	            	}
				 
	            	broadcastConnectState(BLE_SM_CONNECT_STATE_SCANNING);
					transitionTo(mScanState);
	            } else if(mConnectRetryCount > 0 || mConnectRetryCount == BLE_SM_CONNECT_COMMAND_RETRY_COUNT_INFINITE) {
	            	BluetoothDevice device = null;
	            	
	            	if(!BluetoothAdapter.getDefaultAdapter().isEnabled()) {
						LogUtil.d(TAG,"ble is disabled, wait it enable");
						
						mRetryTimer = new Timer();
						mRetryTimer.schedule(new RetryTimeTask(),
								mConnectRetryTime);
						
						return;
					}
	            	
	            	
	            	if(mConnectRetryCount > 0) {
	            		mConnectRetryCount --;
	            	}
	            	
	            	if(mConnectRetryTime < RETRY_CONNECT_TIME_MAX) {
	            		mConnectRetryTime += RETRY_CONNECT_TIME_ADD;
	            	}
	            	
	            	mScanState.setDeviceAddress(mConnectDeviceAddress);
					broadcastConnectState(BLE_SM_CONNECT_STATE_CONNECTING);
					transitionTo(mScanState);
	            }      
		}
		
		@Override
		public void enter() {
			LogUtil.d(TAG, "Enter IdleState");
			super.enter();	
			
			 
			LogUtil.d(TAG, "mScanRetryCount:" + mScanRetryCount + " mConnectRetryCount:" + mConnectRetryCount);
			
			if(mErrorCount  > NEED_RESET_BLE_ERROR_COUNT) {
				LogUtil.d(TAG, "mErrorCount big than NEED_RESET_BLE_ERROR_COUNT, need to disale the bluetooth");
				
				if(null != mConnectDeviceAddress) {
					BluetoothDevice device = getBluetoothDevice(mConnectDeviceAddress);
					if(null != device && device.getBondState() == BluetoothDevice.BOND_BONDED) {
						removeBond(device);
					}
				}
				
				releaseWakeLock();
				
				mErrorCount = 0;
			}
			 
			if(mScanRetryCount > 0 || mScanRetryCount == BLE_SM_CONNECT_COMMAND_RETRY_COUNT_INFINITE) {
				broadcastConnectState(BLE_SM_CONNECT_STATE_WAIT_RETRY);
				
				mRetryTimer = new Timer();
				mRetryTimer.schedule(new RetryTimeTask(),
						RETRY_SCAN_TIME);
				
			} else if ( mConnectRetryCount > 0 || mConnectRetryCount == BLE_SM_CONNECT_COMMAND_RETRY_COUNT_INFINITE) {
				broadcastConnectState(BLE_SM_CONNECT_STATE_WAIT_RETRY);
            	
				mRetryTimer = new Timer();
				mRetryTimer.schedule(new RetryTimeTask(),
						mConnectRetryTime);	
			} else {
				broadcastConnectState(BLE_SM_CONNECT_STATE_DISCONNECTED);
			}
		}
		
		private boolean processIntent(Intent intent) {
			if(intent.getAction().equals(ACTION_BLE_SM_CONNECT_COMMAND)) {		
				if(intent.getIntExtra(EXTRA_BLE_SM_CONNECT_COMMAND,BLE_SM_CONNECT_COMMAND_INVALID) == BLE_SM_CONNECT_COMMAND_START_SCAN) {
					LogUtil.d(TAG,"receive BLE_SM_CONNETE_COMMAND_START_SCAN command");
					
					mConnectDeviceAddress = null;
					mConnectRetryCount = BLE_SM_CONNECT_COMMAND_RETRY_COUNT_INVALID;
					mScanRetryCount = intent.getIntExtra(EXTRA_BLE_SM_CONNECT_COMMAND_RETRY_COUNT,BLE_SM_CONNECT_COMMAND_RETRY_COUNT_INVALID);
					
					if(BluetoothAdapter.getDefaultAdapter().isEnabled()) {
						LogUtil.d(TAG,"ble is enbled , start to scan");
						broadcastConnectState(BLE_SM_CONNECT_STATE_SCANNING);
						transitionTo(mScanState);
					} else {
						LogUtil.d(TAG,"ble is disabled , wait for enabled");
						
						if(mScanRetryCount != BLE_SM_CONNECT_COMMAND_RETRY_COUNT_INFINITE) {
							if(mScanRetryCount == BLE_SM_CONNECT_COMMAND_RETRY_COUNT_INVALID) {
								mScanRetryCount = 1;
							} else {
								mScanRetryCount ++;
							}
						}
						
						mRetryTimer = new Timer();
						mRetryTimer.schedule(new RetryTimeTask(),
								RETRY_SCAN_TIME);
					}
									
					return true;
				} else {
					if(intent.getIntExtra(EXTRA_BLE_SM_CONNECT_COMMAND,BLE_SM_CONNECT_COMMAND_INVALID) == BLE_SM_CONNECT_COMMAND_START_CONNECT) {
						LogUtil.d(TAG,"receive BLE_SM_CONNETE_COMMAND_START_CONNECT command");
									
						mScanRetryCount = BLE_SM_CONNECT_COMMAND_RETRY_COUNT_INVALID;
						
						mConnectRetryTime = 2000;
						
						mConnectRetryCount = intent.getIntExtra(EXTRA_BLE_SM_CONNECT_COMMAND_RETRY_COUNT,BLE_SM_CONNECT_COMMAND_RETRY_COUNT_INVALID);
						mConnectDeviceAddress = intent.getStringExtra(EXTRA_BLE_SM_CONNECT_COMMAND_DEVICE_ADDRESS);
						
						if(BluetoothAdapter.getDefaultAdapter().isEnabled()) {
							LogUtil.d(TAG,"ble is enbled , start to scan");
			            	mScanState.setDeviceAddress(mConnectDeviceAddress);
							broadcastConnectState(BLE_SM_CONNECT_STATE_CONNECTING);
							transitionTo(mScanState);
						} else {
							LogUtil.d(TAG,"ble is disabled , wait for enabled");
							
							if(mConnectRetryCount != BLE_SM_CONNECT_COMMAND_RETRY_COUNT_INFINITE) {
								if(mConnectRetryCount == BLE_SM_CONNECT_COMMAND_RETRY_COUNT_INVALID) {
									mConnectRetryCount = 1;
								} else {
									mConnectRetryCount ++;
								}
							}
							
							mRetryTimer = new Timer();
							mRetryTimer.schedule(new RetryTimeTask(),
									mConnectRetryTime);	
						}
					
						
						return true;
					}
				}
				
			} else {
				LogUtil.e(TAG, "action not support in BleBaseState.processIntent: " + intent.getAction());
				assert(false);
				return false;
			}
			
			return false;
		}
		
		@Override
		public boolean processMessage(Message msg) {
			boolean ret = NOT_HANDLED;
			
			LogUtil.d(TAG,"IdleState processMessage what:" + msg.what);
			
			switch (msg.what) {
			case MSG_RECEIVE_INTENT:
				Intent intent = (Intent)msg.obj;
				if(processIntent(intent)) {
					ret = HANDLED;
				} 
				break;
			case MSG_STATE_TIMEOUT:
				onTimeout();	
				ret = HANDLED;
				break;
			}
			return ret|super.processMessage(msg);
		}
		
		@Override
		public void exit() {
			LogUtil.d(TAG, "Exit IdleState");
			
			super.exit();
		}

		@Override
		protected void onError() {
			LogUtil.d(TAG, "onError IdleState");
			
			broadcastConnectState(BLE_SM_CONNECT_STATE_DISCONNECTED);
		}

		@Override
		protected void onFinalize() {
			LogUtil.d(TAG, "onFinalize IdleState");
			
		}
	}
	
	private class ScanState extends BleBaseState {
		private final int SCAN_TIMEOUT = 20*1000; //in second
		
		private final int TARGET_DEVICE_RSSI_MIN_VALUE = -65;
		
		private Timer mScanTimer = null;
	
		
		private String mDeviceAddress = null;
		
		private class ScanResult {
			public BluetoothDevice mDevice = null;
			public int mrssi;
		}
		
		private List<ScanResult> mDevicesList = null;
		
		private BluetoothAdapter.LeScanCallback mLeScanCallback =
				new BluetoothAdapter.LeScanCallback() {

			@Override
			public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
				LogUtil.e(TAG, "device name:" + device.getName() + ", device address: " + device.getAddress() + " RSSI:" + rssi);
				if (isTargetDevice(device, rssi)) {	
					Message msg = obtainMessage(MSG_DEVICE_FIND);
					Bundle args = new Bundle();
					args.putParcelable(KEY_DEVIC_FIND_DEVICE, device);
					args.putInt(KEY_DEVIC_FIND_RSSI, rssi);
					msg.setData(args);
					sendMessage(msg);
					
					if(null == mDeviceAddress && rssi >= TARGET_DEVICE_RSSI_MIN_VALUE) {
						LogUtil.e(TAG, "get the target rssi device"); 
			            sendMessage(MSG_STATE_TIMEOUT);	
					}
				}
			}
		};
		
		class ScanTimeoutTask extends TimerTask {
	        @Override
	        public void run() {
	            LogUtil.e(TAG, "scan timeout"); 
	            sendMessage(MSG_STATE_TIMEOUT);
	        }
	    }
		
		private void onTimeout() {
			
			stopScan();
			
			if(null == mDeviceAddress && mDevicesList.size() != 0) {

				ScanResult targetResult = null;
				for(int i=0; i<mDevicesList.size(); i++) {
					if(null == targetResult) {
						targetResult = mDevicesList.get(i);
					} else {
						if(targetResult.mrssi < mDevicesList.get(i).mrssi) {
							targetResult = mDevicesList.get(i);
						}
					}
					
				}
				
				LogUtil.d(TAG, "target device address:" + targetResult.mDevice.getAddress() + " rssi:" + targetResult.mrssi); 
				Intent intent = new Intent(BluetoothState.ACTION_BLE_SCAN_RESULT);
				intent.putExtra(BluetoothState.EXTRA_BLE_SCAN_RESULT, BluetoothState.BLE_SCAN_RESULT_SUCCESS);
				intent.putExtra(BluetoothState.EXTRA_BLE_SCAN_RESULT_DEVICE_ADDRESS, targetResult.mDevice.getAddress());
				intent.putExtra(BluetoothState.EXTRA_BLE_SCAN_RESULT_DEVICE_NAME, targetResult.mDevice.getName());
				mLocalBroadcastManager.sendBroadcast(intent);			
				 
				mScanRetryCount = BLE_SM_CONNECT_COMMAND_RETRY_COUNT_INVALID;
					
				mConnectRetryTime = 2000;
				mConnectRetryCount = BLE_SM_CONNECT_COMMAND_RETRY_COUNT_INVALID;
				mConnectDeviceAddress =  targetResult.mDevice.getAddress();
				
				mBindState.setDevice(targetResult.mDevice);
				broadcastConnectState(BLE_SM_CONNECT_STATE_CONNECTING);
				transitionTo(mBindState);
				
			} else {
			
				onError();   
			}
		}
		
		public void setDeviceAddress(String deviceAddress) {
			LogUtil.d(TAG, "setDeviceAddress address:" + deviceAddress);
			mDeviceAddress = deviceAddress;
		}
		
		@Override
		public void enter() {
			LogUtil.d(TAG, "Enter ScanState");
			
			super.enter();
			
		    if(null == mDeviceAddress) {
		    	mDevicesList = new ArrayList<ScanResult>();
		    } 
			
			startScan();
		}
				
		@Override
		public boolean processMessage(Message msg) {
			boolean ret = NOT_HANDLED;
			
			LogUtil.d(TAG,"ScanState processMessage what:" + msg.what);
			
			switch (msg.what) {
			case MSG_DEVICE_FIND:
				if(null == mDeviceAddress) {
					boolean isInList = false;
					BluetoothDevice device = (BluetoothDevice)(msg.getData().getParcelable(KEY_DEVIC_FIND_DEVICE));
					for(int i=0; i<mDevicesList.size(); i++) {
						if(mDevicesList.get(i).mDevice.getAddress().equals(device.getAddress())) {
							isInList = true;
							break;
						}	
					}
					
					if(!isInList) {
					
						LogUtil.d(TAG,"new deivce address :" + device.getAddress());
						ScanResult scanResult = new ScanResult();
						scanResult.mDevice =device;
						scanResult.mrssi = msg.getData().getInt(KEY_DEVIC_FIND_RSSI);
						
						mDevicesList.add(scanResult);
					}
				} else {
					BluetoothDevice device = (BluetoothDevice)(msg.getData().getParcelable(KEY_DEVIC_FIND_DEVICE));
					if(mDeviceAddress.equals(device.getAddress())) {
						LogUtil.d(TAG, "target device address:" + device.getAddress() + " rssi:" + msg.getData().getInt(KEY_DEVIC_FIND_RSSI)); 
						
						stopScan();
						
						Intent intent = new Intent(BluetoothState.ACTION_BLE_SCAN_RESULT);
						intent.putExtra(BluetoothState.EXTRA_BLE_SCAN_RESULT, BluetoothState.BLE_SCAN_RESULT_SUCCESS);
						intent.putExtra(BluetoothState.EXTRA_BLE_SCAN_RESULT_DEVICE_ADDRESS, device.getAddress());
						intent.putExtra(BluetoothState.EXTRA_BLE_SCAN_RESULT_DEVICE_NAME, device.getName());
						mLocalBroadcastManager.sendBroadcast(intent);			
						
						mBindState.setDevice(device);
						transitionTo(mBindState);
					}
				}
				
				ret = HANDLED;
				break;
			case MSG_STATE_TIMEOUT:
				onTimeout();
				ret = HANDLED;
				break;
			}
			
			return ret|super.processMessage(msg);
		}
		
		@Override
		public void exit() {
			LogUtil.d(TAG, "Exit ScanState");
			
			if(null != mDevicesList) {
				mDevicesList.clear();
				mDevicesList = null;
			}
			
			if(null != mDeviceAddress) {
				mDeviceAddress = null;
			}
			stopTimer();
			super.exit();
		}
		
		private void startScan() {
			LogUtil.d(TAG, "startScan");
			boolean ret = mBtAdapter.startLeScan(mLeScanCallback);
			
			if(ret) {
				mScanTimer = new Timer();
				mScanTimer.schedule(new ScanTimeoutTask(),
						SCAN_TIMEOUT);
			} else {
				LogUtil.e(TAG, "call startLeScan error");
				onError();
			}			
		}
		
		
		private void stopScan() {
			LogUtil.d(TAG, "stopScan");
			if(mBtAdapter != null){
				mBtAdapter.stopLeScan(mLeScanCallback);
			}
		}
		
		private void stopTimer() {
			if(null != mScanTimer) {
				mScanTimer.cancel();
				mScanTimer.purge();
				mScanTimer = null;
				
			}
		}

		@Override
		protected void onError() {
			LogUtil.d(TAG, "onError ScanState");
			
			stopScan();
			
			LogUtil.d(TAG, "mScanRetryCount:" + mScanRetryCount);
			
			if(mScanRetryCount <= 0 && mScanRetryCount != BLE_SM_CONNECT_COMMAND_RETRY_COUNT_INFINITE) {
				Intent intent = new Intent(BluetoothState.ACTION_BLE_SCAN_RESULT);
				intent.putExtra(BluetoothState.EXTRA_BLE_SCAN_RESULT, BluetoothState.BLE_SCAN_RESULT_ERROR);
				mLocalBroadcastManager.sendBroadcast(intent);	
			}
			
			mErrorCount ++;
			
			
	        transitionTo(mIdleState);  		
		}

		@Override
		protected void onFinalize() {
			LogUtil.d(TAG, "onFinalizes ScanState");
			
			stopScan();
		}
	}
	
	private class BindState extends BleBaseState {
		private BluetoothDevice mDevice = null;
		
		private Timer mBondTimer = null;
		private final int BOND_TIMEOUT = 40*1000;//in second
		
		class BondTimeoutTask extends TimerTask {
	        @Override
	        public void run() {
	            LogUtil.e(TAG, "bond timeout");  
	            sendMessage(MSG_STATE_TIMEOUT);
	        } 
	    }
		
		
		private void onTimeout() {
			onError();
		}
		
		@Override
		public void enter() {
			LogUtil.d(TAG, "Enter BindState");
			
			super.enter();
			
			if(null == mDevice) {
				LogUtil.e(TAG, "mDevice is null");
				assert(false);
				transitionTo(mIdleState);
			}
			
			if(mDevice.getBondState() == BluetoothDevice.BOND_BONDING) {
				LogUtil.w(TAG, "device is bonding by another app");
				createBond(); //TODO:is create bond again for binding device ok?
			} else if(mDevice.getBondState() == BluetoothDevice.BOND_NONE){
				LogUtil.d(TAG, "device is not bond, so  create bond for it");
				createBond();
			} else if(mDevice.getBondState() == BluetoothDevice.BOND_BONDED){
				LogUtil.d(TAG, "device have been bond");
				mConnectState.setDevice(mDevice);
				transitionTo(mConnectState);			
			} else {
				LogUtil.e(TAG, "bond state not valid");
				assert(false);
				transitionTo(mIdleState);
			}

		}
		
		private boolean processIntent(Intent intent) {
			if(intent.getAction().equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)) {		
				 int state = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, -1);
				switch (state) {
				case BluetoothDevice.BOND_NONE:
					LogUtil.e(TAG, "bond error");
					onError();
					break;
				case BluetoothDevice.BOND_BONDING:
					LogUtil.d(TAG, "bond in process");
					//do nothing here
					break;
				case BluetoothDevice.BOND_BONDED:
					LogUtil.d(TAG, "device bond success");
					mConnectState.setDevice(mDevice);
					transitionTo(mConnectState);
					break;
				}
			} 
			
			return false;
		}
		
		@Override
		public boolean processMessage(Message msg) {
			boolean ret = NOT_HANDLED;
			
			LogUtil.d(TAG,"BindState processMessage what:" + msg.what);
			
			switch (msg.what) {
			case MSG_RECEIVE_INTENT:
				Intent intent = (Intent)msg.obj;
				if(processIntent(intent)) {
					ret = HANDLED;
				}
				break;
			case MSG_STATE_TIMEOUT:
				onTimeout();
				ret = HANDLED;
				break;
			}
			return ret|super.processMessage(msg);
		}
	
		
		@Override
		public void exit() {
			LogUtil.d(TAG, "Exit BindState");
			
			mDevice = null;
			stopTimer();
			
			super.exit();
		}
		
		public void setDevice(BluetoothDevice device) {
			LogUtil.d(TAG, "BindState setDevice address:" + device.getAddress());
			mDevice = device;		
		}
		
		private void createBond() {
			LogUtil.d(TAG, "createBond");
			boolean ret = mDevice.createBond();
			
			if(ret) {
				mBondTimer = new Timer();
				mBondTimer.schedule(new BondTimeoutTask(),
						BOND_TIMEOUT);
			} else {
				LogUtil.e(TAG, "call createBond error");
				onError();
			}			
		}
		
		private void stopTimer() {
			LogUtil.d(TAG, "stopTimer");
			
			if(null != mBondTimer) {
				mBondTimer.cancel();
				mBondTimer.purge();
				mBondTimer = null;
				
			}
		}

		@Override
		protected void onError() {
			LogUtil.d(TAG, "onError BindState");
			mErrorCount ++;
			transitionTo(mIdleState);
		}

		@Override
		protected void onFinalize() {
			LogUtil.d(TAG, "onFinalize BindState");
			
		}
	}
	
	private class ConnectState extends BleBaseState {
		private BluetoothDevice mDevice = null;
		private BluetoothGatt mGatt = null;
		
		private Timer mConnectTimer = null;
		private final int CONNECT_TIMEOUT = 30*1000;//in second
		
		class ConnectTimeoutTask extends TimerTask {
	        @Override
	        public void run() {
	            LogUtil.e(TAG, "connect timeout");
	             sendMessage(MSG_STATE_TIMEOUT);
	        }
	    }
		
		private void onTimeout() {
			onError(); 
		}
		
		@Override
		public void enter() {
			LogUtil.d(TAG, "Enter ConnectState");
			
			super.enter();
			
			if(null == mDevice) {
				LogUtil.e(TAG, "mDevice is null");
				assert(false);
				transitionTo(mIdleState);
			}
			
			startConnect();

		}
		
		
		@Override
		public boolean processMessage(Message msg) {
			boolean ret = NOT_HANDLED;
			
			LogUtil.d(TAG,"ConnectState processMessage what:" + msg.what);
			
			switch (msg.what) {
			case MSG_DEVICE_CONNECTED:
				mConfigState.setDevice(mDevice);
				mConfigState.setGatt((BluetoothGatt)(msg.obj));
				transitionTo(mConfigState);
				ret = HANDLED;
				break;
			case MSG_STATE_TIMEOUT:
				onTimeout();
				ret = HANDLED;
				break;
			}
			
			return ret|super.processMessage(msg);
		}
		
		@Override
		public void exit() {
			LogUtil.d(TAG, "Exit ConnectState");
			mDevice = null;
			mGatt = null;
			stopTimer();
			
			super.exit();
		}
		
		public void setDevice(BluetoothDevice device) {
			LogUtil.d(TAG, "ConnectState setDevice address:" + device.getAddress());
			mDevice = device;		
		}
		
		private void startConnect() {
			LogUtil.d(TAG, "startConnect");
			mGatt = mDevice.connectGatt(mContext, false, mBtGattCallback);
			
			if(null != mGatt) {
				mConnectTimer = new Timer();
				mConnectTimer.schedule(new ConnectTimeoutTask(),
						CONNECT_TIMEOUT);
			} else {
				LogUtil.e(TAG, "startConnect error");
				transitionTo(mIdleState);
			}			
		}
		
		private void stopTimer() {
			LogUtil.d(TAG, "stopTimer");
			
			if(null != mConnectTimer) {
				mConnectTimer.cancel();
				mConnectTimer.purge();
				mConnectTimer = null;		
			}
		}

		@Override
		protected void onError() {
			LogUtil.d(TAG, "onError ConnectState");
			if(null != mGatt) {
				mGatt.disconnect();
				mGatt.close();
				mGatt = null;
			}
			
			mErrorCount ++;
			transitionTo(mIdleState);  
			
		}


		@Override
		protected void onFinalize() {
			LogUtil.d(TAG, "onFinalize ConnectState");
			if(null != mGatt) {
				mGatt.disconnect();
				mGatt.close();
				mGatt = null;
			}
		}
	}
	
	private class ConfigState extends BleConnectBaseState {
		
		private Timer mConfigTimer = null;
		
		private final int CONFIG_TIMEOUT = 30*1000;//in second
		
		
		private final int STEP_INVALID = -1;
		private final int STEP_DISCOVER_SERVICE = 0;
		private final int STEP_SET_NORIFY = STEP_DISCOVER_SERVICE + 1;
		
		private int mCurrentStep = STEP_INVALID;
		
		
		class ConfigTimeoutTask extends TimerTask {
	        @Override
			public void run() {
				LogUtil.e(TAG, "config timeout mCurrentStep:" + mCurrentStep);
				sendMessage(MSG_STATE_TIMEOUT);
				        
	        }
	    }
		
		private void onTimeout() {
			switch (mCurrentStep) {
			case STEP_DISCOVER_SERVICE:

				break;
			case STEP_SET_NORIFY:

				break;
			default:
				LogUtil.e(TAG, "not valid state:" + mCurrentStep);
				assert (false);
				break;

			}
            onError();   
		}
		
		@Override
		public void enter() {
			LogUtil.d(TAG, "Enter ConfigState");
			
			super.enter();
			
			if(mNeedRefreshGatt ) {
				mNeedRefreshGatt = false;
				refreshGatt(mGatt);
			}
			
			startDiscoverService();
		}	
		
		@Override
		public boolean processMessage(Message msg) {
			boolean ret = NOT_HANDLED;
			
			LogUtil.d(TAG,"ConfigState processMessage what:" + msg.what);
			
			switch (msg.what) {
			case MSG_SERVICE_DISCOVERED_SUCCESS:
			case MSG_SERVICE_DISCOVERED_ERROR:
				assert(STEP_DISCOVER_SERVICE == mCurrentStep);
				if(MSG_SERVICE_DISCOVERED_SUCCESS == msg.what) {
					getServicesAndSetNotify();
					mCurrentStep = STEP_SET_NORIFY;
				} else {
					LogUtil.e(TAG, "discover service error");
					onError();  
				}
				return HANDLED;
			case MSG_UART_RO_SET_NOTIFY_SUCCESS:
			case MSG_UART_RO_SET_NOTIFY_ERROR:	
				assert(STEP_SET_NORIFY == mCurrentStep);
				if(MSG_UART_RO_SET_NOTIFY_SUCCESS == msg.what) {
					mOKState.setDevice(mDevice);
					mOKState.setGatt(mGatt);
					transitionTo(mOKState);
				} else {
					LogUtil.e(TAG, "set uart ro charactoristic notify error");
					onError();
				}
				
				return HANDLED;
			case MSG_STATE_TIMEOUT:
				onTimeout();
				ret = HANDLED;
				break;
			}
			
			return ret|super.processMessage(msg);
		}
		
		@Override
		public void exit() {
			LogUtil.d(TAG, "Exit ConfigState");
			
			mCurrentStep = STEP_INVALID;
			stopTimer();
			
			super.exit();
		}
		
		private void startDiscoverService() {
			LogUtil.d(TAG, "startDiscoverService");
			boolean ret = mGatt.discoverServices();
			
			if(ret) {
				mConfigTimer = new Timer();
				mConfigTimer.schedule(new ConfigTimeoutTask(),
						CONFIG_TIMEOUT);
				mCurrentStep = STEP_DISCOVER_SERVICE;
			} else {
				LogUtil.e(TAG, "startDiscoverService error");
				onError();  
			}			
		}
		
		private void stopTimer() {
			LogUtil.d(TAG, "stopTimer");
			
			if(null != mConfigTimer) {
				mConfigTimer.cancel();
				mConfigTimer.purge();
				mConfigTimer = null;		
			}
			
		}
		
		@Override
		protected void onDisconnected() {
			LogUtil.d(TAG, "ConfigState onDisconnected");
		}
		
		private boolean setCharacteristicNotification(BluetoothGattCharacteristic ch, boolean enable) {
			LogUtil.d(TAG, "setCharacteristicNotification");
			if (mGatt == null) {
				LogUtil.e(TAG, "setCharacteristicNotification mGatt is  null");
				return false;
			}
			
			boolean ret = mGatt.setCharacteristicNotification(ch, enable);
			if (! ret) {
				LogUtil.e(TAG, "setCharacteristicNotification setCharacteristicNotification error");
				return false;
			}
			BluetoothGattDescriptor descriptor = ch.getDescriptor(UUID.fromString(CLIENT_CHARACTERISTIC_CONFIG));
			if(null == descriptor) {
				LogUtil.e(TAG, "setCharacteristicNotification get CLIENT_CHARACTERISTIC_CONFIG descriptor null");
				return false;
			}
			ret = descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
			if (!ret) {
				LogUtil.e(TAG, "setCharacteristicNotification setValue  for descripter  error");
				return false;
			}
			ret = mGatt.writeDescriptor(descriptor);
			if (!ret) {
				LogUtil.e(TAG, "setCharacteristicNotification writeDescriptor error");
				return false;
			} 
			
			return true;
		}
		
		private void getServicesAndSetNotify() {
			LogUtil.d(TAG, "startDiscoverService");
			BluetoothGattService uartService = mGatt.getService(UUID.fromString(UART_UUID_STR));
			if (uartService == null) {
				mNeedRefreshGatt = true;
				LogUtil.e(TAG, "get uart service null");
				onError();  		
				
				return;
			}
			
			BluetoothGattCharacteristic uart_ro_ch = uartService.getCharacteristic(UUID
					.fromString(UART_NO_UUID_STR));
			if (uart_ro_ch == null) {
				mNeedRefreshGatt = true;
				LogUtil.e(TAG, "get uart ro character null");
				onError();  	
				
				return;
			}
			if(!setCharacteristicNotification(uart_ro_ch, true)) {
				mNeedRefreshGatt = true;
				LogUtil.e(TAG, "setCharacteristicNotification error");
				onError();  
				
				return;
			}
			
			
			BluetoothGattService batteryService = mGatt.getService(UUID.fromString(BATTERY_UUID_STR));
			if (batteryService == null) {
				mNeedRefreshGatt = true;
				LogUtil.e(TAG, "get battery service null");
				onError();  
				
				return;
			}		
		}
		
		protected void onError() {
			LogUtil.d(TAG, "onError ConfigState");
			if(null != mGatt) {
				mGatt.disconnect();
				mGatt.close();
				mGatt = null;
			}
			
			mErrorCount ++;
			
			transitionTo(mIdleState);
			
		}
		
		@Override
		protected void onFinalize() {
			LogUtil.d(TAG, "onFinalize ConfigState");
			super.onFinalize();
		}
	}
	
	
	
	private class OKState extends BleConnectBaseState {
		private BluetoothGattService mUartService = null;
		private BluetoothGattCharacteristic mUartROCharatoristic = null;
		
		private BluetoothGattService mBatteryLevelService = null;
		private BluetoothGattCharacteristic mBatteryLevelROCharatoristic = null;
		
		private BluetoothGattService mDeviceInformationService = null;
		private BluetoothGattCharacteristic mManufacturerNameCharatoristic = null;
		private BluetoothGattCharacteristic mModelNumberCharatoristic = null;
		private BluetoothGattCharacteristic mHardwareRevisionCharatoristic = null;
		private BluetoothGattCharacteristic mSoftwareRevisionCharatoristic = null;
		

		
		@Override
		public void enter() {
			LogUtil.d(TAG, "Enter OKState");
		
			super.enter();
			
			mUartService = mGatt.getService(UUID.fromString(UART_UUID_STR));
			if (null == mUartService) {
				LogUtil.e(TAG, "get mUartService null");
				assert(false);
				onError();
				
				return;
			}
			mUartROCharatoristic = mUartService.getCharacteristic(UUID.fromString(UART_WR_UUID_STR));
			if (null == mUartROCharatoristic) {
				LogUtil.e(TAG, "get mUartROCharatoristic null");
				assert(false);
				onError();
				
				return;
			}
			
			mBatteryLevelService = mGatt.getService(UUID.fromString(BATTERY_UUID_STR));
			if (null == mBatteryLevelService) {
				LogUtil.e(TAG, "get mBatteryLevelService null");
				assert(false);
				onError();
				
				return;
			}
			mBatteryLevelROCharatoristic = mBatteryLevelService.getCharacteristic(UUID.fromString(BATTERY_RD_UUID_STR));
			if (null == mBatteryLevelROCharatoristic) {
				LogUtil.e(TAG, "get mBatteryLevelROCharatoristic null");
				assert(false);
				onError();
				
				return;
			}
			
			mDeviceInformationService = mGatt.getService(UUID.fromString(DEVICE_INFORMATION_UUID_STR));
			if (null == mDeviceInformationService) {
				LogUtil.e(TAG, "get mDeviceInformationService null");
				assert(false);
				onError();
				
				return;
			}
			mManufacturerNameCharatoristic = mDeviceInformationService.getCharacteristic(UUID.fromString(MANUFACTURER_NAME_UUID_STR));
			if (null == mManufacturerNameCharatoristic) {
				LogUtil.e(TAG, "get mManufacturerNameCharatoristic null");
				assert(false);
				onError();
				
				return;
			}
			
			mModelNumberCharatoristic = mDeviceInformationService.getCharacteristic(UUID.fromString(MODEL_NUMBER_UUID_STR));
			if (null == mModelNumberCharatoristic) {
				LogUtil.e(TAG, "get mModelNumberCharatoristic null");
				assert(false);
				onError();
				
				return;
			}
			
			mHardwareRevisionCharatoristic = mDeviceInformationService.getCharacteristic(UUID.fromString(HARDWARE_REVISION_UUID_STR));
			if (null == mHardwareRevisionCharatoristic) {
				LogUtil.e(TAG, "get mHardwareRevisionCharatoristic null");
				assert(false);
				onError();
				
				return;
			}
			
			mSoftwareRevisionCharatoristic = mDeviceInformationService.getCharacteristic(UUID.fromString(SOFTWARE_REVISION_UUID_STR));
			if (null == mSoftwareRevisionCharatoristic) {
				LogUtil.e(TAG, "get mSoftwareRevisionCharatoristic null");
				assert(false);
				onError();
				
				return;
			}
			
			
			mIsInOKState = true;
			
			
			releaseWakeLock();
			
			broadcastConnectState(BLE_SM_CONNECT_STATE_CONNECTED,mDevice.getAddress(),mDevice.getName());
		}	
		
		@Override
		public boolean processMessage(Message msg) {
			boolean ret = NOT_HANDLED;
			
			LogUtil.d(TAG,"OKState processMessage what:" + msg.what);
			
			switch (msg.what) {
			case MSG_READ_BATTERY_LEVEL_TIMEOUNT:
				LogUtil.d(TAG, "MSG_READ_BATTERY_LEVEL_TIMEOUNT ");
				if(null != mReadBatteryLevelCallback) {
					ReadBatteryLevelCallback callback = mReadBatteryLevelCallback;
					mReadBatteryLevelCallback = null;
					callback.onFinish(-1,BluetoothState.BATTERY_LEVEL_UNKNOWN);
				}
				
				break;
			case MSG_READ_DEVICE_INFORMATION_TIMEOUNT:
				LogUtil.d(TAG, "MSG_READ_DEVICE_INFORMATION_TIMEOUNT ");
				handleReadDeviceInformationError();
				
				break;
				
			case MSG_SEND_UART_TIMEOUNT:
				LogUtil.d(TAG, "MSG_SEND_UART_TIMEOUNT ");
				if(mUartBeenSending) {
					mHealthStackL0JNITransprot.sendWriteResult(-1);
					mUartBeenSending = false;
				} 
							
				break;
			
			}
			
			return ret|super.processMessage(msg);
		}
		
		@Override
		public void exit() {
			LogUtil.d(TAG, "Exit OKState");
			
			mUartService = null;
			mUartROCharatoristic = null;
	
			stopTimer();
			
			mIsInOKState = false;
			
			
			removeMessages(MSG_READ_BATTERY_LEVEL_TIMEOUNT);
			
			removeMessages(MSG_READ_DEVICE_INFORMATION_TIMEOUNT);
			
			if(mUartBeenSending) {
				removeMessages(MSG_SEND_UART_TIMEOUNT);
				if(null != mHealthStackL0JNITransprot) {
					mHealthStackL0JNITransprot.sendWriteResult(-1);
				}
				mUartBeenSending = false;
			}
			
			if(null != mReadBatteryLevelCallback) {
				ReadBatteryLevelCallback callback = mReadBatteryLevelCallback;
				mReadBatteryLevelCallback = null;
				callback.onFinish(-1,BluetoothState.BATTERY_LEVEL_UNKNOWN);
			}
			
			handleReadDeviceInformationError();
			
			super.exit();
		}
		
		public void setDevice(BluetoothDevice device) {
			LogUtil.d(TAG, "OKState setDevice address:" + device.getAddress());
			mDevice = device;		
		}
		
		public void setGatt(BluetoothGatt gatt) {
			LogUtil.d(TAG, "OKState setGatt ");
			mGatt = gatt;		
		}
		
		
		private void stopTimer() {
			LogUtil.d(TAG, "stopTimer");
				
		}
		
		public int sendData(byte[] data) {
			boolean ret = false;
			LogUtil.d(TAG, "Data in hex: " + convertToHexString(data));
			if(null == mGatt) {
				LogUtil.d(TAG, "have not ready to send data ");
				return -1;
			}
			
			if(mUartBeenSending) {
				LogUtil.d(TAG, "a write operation is in progress, can write again ");
				return -1;
			}
				
			ret = mUartROCharatoristic.setValue(data);
			if (!ret) {
				LogUtil.e(TAG, "uart WR charactoristic setValue error");
				return -1;
			}
			ret = mGatt.writeCharacteristic(mUartROCharatoristic);
			if (!ret) {
				LogUtil.e(TAG, "uart WR charactoristic setValue error");
				return -1;
			}
			
			mUartBeenSending = true;
			
			sendMessageDelayed(MSG_SEND_UART_TIMEOUNT,SEND_UART_TIMEOUT);
						
			return 0;
		}
		
		public int readBatteryLevel() {
			boolean ret = false;
			
			if(null == mGatt) {
				LogUtil.d(TAG, "have not ready to readBatteryLevel ");
				return -1;
			}
			
			ret = mGatt.readCharacteristic(mBatteryLevelROCharatoristic);
			if (!ret) {
				LogUtil.e(TAG, "read battery level charactoristic  error");
				return -1;
			}
			
			sendMessageDelayed(MSG_READ_BATTERY_LEVEL_TIMEOUNT,READ_BATTERY_LEVEL_TIMEOUT);
						
			return 0;
		}
		
		public int readManufactureName() {
			boolean ret = false;
			
			if(null == mGatt) {
				LogUtil.d(TAG, "have not ready to readManufactureName ");
				return -1;
			}
			
			ret = mGatt.readCharacteristic(mManufacturerNameCharatoristic);
			if (!ret) {
				LogUtil.e(TAG, "read mManufacturerNameCharatoristic charactoristic  error");
				return -1;
			}
			
			sendMessageDelayed(MSG_READ_DEVICE_INFORMATION_TIMEOUNT,READ_DEVICE_INFORMATION_TIMEOUT);
						
			return 0;
		}
		
		public int readModelNumber() {
			boolean ret = false;
			
			if(null == mGatt) {
				LogUtil.d(TAG, "have not ready to readModelNumber ");
				return -1;
			}
			
			ret = mGatt.readCharacteristic(mModelNumberCharatoristic);
			if (!ret) {
				LogUtil.e(TAG, "read mModelNumberCharatoristic charactoristic  error");
				return -1;
			}
			
			sendMessageDelayed(MSG_READ_DEVICE_INFORMATION_TIMEOUNT,READ_DEVICE_INFORMATION_TIMEOUT);
						
			return 0;
		}
		
		public int readHardwareRevision() {
			boolean ret = false;
			
			if(null == mGatt) {
				LogUtil.d(TAG, "have not ready to readHardwareRevision ");
				return -1;
			}
			
			ret = mGatt.readCharacteristic(mHardwareRevisionCharatoristic);
			if (!ret) {
				LogUtil.e(TAG, "read mHardwareRevisionCharatoristic charactoristic  error");
				return -1;
			}
			
			sendMessageDelayed(MSG_READ_DEVICE_INFORMATION_TIMEOUNT,READ_DEVICE_INFORMATION_TIMEOUT);
						
			return 0;
		}
		
		public int readSoftwareRevision() {
			boolean ret = false;
			
			if(null == mGatt) {
				LogUtil.d(TAG, "have not ready to readSoftwareRevision ");
				return -1;
			}
			
			ret = mGatt.readCharacteristic(mSoftwareRevisionCharatoristic);
			if (!ret) {
				LogUtil.e(TAG, "read mSoftwareRevisionCharatoristic charactoristic  error");
				return -1;
			}
			
			sendMessageDelayed(MSG_READ_DEVICE_INFORMATION_TIMEOUNT,READ_DEVICE_INFORMATION_TIMEOUT);
						
			return 0;
		}

		@Override
		void onDisconnected() {
			LogUtil.d(TAG, "onDisconnected OKState");
			
		}
		
		protected void onError() {
			LogUtil.d(TAG, "onError OKState");
			if(null != mGatt) {
				mGatt.disconnect();
				mGatt.close();
				mGatt = null;
			}
			transitionTo(mIdleState);
		}
		
		@Override
		protected void onFinalize() {
			LogUtil.d(TAG, "onFinalize OKState");
			super.onFinalize();
		}
		
		
	}
	
	
	/**
	 * Get the single object instance.
	 */
	public static BluetoothLeStateMachine getInstance(Context context) {
		if (sInstance == null) {
			synchronized (BluetoothLeStateMachine.class) {
				if (sInstance == null) {
					sInstance = new BluetoothLeStateMachine(context);
				}
			}
		}
		return sInstance;
	}
	
	/**
	 * Constructor using this object's own Looper for message handling.
	 * @param name
	 */
	private BluetoothLeStateMachine(Context context) {
		super(NAME);
		mContext = context;
		init();
	}

	/**
	 * Constructor using the provided Looper for message handling. 
	 */
	private BluetoothLeStateMachine(Looper looper, Context context) {
		super(NAME, looper);
		mContext = context;
		init();
	}
	
	private void initReceiver() {
		LogUtil.d(TAG, "initReceiver");
		mReceiver = new Receiver(mContext);
		mReceiver.registerAction(ACTION_BLE_SM_CONNECT_COMMAND);
		mReceiver.registerAction(ACTION_BLE_SM_ALARM);
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
		intentFilter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
		mContext.registerReceiver(mReceiver, intentFilter);
	}
	
	/**
	 * Initialize Bluetooth and States.
	 */
	private void init() {
		LogUtil.d(TAG, "BluetoothLeStateMachine initializing...");
		BluetoothManager bm = (BluetoothManager)mContext.getSystemService(Context.BLUETOOTH_SERVICE);
		mBtAdapter = bm.getAdapter();
		
		mHealthStackL0JNITransprot = HealthStackL0JNITransprot.getInstance(mContext, this);
		
		mIdleState = new IdleState();
		mScanState = new ScanState();
		mBindState = new BindState();
		mConnectState = new ConnectState();
		mConfigState = new ConfigState();
		mOKState = new OKState();
		
		addState(mIdleState);
		addState(mScanState);
		addState(mBindState);
		addState(mConnectState);
		addState(mConfigState);
		addState(mOKState);
		
		initReceiver();
		
		startAlarm();
		
		setInitialState(mIdleState);
		
		start();
	}
	
	public void finalize() {
		LogUtil.d(TAG, "BluetoothLeStateMachine finalize...");
		mBtAdapter = null;
		 
		
		sendMessage(MSG_FINALIZE);
		
		if(mReceiver != null){
			mLocalBroadcastManager.unregisterReceiver(mReceiver);
			mReceiver = null;
		}
	
		
		mHealthStackL0JNITransprot.finalize();
		
		mHealthStackL0JNITransprot = null;
		
		quit();

		sInstance = null;
		
		mHaveBeenFinalize = true;
	}
	

	private boolean isTargetDevice(BluetoothDevice device, int rssi) {
		return UART_DEVICE_NAME.equals(device.getName());
	}	
	
	private String convertToHexString(byte[] data) {
		StringBuilder sb = new StringBuilder();
		for (byte b : data) {
			sb.append(String.format("%1$02x", b));
		}
		return sb.toString();
	}
	
	
	@Override
	public int sendData(byte[] data) {
		LogUtil.d(TAG, "sendData");
		
		if(!mIsInOKState) {
			LogUtil.e(TAG, "sendData while not in OKState failed");
			LogUtil.e(TAG, "current state:" + getCurrentState().getName());
			return -1;
		}
		
		return mOKState.sendData(data);
	}
	
	public int getStateMachineState() {
		LogUtil.d(TAG, "getStateMachineState");
		
		if(getCurrentState() == mIdleState) {
			return BLE_SM_CONNECT_STATE_DISCONNECTED;
		} else if(getCurrentState() == mOKState) {
			return BLE_SM_CONNECT_STATE_CONNECTED;
		} else {
			return BLE_SM_CONNECT_STATE_CONNECTING;
		}
	}
	
	public int readBatterLevel(ReadBatteryLevelCallback callback) {
		LogUtil.d(TAG, "readBatterLevel");
		
		if(null != mReadBatteryLevelCallback) {
			LogUtil.d(TAG, "have one read  battery level in process");
			return -1;
			
		}
		
		if(getCurrentState() != mOKState) {
			LogUtil.e(TAG, "readBatterLevel while not in OKState failed");
			return -1;
		}
		
		if(0 == mOKState.readBatteryLevel()) {
			mReadBatteryLevelCallback = callback;
			return 0;
		} else {
			return -1;
		}
	}
	
	public void handleReadDeviceInformationError() {
		if(null != mReadManufactoryNameCallback) {
			ReadDeviceInformationCallback callback = mReadManufactoryNameCallback;
			mReadManufactoryNameCallback = null;
			callback.onFinish(-1,null);
		}
		
		if(null != mReadModelNumberCallback) {
			ReadDeviceInformationCallback callback = mReadModelNumberCallback;
			mReadModelNumberCallback = null;
			callback.onFinish(-1,null);
		}
		
		if(null != mReadHWRevisionCallback) {
			ReadDeviceInformationCallback callback = mReadHWRevisionCallback;
			mReadHWRevisionCallback = null;
			callback.onFinish(-1,null);
		}
		
		if(null != mReadSWRevisionCallback) {
			ReadDeviceInformationCallback callback = mReadSWRevisionCallback;
			mReadSWRevisionCallback = null;
			callback.onFinish(-1,null);
		}
		
		
	}
	
	public interface ReadBatteryLevelCallback {
		void onFinish(int status,int value);
	}
	
	
	private boolean haveReadDeviceInformationInProcess() {
		if(null != mReadManufactoryNameCallback || null != mReadModelNumberCallback 
				|| null != mReadHWRevisionCallback || null != mReadSWRevisionCallback) {
			
			return true;
		} else {
			
			return false;
		}
		
	}
	
	
	public int readManufactureName(ReadDeviceInformationCallback callback) {
		LogUtil.d(TAG, "readModelNumber");
		
		if(haveReadDeviceInformationInProcess()) {
			LogUtil.d(TAG, "have one read device information in process");
			return -1;
			
		}
		
		if(getCurrentState() != mOKState) {
			LogUtil.e(TAG, "readManufactureName while not in OKState failed");
			return -1;
		}
		
		if(0 == mOKState.readManufactureName()) {
			mReadManufactoryNameCallback = callback;
			return 0;
		} else {
			return -1;
		}
	}
	
	
	public int readModelNumber(ReadDeviceInformationCallback callback) {
		LogUtil.d(TAG, "readModelNumber");
		
		if(haveReadDeviceInformationInProcess()) {
			LogUtil.d(TAG, "have one read device information in process");
			return -1;
			
		}
		
		if(getCurrentState() != mOKState) {
			LogUtil.e(TAG, "readModelNumber while not in OKState failed");
			return -1;
		}
		
		if(0 == mOKState.readModelNumber()) {
			mReadModelNumberCallback = callback;
			return 0;
		} else {
			return -1;
		}
	}
	
	public int readHardwareRevision(ReadDeviceInformationCallback callback) {
		LogUtil.d(TAG, "readHardwareRevision");
		
		if(haveReadDeviceInformationInProcess()) {
			LogUtil.d(TAG, "have one read device information in process");
			return -1;
			
		}
		
		if(getCurrentState() != mOKState) {
			LogUtil.e(TAG, "readHardwareRevision while not in OKState failed");
			return -1;
		}
		
		if(0 == mOKState.readHardwareRevision()) {
			mReadHWRevisionCallback = callback;
			return 0;
		} else {
			return -1;
		}
	}
	
	public int readSoftwareRevision(ReadDeviceInformationCallback callback) {
		LogUtil.d(TAG, "readSoftwareRevision");
		
		if(haveReadDeviceInformationInProcess()) {
			LogUtil.d(TAG, "have one read device information in process");
			return -1;
			
		}
		
		if(getCurrentState() != mOKState) {
			LogUtil.e(TAG, "readSoftwareRevision while not in OKState failed");
			return -1;
		}
		
		if(0 == mOKState.readSoftwareRevision()) {
			mReadSWRevisionCallback = callback;
			return 0;
		} else {
			return -1;
		}
	}
	
	public interface ReadDeviceInformationCallback {
		void onFinish(int status,String value);
	}
	
	
	
	public void setBindOk(int connnectRetryCount,String deviceAddress) {
		LogUtil.d(TAG, "setBindOk");
		
		mScanRetryCount = BLE_SM_CONNECT_COMMAND_RETRY_COUNT_INVALID;
		 
		mConnectRetryCount = connnectRetryCount;
		mConnectDeviceAddress = deviceAddress;
		mConnectRetryTime = 2000;
		
	}
	
}
