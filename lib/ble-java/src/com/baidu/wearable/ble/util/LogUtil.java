package com.baidu.wearable.ble.util;

import java.io.File;
import java.io.PrintStream;
import java.util.Calendar;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import de.mindpipe.android.logging.log4j.LogConfigurator;


public class LogUtil {
	  final static String TAG = LogUtil.class.getSimpleName();
	  /** 日志总开关 **/
	  private static final boolean IS_DEBUGING = false;  
	  
	  // logcat level
	  private static int LOGCAT_LEVEL = (IS_DEBUGING) ? 2 : 32;
	  
	  // log file level,must >=LOGCAT_LEVEL
	  private static int FILE_LOG_LEVEL = (IS_DEBUGING) ? 2 : 32;
	  
	  final static int LOG_LEVEL_ERROR = 16;
	  final static int LOG_LEVEL_WARN = 8;
	  final static int LOG_LEVEL_INFO = 4;
	  final static int LOG_LEVEL_DEBUG = 2;

	  final static int LOG_LEVEL_VERBOSE = 0;
	  private static boolean VERBOSE = (LOGCAT_LEVEL <= LOG_LEVEL_VERBOSE);
	  private static boolean DEBUG = (LOGCAT_LEVEL <= LOG_LEVEL_DEBUG);
	  private static boolean INFO = (LOGCAT_LEVEL <= LOG_LEVEL_INFO);
	  private static boolean WARN = (LOGCAT_LEVEL <= LOG_LEVEL_WARN);
	  private static boolean ERROR = (LOGCAT_LEVEL <= LOG_LEVEL_ERROR);

	  /** 统一TAG **/
	  private final static String LOG_TAG_STRING = "WearableLog";
	  /** 当前日志日志文件名 **/
	  private final static String LOG_FILE_NAME = ".Wearable.log";
	  /** 日志文件大小阀值，只在commit方法调用时构建文件 **/
	  private final static long LOG_SIZE = 5 * 1024 * 1024;
	  /** 日志格式，形如：[2010-01-22 13:39:1][D][com.a.c]error occured **/
	  private final static String LOG_ENTRY_FORMAT = "[%tF %tT][%s][%s]%s";

	  static PrintStream logStream;

	  static boolean initialized = false;

	  public static boolean isDebug() {
	    return IS_DEBUGING;
	  }

	  /**
	   * 不写文件的debug日志
	   * 
	   * @param logFile
	   */
	  private static void d(File logFile) {
	    if (DEBUG) {
	      Log.d(LOG_TAG_STRING, TAG + " : Log to file : " + logFile);
	    }
	  }

	  public static void d(String tag, String msg) {
	    if (DEBUG) {
	      tag = Thread.currentThread().getName() + ":" + tag;
	      Log.d(LOG_TAG_STRING, tag + " : " + msg);
	      if (FILE_LOG_LEVEL <= LOG_LEVEL_DEBUG)
	        write("D", tag, msg, null);
	    }
	  }

	  public static void d(String tag, String msg, Throwable error) {
	    if (DEBUG) {
	      tag = Thread.currentThread().getName() + ":" + tag;
	      Log.d(LOG_TAG_STRING, tag + " : " + msg, error);
	      if (FILE_LOG_LEVEL <= LOG_LEVEL_DEBUG)
	        write("D", tag, msg, error);
	    }
	  }

	  /**
	   * 不写文件的verbose日志
	   * 
	   * @param backfile
	   */
	  private static void v(File backfile) {
	    if (VERBOSE) {
	      Log.v(LOG_TAG_STRING, TAG + " : Create back log file : " + backfile.getName());
	    }
	  }

	  public static void v(String tag, String msg) {
	    if (DEBUG) {
	      tag = Thread.currentThread().getName() + ":" + tag;
	      Log.v(LOG_TAG_STRING, tag + " : " + msg);
	      if (FILE_LOG_LEVEL <= LOG_LEVEL_DEBUG)
	        write("V", tag, msg, null);
	    }
	  }

	  public static void v(String tag, String msg, Throwable error) {
	    if (DEBUG) {
	      tag = Thread.currentThread().getName() + ":" + tag;
	      Log.v(LOG_TAG_STRING, tag + " : " + msg, error);
	      if (FILE_LOG_LEVEL <= LOG_LEVEL_DEBUG)
	        write("V", tag, msg, error);
	    }
	  }

	  public static void i(String tag, String msg) {
	    if (INFO) {
	      tag = Thread.currentThread().getName() + ":" + tag;
	      Log.i(LOG_TAG_STRING, tag + " : " + msg);
	      if (FILE_LOG_LEVEL <= LOG_LEVEL_INFO)
	        write("I", tag, msg, null);
	    }
	  }

	  public static void i(String tag, String msg, Throwable error) {
	    if (INFO) {
	      tag = Thread.currentThread().getName() + ":" + tag;
	      Log.i(LOG_TAG_STRING, tag + " : " + msg, error);
	      if (FILE_LOG_LEVEL <= LOG_LEVEL_INFO)
	        write("I", tag, msg, error);
	    }
	  }

	  /**
	   * 不写文件的w
	   */
	  private static void w() {
	    if (WARN) {
	      Log.w(LOG_TAG_STRING, "Unable to create external cache directory");
	    }
	  }

	  public static void w(String tag, String msg) {
	    if (WARN) {
	      tag = Thread.currentThread().getName() + ":" + tag;
	      Log.w(LOG_TAG_STRING, tag + " : " + msg);
	      if (FILE_LOG_LEVEL <= LOG_LEVEL_WARN)
	        write("W", tag, msg, null);
	    }
	  }

	  public static void w(String tag, String msg, Throwable error) {
	    if (WARN) {
	      tag = Thread.currentThread().getName() + ":" + tag;
	      Log.w(LOG_TAG_STRING, tag + " : " + msg, error);
	      if (FILE_LOG_LEVEL <= LOG_LEVEL_WARN)
	        write("W", tag, msg, error);
	    }
	  }

	  /**
	   * 不写文件的错误日志
	   * 
	   * @param e
	   */
	  private static void e(String msg, Exception e) {
	    if (ERROR) {
	      Log.e(LOG_TAG_STRING, msg, e);
	    }
	  }

	  public static void e(String tag, String msg) {
	    if (ERROR) {
	      tag = Thread.currentThread().getName() + ":" + tag;
	      Log.e(LOG_TAG_STRING, tag + " : " + msg);
	      if (FILE_LOG_LEVEL <= LOG_LEVEL_ERROR)
	        write("E", tag, msg, null);
	    }
	  }

	  public static void e(String tag, String msg, Throwable error) {
	    if (ERROR) {
	      tag = Thread.currentThread().getName() + ":" + tag;
	      Log.e(LOG_TAG_STRING, tag + " : " + msg, error);
	      if (FILE_LOG_LEVEL <= LOG_LEVEL_ERROR)
	        write("E", tag, msg, error);
	    }
	  }

	  public static void wtf(String tag, String msg) {
	    if (ERROR) {
	      tag = Thread.currentThread().getName() + ":" + tag;
	      Log.wtf(LOG_TAG_STRING, tag + " : " + msg);
	      if (FILE_LOG_LEVEL <= LOG_LEVEL_ERROR)
	        write("E", tag, msg, null);
	    }
	  }

	  public static void wtf(String tag, String msg, Throwable error) {
	    if (ERROR) {
	      tag = Thread.currentThread().getName() + ":" + tag;
	      Log.wtf(LOG_TAG_STRING, tag + " : " + msg, error);
	      if (FILE_LOG_LEVEL <= LOG_LEVEL_ERROR)
	        write("E", tag, msg, error);
	    }
	  }

	  private static void write(String level, String tag, String msg, Throwable error) {
		  //TODO 存SD卡
//	    if (!initialized)
//	      init();
//	    if (logStream == null || logStream.checkError()) {
//	      initialized = false;
//	      return;
//	    }
//	    Date now = new Date();
//
//	    logStream.printf(LOG_ENTRY_FORMAT, now, now, level, tag, " : " + msg);
//	    logStream.println();
//
//	    if (error != null) {
//	      error.printStackTrace(logStream);
//	      logStream.println();
//	    }

	  }

	public static Logger mInstance;
	
	public static Intent mIntent;
	
	public static File mFile;
	
	public static void debug(String str) {
		if (DEBUG) {			
			Logger.getLogger("Wearable").debug(str);
		}
	}
	
	public static void init() {
		if (DEBUG) {
			Calendar calendar = Calendar.getInstance();
			int year = calendar.get(Calendar.YEAR);
			int month = calendar.get(Calendar.MONTH) + 1;
			int day = calendar.get(Calendar.DAY_OF_MONTH);
			int hour = calendar.get(Calendar.HOUR_OF_DAY);
			int minute = calendar.get(Calendar.MINUTE);
			int second = calendar.get(Calendar.SECOND);

			String fileName = year + "-" + month + "-" + day + "-" + hour + "-"
					+ minute + "-" + second;
			String filePathAndName = Environment
					.getExternalStorageDirectory()
					+ File.separator
					+ "Dulife"
					+ File.separator + "logs" + File.separator + fileName;

			LogConfigurator logConfigurator = new LogConfigurator();
			logConfigurator.setFileName(filePathAndName);
			logConfigurator.setRootLevel(Level.DEBUG);
			logConfigurator.setLevel("org.apache", Level.ERROR);
			logConfigurator.setFilePattern("%d %-5p [%c{2}]-[%L] %m%n");
			logConfigurator.setMaxFileSize(1024 * 1024 * 20);
			logConfigurator.setImmediateFlush(true);
			logConfigurator.configure();
		}
	}
			
	private static Intent getSendToBaiduYunIntent(String filePathAndName) {
		Intent intent = new Intent(Intent.ACTION_SEND);
		intent.setType("text/plain");
		mFile = new File(Environment
				.getExternalStorageDirectory()
				+ File.separator
				+ "Dulife"
				+ File.separator + "logs" + File.separator + filePathAndName);
		intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(mFile));
		intent.setPackage("com.baidu.netdisk");
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		
		return intent;
	}
	
	public static void sendBaiduYun(Context context) {		
		if (DEBUG) {
			if (null != mIntent && null != mFile && mFile.length() > 10) {
				context.startActivity(mIntent);
			}
		}
	}
}
