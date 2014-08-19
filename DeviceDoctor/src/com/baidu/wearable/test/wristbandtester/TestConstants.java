package com.baidu.wearable.test.wristbandtester;


public class TestConstants {
	/*
	 * Package Name.
	 */
	public static final String PKG = TestConstants.class.getPackage().getName();
	
	/*
	 * UUIDs
	 */
	public static final String UART_UUID_STR = "6e400001-b5a3-f393-e0a9-e50e24dcca9e";
	public static final String UART_WRITE_UUID_STR = "6e400002-b5a3-f393-e0a9-e50e24dcca9e";
	public static final String UART_READ_UUID_STR = "6e400003-b5a3-f393-e0a9-e50e24dcca9e";
	public static final String CLIENT_CHARACTERISTIC_CONFIG = "00002902-0000-1000-8000-00805f9b34fb";
	public static final String DIS_UUID_STR = "0000180a-0000-1000-8000-00805f9b34fb";
	public static final String DIS_FWRV_UUID_STR = "00002a26-0000-1000-8000-00805f9b34fb";
	
	// The Battery UUIDs.
	public static final String BATTERY_UUID_STR = "0000180f-0000-1000-8000-00805f9b34fb";
	public static final String BATTERY_RD_UUID_STR = "00002a19-0000-1000-8000-00805f9b34fb";
	
	public static final String DFU_NAME = "DfuTrag";
	
	/*
	 * File Paths.
	 */
	public static final String FOLDER = "production_test_report";
	
	public static final int VENDOR_CODE_T1000 = 1;
	public static final int VENDOR_CODE_O1000 = 2;
	
}
