package com.baidu.wearable.test.wristbandtester;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.baidu.wearable.ble.model.ClockList;
import com.baidu.wearable.ble.stack.BlueTooth;
import com.baidu.wearable.ble.stack.BlueTooth.BlueToothCommonListener;
import com.baidu.wearable.ble.stack.HealthStackL0JNITransprot;
import com.baidu.wearable.ble.stack.IBlueToothSend;
import com.baidu.wearable.test.wristbandtester.TestActivity.TestStatusHandler;

public abstract class ProtocolHelper implements IBlueToothSend, BlueTooth.BlueToothTestModeReceiverListener {

	private final String TAG = ProtocolHelper.class.getSimpleName();
	
	private HealthStackL0JNITransprot mTransport;
	private BlueTooth mBt;
	
	private Handler mEchoReceivedHandler;
	private Handler mSensorReceivedHandler;
	private Handler mFlagReceivedHandler;
	private Handler mSnReceivedHandler;
	private Handler mVoltageReceivedHandler;
	private Handler mButtonEventReceivedHandler;
	
	ProtocolHelper(Context context){
		mBt = BlueTooth.getInstance();
		initStack(context);
	}

	public void finalizeStack(){
		if(mBt != null){
			mBt.finalizeBleStackNative();
		}
	}
	
	public void initStack(Context context){
		finalizeStack();
		if(mBt != null){
			mBt.registerTestModeResponseReceiverListener(this);
			mBt.initBleStackNative();
		}
		mTransport = HealthStackL0JNITransprot.getInstance(context, this);
	}
	
	public void enterTestMode(BlueToothCommonListener listener){
		mBt.testEnterTestModeRequest(listener);
	}

	public void requestEcho(byte[] data, BlueToothCommonListener listener) {
		mBt.testEchoRequest(data, listener);
		
	}
	
	public void testLed(BlueToothCommonListener listener){
		mBt.testLedOnRequest(listener);
	}

	public void testVibrator(BlueToothCommonListener listener) {
		mBt.testVibrateRequest(listener);
	}
	
	public void testWriteSn(byte[] sn, BlueToothCommonListener listener) {
		mBt.testWriteSnRequest(sn, listener);
	}

	public void testReadSn(BlueToothCommonListener listener){
		mBt.testReadSnRequest(listener);
	}

	public void testCharge(BlueToothCommonListener listener) {
		mBt.testChargeRequest(listener);
	}

	public void testWriteFlag(BlueToothCommonListener listener) {
		mBt.testWriteFlagRequest((byte)0x00, listener);
	}
	
	public void testReadFlag(BlueToothCommonListener listener) {
		mBt.testReadFlagRequest(listener);
	}
	
	public void testSensor(BlueToothCommonListener listener) {
		mBt.testReadSensorRequest(listener);
	}

	public void exitTest(BlueToothCommonListener listener){
		mBt.testExitTestModeRequest(listener);
	}

	public void enterOTA(BlueToothCommonListener listener) {
		mBt.otaEnterOTAMode(listener);
	}
	
	/**
	 * 闹钟处理
	 */
	public void testAlarm(ClockList alarmList,BlueToothCommonListener listener) {
		mBt.setAlarmList(alarmList, listener);
	}
	
	/**
	 * bind
	 */
	public void testBind(String userId, BlueToothCommonListener listener) {
		mBt.bind(userId, listener);
	}
	
	@Override
	public void onTestEchoResponse(byte[] data) {
		Log.d(TAG,"onTestEchoResponse() data = " + BluetoothService.byteToHexString(data) );
		Message msg = mEchoReceivedHandler.obtainMessage(TestStatusHandler.TEST_STATUS_ECHO_RECEIVED);
		Bundle bundle = new Bundle();
		bundle.putByteArray(TestStatusHandler.KEY_ECHO, data);
		
		msg.setData(bundle);
		mEchoReceivedHandler.sendMessage(msg);
	}

	@Override
	public void onTestSnReadResponse(byte[] sn) {
		Log.d(TAG,"onTestSnReadResponse() data = " + BluetoothService.byteToHexString(sn) );
		Message msg = mSnReceivedHandler.obtainMessage(TestStatusHandler.TEST_STATUS_SN_RECEIVED);
		Bundle bundle = new Bundle();
		bundle.putByteArray(TestStatusHandler.KEY_SN, sn);
		
		msg.setData(bundle);
		mSnReceivedHandler.sendMessage(msg);
	}
	
	@Override
	public void onTestChargeReadResponse(short voltage) {
		Log.d(TAG,"onTestChargeReadResponse() " + voltage);
		Message msg = mVoltageReceivedHandler.obtainMessage(TestStatusHandler.TEST_SUCCESS);
		msg.arg1 = voltage;
		mVoltageReceivedHandler.sendMessage(msg);
	}
	
	@Override
	public void onTestFlagReadResponse(byte flag) {
		Message msg = mFlagReceivedHandler.obtainMessage(TestStatusHandler.TEST_STATUS_FLAG_RECEIVED);
		msg.arg1 = flag;
		mFlagReceivedHandler.sendMessage(msg);		
	}
	
	@Override
	public void onTestSensorReadResponse(short x_axis, short y_axis,
			short z_axis) {
		Log.d(TAG,"onTestSensorReadResponse() x = " + x_axis + ", y = " + y_axis + ", z = " + z_axis);
		Message msg = mSensorReceivedHandler.obtainMessage(TestStatusHandler.TEST_STATUS_SENSOR_RECEIVED);
		Bundle bundle = new Bundle();
		bundle.putShortArray(TestStatusHandler.KEY_SENSOR, new short[]{x_axis,y_axis,z_axis});
		msg.setData(bundle);
		mSensorReceivedHandler.sendMessage(msg);	
	}

	/* (non-Javadoc)
	 * @see com.baidu.wearable.ble.stack.BlueTooth.BlueToothTestModeReceiverListener#onTestButton(int, int, long)
	 */
	@Override
	public void onTestButton(int code, int button_id, long timestamp) {
		Log.d(TAG,"onTestButton() code = " + code + ", id = " + button_id + ", timestamp = " + timestamp);
		Message msg = mButtonEventReceivedHandler.obtainMessage(TestStatusHandler.TEST_STATUS_BUTTON_EVENT_RECEIVED, button_id, code);
		mButtonEventReceivedHandler.sendMessage(msg);
	}

	public void onDataReceived(byte[] data) {
		mTransport.sendReadResult(data, (char)data.length);
	}

	abstract public int sendData( byte[] contents);

	public void sendWriteResult(int statusCode) {
		mTransport.sendWriteResult(statusCode);
		
	}

	public void registerEchoRequestHandler(TestStatusHandler handler) {
		mEchoReceivedHandler = handler; 
	}

	public void registerSnRequestHandler(TestStatusHandler handler) {
		mSnReceivedHandler = handler;
	}
	
	public void registerVoltageRequestHandler(TestStatusHandler handler) {
		mVoltageReceivedHandler = handler;
	}
	
	public void registerFlagRequestHandler(TestStatusHandler handler) {
		mFlagReceivedHandler = handler;
	}

	public void registerSensorRequestHandler(TestStatusHandler handler) {
		mSensorReceivedHandler = handler;
	}

	public void registerButtonEventReceivedHandler(TestStatusHandler handler) {
		mButtonEventReceivedHandler = handler;
	}
	
	public void clear() {
		mBt.finalizeBleStackNative();	
	}





}
