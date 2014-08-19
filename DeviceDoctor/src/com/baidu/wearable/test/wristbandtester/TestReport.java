/**
 * 
 */
package com.baidu.wearable.test.wristbandtester;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Calendar;
import java.util.Locale;

import android.os.Environment;
import android.util.Log;

/**
 * @author chenxixiong
 *
 */
public abstract class TestReport {
	private final static String TAG = TestReport.class.getSimpleName();

	public abstract void printReport(TestManager testMgr) throws FileNotFoundException;

	protected static String getTimeString() {
		Calendar c = Calendar.getInstance();
		int year = c.get(Calendar.YEAR);
		int month = c.get(Calendar.MONTH) + 1;
		int day = c.get(Calendar.DAY_OF_MONTH);
		int hour = c.get(Calendar.HOUR_OF_DAY);
		int min = c.get(Calendar.MINUTE);
		int sec = c.get(Calendar.SECOND);
		String timeStr = String.format(Locale.US, "%1$04d%2$02d%3$02d(%4$02d%5$02d%6$02d)",
				year, month, day, hour, min, sec);
		return timeStr;
	}
	
	protected static File getResultFile(String name) throws FileNotFoundException {
		File root = null;
		// Check if SDCARD is available.
		if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
			root = Environment.getExternalStorageDirectory();
		} else {
			throw new FileNotFoundException();
		}
		
		File dir = new File(root, TestConstants.FOLDER);
		if (!dir.exists()) {
			dir.mkdirs();
		}
		// TOOD: check folder and writable
		
		File file = new File(dir, name);
		Log.d(TAG, "Result file path: " + file.getAbsolutePath());
		return file;
	}
	
	protected static String formatTimeString(long timeMills) {
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(timeMills);
		int y = c.get(Calendar.YEAR);
		int m = c.get(Calendar.MONTH) + 1;
		int d = c.get(Calendar.DAY_OF_MONTH);
		int h = c.get(Calendar.HOUR_OF_DAY);
		int min = c.get(Calendar.MINUTE);
		int s = c.get(Calendar.SECOND);
		return String.format(Locale.US, "%1$d-%2$02d-%3$02d %4$02d:%5$02d:%6$02d",
				y, m, d, h, min, s);
	}
	
}
