package com.baidu.http;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import com.baidu.lib.wifi.NetPing;
import com.baidu.lib.wifi.WifiCompute;
import com.baidu.wifi.demo.Utils;
import com.baidu.wifi.demo.WifiActivity;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ScrollView;
import android.widget.TextView;

/**
 * 网络类基类
 * 
 * @author
 * 
 */
public class Transport {

	private final static String TAG =  Transport.class.getSimpleName();

	
	protected final static String METHOD_KEY = "method";

	protected final static String METHOD_INSERT = "insert";

	protected final static String METHOD_SELECT = "select";

	protected final static String METHOD_GET = "get";

	protected final static String METHOD_UPDATE = "update";

	protected final static String METHOD_UPDATEX = "updatex";

	protected final static String METHOD_REGISTER = "register";

	protected final static String METHOD_UNREGISTER = "unregister";

	protected final static String METHOD_LIST = "list";

	protected final static String METHOD_SUMMARIZE = "summarize";

	protected final static String METHOD_SET = "set";

	protected final static String FROM_DATE_KEY = "from_date";

	protected final static String DAYS_KEY = "days";

	protected final static String APP_ID = "app_id";

	protected final static String AGENT_ID = "agent_id";

	protected final static String MTIME = "_mtime";

	protected final static String IS_INPUT_LATEST_KEY = "is_input_latest";

	// public final static String APP_ID_VALUE = "1325340";//"1079155";

	public final static int PULL_DATA_FROM_NET_DAYS = 32;

	public final static String KEY_ERROR_CODE = "error_code";

	public final static String KEY_ERROR_MSG = "error_msg";

	public final static String KEY_ERROR_EXT_DATA = "ext_data";

	public final static int SUCCESS = 0;

	public final static int ERROR_DEFAULT = -1;

	public final static String ERROR_TRING_DEFAULT = "ERROR";
	// private static Transport mInstance;

	protected static String mUserId;

	public final static String[] connect_url_arr = {"http://www.baidu.com","http://www.sina.com.cn","http://www.qq.com"};

//	public final static String[] connect_url_arr = {"http://www.baidu.com"};

	// protected static String mBduss;

	public interface CommonListener {

		void onSuccess();

		void onFailure(int errCode, String errMsg);

	}

	public static class CommonResult {

		public int errCode = ERROR_DEFAULT;

		public String errMsg = null;

		public Object extra = null;

	}
	
	public static class UrlCrawResult {
		
		public String url;

		public int speed;

		public int result;
		
		public int htmlsize;

	}


	interface Listener {
		void onSuccess(JSONObject json);

		void onFailure(int errCode, String errMsg);
	}

	protected static JSONObject sendPostRequest(Context context,String url,JSONObject params)
			throws IOException, JSONException {

	//	if (null != params) {
//
			HttpPost req = new HttpPost(url);
//			
//			JSONObject params = new JSONObject();
//			params.put(Utils.UID, Utils.getString(context, Utils.getString(context, Utils.UID)));
//			params.put("interface", Utils.PING_PACKAGE_LOSS_RATE);
//			params.put("result", Utils.getString(context, Utils.PING_PACKAGE_LOSS_RATE));
//			params.put("type", 0);
			Log.d(TAG, params.toString());
			List<NameValuePair> pairs = new ArrayList<NameValuePair>();
			pairs.add(new BasicNameValuePair("param", params.toString()));
			req.setEntity(new UrlEncodedFormEntity(pairs));

			HttpResponse response = HttpRequest.executeHttpRequest(req);
			
			if (response != null && null != response.getStatusLine()) {

			}

			if (null != response) {
				HttpEntity respBody = response.getEntity();
				String respMsg = EntityUtils.toString(respBody);
				// LogUtil.d(TAG, "response body: " + respMsg + ", body length:"
				// + respMsg.length() + ", status code:"
				// + response.getStatusLine().getStatusCode());
				return new JSONObject(respMsg);
			}
	//	}

		return null;
	}

	
	/**
	 * 获取外网访问连通性和外网结果
	 * @param context
	 */
	public static void connectNet(  Context context) {

		AsyncTask<Object, Void, List<UrlCrawResult>> task = new AsyncTask<Object,Void, List<UrlCrawResult>>() {

			String mErrMsg;

			@Override
			protected List<UrlCrawResult> doInBackground(Object ... params) {
				try {
					Log.d(TAG, "connectNet doInBackground");
					List<UrlCrawResult> result = connect2Net((Context) params[0]);
					for(UrlCrawResult res:result){
						Utils.saveInteger((Context) params[0],res.url+"_result",res.result);
						Utils.saveInteger((Context) params[0], res.url+"_speed", res.speed);
						Utils.saveInteger ((Context) params[0],res.url+"_htmlsize",res.htmlsize);
					//	Log.d(TAG, "htmlsize is :"+res.htmlsize);
					}
					return result;
					
				} catch (IOException e) {
					e.printStackTrace();
					mErrMsg = e.getMessage();
				}
				return null;
			}
			
			@Override
			protected void onPostExecute(List<UrlCrawResult> result) {
				super.onPostExecute(result);
				
				
				}
		};

		task.execute(context);

	}
	
	
	public static void upload_network( Context context, String url, JSONObject param) {

		AsyncTask<Object, Void, JSONObject> task = new AsyncTask<Object, Void, JSONObject>() {

			String mErrMsg;

			@Override
			protected JSONObject doInBackground(Object... params) {
				try {
					Log.d(TAG, "upload_network doInBackground");
					return sendPostRequest((Context) params[0],params[1].toString(),(JSONObject) params[2]);
				
				} catch (IOException e) {
					e.printStackTrace();
					mErrMsg = e.getMessage();
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return null;
			}
			
			@Override
			protected void onPostExecute(JSONObject result) {
				super.onPostExecute(result);
				Log.d(TAG, result.toString());
				
				}

		};

		task.execute(context,url,param);

	}

	public static void getLoginedUid(String url) {

		AsyncTask<String, Void, JSONObject> task = new AsyncTask<String, Void, JSONObject>() {

			String mErrMsg;

			@Override
			protected JSONObject doInBackground(String... params) {
				try {
					return sendGetRequest(params[0]);
				} catch (IOException e) {
					e.printStackTrace();
					mErrMsg = e.getMessage();
				} catch (JSONException e) {
					e.printStackTrace();
					mErrMsg = e.getMessage();
				}
				return null;
			}

			@Override
			protected void onPostExecute(JSONObject result) {
				super.onPostExecute(result);

				Log.d(TAG, "mErrMsg:" + mErrMsg);
				String uid = "";
				String uname = "";
				try {
					uid = (String) result.getString("uid");
					uname = (String) result.getString("uname");
					Utils.UID = uid;
					Utils.UNAME = uname;
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				Log.d(TAG, uid + "---" + uname);

			}

		};

		task.execute(url);

	}

	public static JSONObject sendGetRequest(String url) throws IOException,
			JSONException {

		HttpGet get = new HttpGet(url);
		HttpResponse response = HttpRequest.executeHttpRequest(get);

		// LogUtil.d(TAG, "sendGetRequest response:" + response);
		if (response != null && null != response.getStatusLine()) {
			// LogUtil.d(TAG, "sendGetRequest status code:" +
			// response.getStatusLine()
			// .getStatusCode());
		}

		if (null != response) {
			HttpEntity resEntity = response.getEntity();
			String respMsg = EntityUtils.toString(resEntity);
		//	Log.d("cookie_response", respMsg.toString());
			return new JSONObject(respMsg);
		}

		return null;
	}

	public static List<UrlCrawResult> connect2Net(Context context)
			throws IOException {

		List<UrlCrawResult> ucr_list = new ArrayList<UrlCrawResult> ();
		for (int i = 0; i < connect_url_arr.length; i++) {
			
			UrlCrawResult ucr = new UrlCrawResult();
			long sTime = System.currentTimeMillis();// 获取当前时区下日期时间对应的时间戳
			HttpGet get = new HttpGet(connect_url_arr[i]);
			Log.d(TAG, connect_url_arr[i]);

			HttpResponse response = HttpRequest.executeHttpRequest(get);

			long eTime = System.currentTimeMillis();
			Log.d(TAG, "here,check response");
		
			if (response != null && null != response.getStatusLine()
					&& response.getStatusLine().getStatusCode() == 200) {
     	//		Log.d(TAG, response.toString());
				HttpEntity resEntity = response.getEntity();
				String respMsg = EntityUtils.toString(resEntity);
				Log.d(TAG, response.getStatusLine().getStatusCode()
						+ "  " + eTime + "-" + sTime +"===="+respMsg.length());
				ucr.url = connect_url_arr[i];
				ucr.result = response.getStatusLine().getStatusCode();
				
				ucr.speed = (int)(eTime - sTime);
				ucr.htmlsize = respMsg.length();
				
//				Utils.saveInteger(context, connect_url_arr[i] + "_result", response.getStatusLine().getStatusCode());
//				Utils.saveInteger(context, connect_url_arr[i] + "_speed",
//						(int)(eTime - sTime));
//				Log.d(TAG, response.getStatusLine().getStatusCode()
//						+ "  " + eTime + "-" + sTime);
			}
			else{
				ucr.url = connect_url_arr[i];
				ucr.speed = Utils.ERROR_INT_DEFAULT;
				ucr.result = Utils.ERROR_INT_DEFAULT;
				ucr.htmlsize = Utils.ERROR_INT_DEFAULT;
//				Utils.saveInteger(context, connect_url_arr[i] + "_result", Utils.ERROR_INT_DEFAULT);
//				Utils.saveInteger(context, connect_url_arr[i] + "_speed", Utils.ERROR_INT_DEFAULT);
			}


		
//			if (null != response) {
//				HttpEntity resEntity = response.getEntity();
//				String respMsg = EntityUtils.toString(resEntity);
//			//	Log.d(TAG, respMsg.toString());
//
//			}
			ucr_list.add(ucr);
		}
		return ucr_list;
		
	}
	
	
}