package com.baidu.wearable.ble.connectmanager;

import com.baidu.wearable.ble.util.LogUtil;

public class BluetoothState {
	 
	public static final String  TAG = "BluetoothState";
	
	
	public static final String ACTION_BLE_NEED_BIND = "action.wearable.ble.need.bind";
	public static final String EXTRA_BLE_NEED_BIND_DEVICE_ADDRESS = "extra.wearable.ble.need.bind.device.address";
	public static final String EXTRA_BLE_NEED_BIND_DEVICE_NAME = "extra.wearable.ble.need.bind.device.name";
	
	public static final String ACTION_BLE_BIND_RESULT = "action.wearable.ble.bind.result";
	public static final String EXTRA_BLE_BIND_RESULT = "extra.wearable.ble.bind.result";
	public static final int BLE_BIND_INVALID = -1;
	public static final int BLE_BIND_SUCCESS = 0;
	public static final int BLE_BIND_ERROR =  BLE_BIND_SUCCESS + 1;
	
	public static final String ACTION_BLE_LOGIN_RESULT = "action.wearable.ble.login.result";
	public static final String EXTRA_BLE_LOGIN_RESULT = "extra.wearable.ble.bind.result";
	public static final int BLE_LOGIN_SUCCESS = 0;
	public static final int BLE_LOGIN_ERROR =  BLE_LOGIN_SUCCESS + 1;
	
	
	public static final String ACTION_BLE_CONNECT_COMMAND = "action.wearable.ble.connect.command";
	public static final String EXTRA_BLE_CONNECT_COMMAND = "extra.wearable.ble.connect.command";
	public static final String EXTRA_BLE_CONNECT_COMMAND_DEVICE_ADDRESS = "extra.wearable.ble.connect.command.device.address";
	public static final String EXTRA_BLE_CONNECT_COMMAND_RETRY_COUNT = "extra.wearable.ble.connect.command.retry.count";
	public static final int BLE_CONNECT_COMMAND_INVALID = -1; //this is invalid value
	public static final int BLE_CONNECT_COMMAND_START_SCAN = 0; //start new scan of device
	public static final int BLE_CONNECT_COMMAND_FINALIZE = BLE_CONNECT_COMMAND_START_SCAN + 1; //finalize the state machine, this command will reset the state machine into idle state
	public static final int BLE_CONNECT_COMMAND_START_CONNECT = BLE_CONNECT_COMMAND_FINALIZE + 1; //start new connect the specific device, which address is EXTRA_BLE_SM_CONNECT_COMMAND_DEVICE_ADDRESS
	public static final int BLE_CONNECT_COMMAND_START_BIND = BLE_CONNECT_COMMAND_START_CONNECT + 1;
	
	
	
	public static final String ACTION_BLE_CONNECT_STATUS = "action.wearable.ble.connect.status";  //所有状态变化收到的
	public static final String EXTRA_BLE_CONNECT_STATUS = "extra.wearable.ble.connect.status";   //检查该状态是否为CONNECT_STATE_CONNECTED
	public static final String EXTRA_BLE_CONNECT_STATUS_DEVICE_ADDRESS = "extra.wearable.ble.connect.status.device.address";
	public static final int CONNECT_STATE_INVALID = -1; //invalid state
	public static final int CONNECT_STATE_DISCONNECTED = 0; 
	public static final int CONNECT_STATE_WAIT_RETRY = CONNECT_STATE_DISCONNECTED + 1; 
	public static final int CONNECT_STATE_SCANNING = CONNECT_STATE_WAIT_RETRY + 1; 
	public static final int CONNECT_STATE_CONNECTING = CONNECT_STATE_SCANNING + 1; //in connect device process
	public static final int CONNECT_STATE_CONNECTING_NEED_BIND = CONNECT_STATE_CONNECTING + 1; 
	public static final int CONNECT_STATE_CONNECTED = CONNECT_STATE_CONNECTING_NEED_BIND + 1; //success connect the device, in bluetooth level, not stack level

	
	public static final String ACTION_BLE_SCAN_RESULT = "action.wearable.ble.statemachine.scan.state";
	public static final String EXTRA_BLE_SCAN_RESULT = "extra.wearable.ble.statemachine.scan.state";
	public static final String EXTRA_BLE_SCAN_RESULT_DEVICE_ADDRESS = "extra.wearable.ble.statemachine.scan.state.device.address";
	public static final String EXTRA_BLE_SCAN_RESULT_DEVICE_NAME = "extra.wearable.ble.statemachine.scan.state.device.name";
	public static final int BLE_SCAN_RESULT_INVALID = -1; 
	public static final int BLE_SCAN_RESULT_SUCCESS = 0; 
	public static final int BLE_SCAN_RESULT_ERROR = BLE_SCAN_RESULT_SUCCESS + 1; 
	
	
	public static final String ACTION_BLE_DEVICE_INFORMATION = "action.wearable.ble.device.information";
	public static final String EXTRA_BLE_DEVICE_INFORMATION_MANUFACTURE_NAME = "extra.wearable.ble.device.information.manufacture.name";
	public static final String EXTRA_BLE_DEVICE_INFORMATION_MODEL_NUMBER = "extra.wearable.ble.device.information.model.number";
	public static final String EXTRA_BLE_DEVICE_INFORMATION_HARDWARE_REVISION = "extra.wearable.ble.device.information.hardware.revision";
	public static final String EXTRA_BLE_DEVICE_INFORMATION_SOFTWARE_REVISION = "extra.wearable.ble.device.information.software.revision";
	
	
	public static final String ACTION_BLE_BATTERY_LEVEL = "action.wearable.ble.battery.level";
	public static final String EXTRA_BLE_BATTERY_LEVEL_VALUE = "extra.wearable.ble.battery.level";
	
	public static final String ACTION_BLE_LOW_BATTERY = "action.wearable.ble.battery.lowbattery";
	public static final String EXTRA_BLE_LOW_BATTERY_LEVEL = "extra.wearable.ble.battery.lowbattery_level";
	
	public static final long BATTER_LEVEL_UPDATE_INTERVAL = 5 * 60 * 1000;
	
	public static final long BATTER_LEVEL_UPDATE_RETRY = 10 * 1000;
	
	public static final long DEVICE_INFORMATION_UPDATE_INTERVAL = 5 * 1000;
	
	public static final int BATTERY_LEVEL_UNKNOWN = -1;
	 
	private static BluetoothState sInstance;
	
	private static String mDeviceAddress = null;
	
	private int mCurrentState = CONNECT_STATE_INVALID;
	
	private int mBatteryLevel = BATTERY_LEVEL_UNKNOWN;
	
	private String mManufactureName = null;  
	private String mModelNumber = null;   
	private String mHardwareRevision = null;   
	private String mSoftwareRevision = null;   
	
	
	public int getBleState() {
		LogUtil.d(TAG, "getBleState");
		
		return mCurrentState;
		
	}
	
	public void setBleState(int state) {
		LogUtil.d(TAG, "getBleState");
		
		mCurrentState = state;
		
	}
	
	public String getDeviceAddress() {
		LogUtil.d(TAG, "getDeviceAddress");
		
		return mDeviceAddress;
		
	}
	
	public void setDeviceAddress(String deviceAddress) {
		LogUtil.d(TAG, "setDeviceAddress");
		
		mDeviceAddress = deviceAddress;
		
	}
	
	public int getBatteryLevel() {
		LogUtil.d(TAG, "getBatteryLevel");
		
		return mBatteryLevel;
		
	}
	
	public void setBatteryLevel(int level) {
		LogUtil.d(TAG, "setBatteryLevel");
		
		mBatteryLevel = level;
		
	}
	
	public String getManufactureName() {
		LogUtil.d(TAG, "getManufactureName");
		
		return mManufactureName;
		
	}
	
	public void setManufactureName(String manufactureName) {
		LogUtil.d(TAG, "setManufactureName");
		
		mManufactureName = manufactureName;	
	}
	
	public String getModelNumber() {
		LogUtil.d(TAG, "getModelNumber");
		
		return mModelNumber;
		
	}
	
	public void setModelNumber(String modelNumber) {
		LogUtil.d(TAG, "getModelNumber");
		
		mModelNumber = modelNumber;
		
	}
	
	public String getHardwareRevision() {
		LogUtil.d(TAG, "getHardwareRevision");
		
		return mHardwareRevision;
		
	}
	
	public void setHardwareRevision(String hardwareRevision) {
		LogUtil.d(TAG, "setHardwareRevision");
		
		mHardwareRevision = hardwareRevision;
		
	}
	
	public String getSoftwareRevision() {
		LogUtil.d(TAG, "getSoftwareRevision");
		
		return mSoftwareRevision;
		
	}
	
	public void setSoftwareRevision(String softwareRevision) {
		LogUtil.d(TAG, "mSoftwareRevision");
		
		mSoftwareRevision = softwareRevision;
		
	}
	
	
	public static BluetoothState getInstance() {
		if (sInstance == null) {
			synchronized (BluetoothState.class) {
				if (sInstance == null) {
					sInstance = new BluetoothState();
				}
			}
		}
		return sInstance;
	}

}
