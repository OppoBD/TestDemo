package com.baidu.wearable.ble.stack;

import android.content.Context;

import com.baidu.wearable.ble.util.LogUtil;

public class HealthStackL0JNITransprot {

	private final String TAG = "HealthStackL0JNITransprot";
	private static HealthStackL0JNITransprot instance;

	private Context mContext;
	
	private IBlueToothSend mSender;
 
	private HealthStackL0JNITransprot(Context context,IBlueToothSend send) {

		mContext = context.getApplicationContext();
		this.mSender = send;
	}

	public static HealthStackL0JNITransprot getInstance(Context context,IBlueToothSend send) {
		if (instance == null) {
			synchronized (HealthStackL0JNITransprot.class) {
				if (instance == null) {
					instance = new HealthStackL0JNITransprot(context,send);
					instance.init();
				}
			}
		}
		return instance;
	}

	public native static void classInitNative();

	static {
		classInitNative();
	}
	
	public void finalize() {
		instance = null;
	}
	public native int sendReadResult(byte[] content, char length);

	public native int sendWriteResult( int statusCode);

	public native int init();

	public int sendData(long length, byte[] content) {
		 
		LogUtil.v(TAG, "sendData");
		 
		 return this.mSender.sendData(content);
	}
}
