package com.baidu.lib.router;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import com.baidu.http.HttpRequest;
import com.baidu.http.Transport;
import com.baidu.lib.wifi.NetPing;
import com.baidu.lib.wifi.WifiCompute;
import com.baidu.wifi.demo.Utils;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.util.Log;

/**
 * router相关信息获取
 * 
 * @author li_jing02
 * 
 */
public class ConntectRouter {
	
	private final static String TAG =  ConntectRouter.class.getSimpleName();
	protected final static String URL_BASE = "http://x.du:8090/blink/";
	public static String DEVICEID = "deviceid";
	private static String SIGN = "sign";
	private static String RESULT = "result";
	private static String TIME = "time";

	
	protected static String constructUrl(String url, String method,List<NameValuePair> params) {

		if(params == null)
			return url +method;
		
		String param = URLEncodedUtils.format(params, "UTF-8");

		String newUrl = url +method+ "?" + param;

		return newUrl;
	}
	

	
	/**
	 * 获取路由wifi信息
	 * @return
	 * @throws IOException
	 * @throws JSONException
	 */
	
	
	public static JSONObject  getwifiInfo(){
		
	//	BasicNameValuePair basicPair = new BasicNameValuePair("device_id", this.DEVICEID);
		List<NameValuePair> pairs = new ArrayList<NameValuePair>();
		pairs.add(new BasicNameValuePair("device_id", DEVICEID));
		pairs.add(new BasicNameValuePair("sign", SIGN));
		String url = constructUrl(URL_BASE,"getWifiInfo",pairs);
		JSONObject result;
		try {
			result = Transport.sendGetRequest(url);
			Log.d(TAG, "getwifiInfo:  "+result.toString());
			return result;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	} 
	
	
	/**
	 * 获取路由状态
	 * @return
	 * @throws IOException
	 * @throws JSONException
	 */
	
	public static JSONObject  getBlinkStatus(){
		
	//	BasicNameValuePair basicPair = new BasicNameValuePair("device_id", this.DEVICEID);
		List<NameValuePair> pairs = new ArrayList<NameValuePair>();
		pairs.add(new BasicNameValuePair("device_id", DEVICEID));
		pairs.add(new BasicNameValuePair("sign", SIGN));
		String url = constructUrl(URL_BASE,"getBlinkStatus",pairs);
		JSONObject result;
		try {
			result = Transport.sendGetRequest(url);
			Log.d(TAG, "getBlinkStatus:  "+result.toString());
			return result;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	} 
	
	/**
	 * 校验管理员密码
	 * @return
	 * @throws IOException
	 * @throws JSONException
	 */
	
	public static JSONObject  checkAdminPassword() {
		
	//	BasicNameValuePair basicPair = new BasicNameValuePair("device_id", this.DEVICEID);
		List<NameValuePair> pairs = new ArrayList<NameValuePair>();
		pairs.add(new BasicNameValuePair("device_id", DEVICEID));
		pairs.add(new BasicNameValuePair("sign", SIGN));
		String url = constructUrl(URL_BASE,"checkAdminPassword",pairs);
		JSONObject result;
		try {
			result = Transport.sendGetRequest(url);
			Log.d(TAG, "checkAdminPassword:  "+result.toString());
			return result;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	} 
	
	
	/**
	 * 获取接入路由的详细信息
	 * @return
	 * @throws IOException
	 * @throws JSONException
	 */
	
	public static JSONObject  getAttachDeviceList(){
		
	//	BasicNameValuePair basicPair = new BasicNameValuePair("device_id", this.DEVICEID);
		List<NameValuePair> pairs = new ArrayList<NameValuePair>();
		pairs.add(new BasicNameValuePair("device_id", DEVICEID));
		pairs.add(new BasicNameValuePair("sign", SIGN));
		String url = constructUrl(URL_BASE,"getAttachDeviceList",pairs);
		JSONObject result;
		try {
			result = Transport.sendGetRequest(url);
			Log.d(TAG, "getAttachDeviceList:  "+result.toString());
			return result;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	} 
	
	/**
	 * 获取路由的安全性校验结果
	 * @return
	 * @throws IOException
	 * @throws JSONException
	 */
	public static JSONObject  getSafetyStatus(){
		
	//	BasicNameValuePair basicPair = new BasicNameValuePair("device_id", this.DEVICEID);
		List<NameValuePair> pairs = new ArrayList<NameValuePair>();
		pairs.add(new BasicNameValuePair("device_id", DEVICEID));
		pairs.add(new BasicNameValuePair("sign", SIGN));
		String url = constructUrl(URL_BASE,"getSafetyStatus",pairs);
		JSONObject result;
		try {
			result = Transport.sendGetRequest(url);
			Log.d(TAG, "getSafetyStatus:  "+result.toString());
			return result;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	} 
	
	/**
	 * 获取路由是否初始化,主要用来获取上网方式DHCP/PPPOE/STATIC
	 * @return
	 * @throws IOException
	 * @throws JSONException
	 */
	public static JSONObject  checkInitialized() {
		
	//	BasicNameValuePair basicPair = new BasicNameValuePair("device_id", this.DEVICEID);
		List<NameValuePair> pairs = new ArrayList<NameValuePair>();
		pairs.add(new BasicNameValuePair("device_id", DEVICEID));
		pairs.add(new BasicNameValuePair("sign", SIGN));
		String url = constructUrl(URL_BASE,"checkInitialized",pairs);
		JSONObject result;
		try {
			result = Transport.sendGetRequest(url);
			Log.d(TAG, "checkInitialized:  "+result.toString());
			return result;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;
	} 
	
	/**
	 * 获取路由配置的版本信息
	 * @return
	 * @throws IOException
	 * @throws JSONException
	 */
	public static JSONObject  getVersion(){
		
	//	BasicNameValuePair basicPair = new BasicNameValuePair("device_id", this.DEVICEID);
		List<NameValuePair> pairs = new ArrayList<NameValuePair>();
		pairs.add(new BasicNameValuePair("device_id", DEVICEID));
		pairs.add(new BasicNameValuePair("sign", SIGN));
		String url = constructUrl(URL_BASE,"getVersion",pairs);
		JSONObject result;
		try {
			result = Transport.sendGetRequest(url);
			Log.d(TAG, "getVersion:  "+result.toString());
			return result;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	} 

	public  static String  getDeviceId(){
		String url = constructUrl(URL_BASE,"getDeviceId",null);
	
		try {
			JSONObject result =Transport.sendGetRequest(url);
			if(result != null){
				Log.d(TAG, "getDeviceId: "+result.toString());
				DEVICEID = result.getString("deviceId");
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return DEVICEID;
	}
	
	
	public static void getCPU_MemInfo(String pwd){
		String url = "http://192.168.99.1/cgi-bin/luci?xcloud=0&username=root&password="+pwd;
		HttpGet get = new HttpGet(url);
		HttpResponse response = HttpRequest.executeHttpRequest(get);

		// LogUtil.d(TAG, "sendGetRequest response:" + response);
		if (response != null && null != response.getStatusLine()) {
			 Log.d(TAG, "sendGetRequest status code:" +
			 response.getStatusLine()
			 .getStatusCode());
			 Log.d(TAG, "reponse cookie:" +response.getHeaders("Set-Cookie").toString());
			
		}
		Header[] headers = response.getAllHeaders();
		for(int i=0;i<headers.length;i++){
			Log.d(TAG, headers[i].getName()+"  :  "+headers[i].getValue());
		}
		
		HttpEntity respBody = response.getEntity();
		


		try {
			String respMsg = EntityUtils.toString(respBody);
			Log.d(TAG, "respMsgAA is" +respMsg);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		url = "http://192.168.99.1/cgi-bin/luci/admin/status/device_info";
		get = new HttpGet(url);
		response = HttpRequest.executeHttpRequest(get);
		
		// LogUtil.d(TAG, "sendGetRequest response:" + response);
		if (response != null && null != response.getStatusLine()) {
			 Log.d(TAG, "sendGetRequest status code:" +
			 response.getStatusLine()
			 .getStatusCode());
			 Log.d(TAG, "reponse:" +response.toString());
			
		}
		respBody = response.getEntity();
		try {
			String respMsg = EntityUtils.toString(respBody);
			Log.d(TAG, "respMsgBB is" +respMsg);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
	}
	
	
	public static void computeSign(String pwd){
		if(DEVICEID == "deviceid")
			return;
		try {
		//	SIGN = getStringMD5String(DEVICEID+"12345");
			SIGN = MD5(DEVICEID+pwd);
			Log.d(TAG, "sign is : "+SIGN);
			Log.d(TAG, "device_id is : "+DEVICEID);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	private static String MD5(String sourceStr) {
        String result = "";
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(sourceStr.getBytes());
            byte b[] = md.digest();
            int i;
            StringBuffer buf = new StringBuffer("");
            for (int offset = 0; offset < b.length; offset++) {
                i = b[offset];
                if (i < 0)
                    i += 256;
                if (i < 16)
                    buf.append("0");
                buf.append(Integer.toHexString(i));
            }
            result = buf.toString();
            Log.d(TAG, "md5,32   "+result);
          
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return result;
    }
	
	 /*获取一个字符串的md5码 */
	 public static String getStringMD5String(String str) throws Exception
	 {
		 MessageDigest messagedigest = MessageDigest.getInstance("MD5");
		 messagedigest.update(str.getBytes()); 
		 return bytesToString(messagedigest.digest());
	 }
	 
	 public static String bytesToString(byte[] data)
	 {
		  char hexDigits[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd',
		    'e', 'f' };
		  char[] temp = new char[data.length * 2];
		  for (int i = 0; i < data.length; i++)
		  {
		   byte b = data[i];
		   temp[i * 2] = hexDigits[b >>> 4 & 0x0f];
		   temp[i * 2 + 1] = hexDigits[b & 0x0f];
		  }
		  return new String(temp);
		 }
}
