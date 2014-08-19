package com.baidu.lib.router;
import java.io.IOException;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.baidu.http.NetworkCheck;
import com.baidu.http.Transport;
import com.baidu.http.Transport.UrlCrawResult;
import com.baidu.lib.router.RouterListener;
import com.baidu.lib.wifi.NetPing;
import com.baidu.lib.wifi.WifiCompute;
import com.baidu.wifi.demo.Utils;

import android.content.Context;
import android.os.AsyncTask;  
import android.util.Log;
  
/**  
 * 生成该类的对象，并调用execute方法之后  
 * 首先执行的是onProExecute方法  
 * 其次执行doInBackgroup方法  
 *  
 */  
public class RouterCheckAsyncTask extends AsyncTask<Object, Integer, RouterMeta> {  
  
 //   private TextView textView;  
 //   private ScrollView scrollView;
    private Context mContext;
    private RouterListener mListener;
    private final static String TAG =  RouterCheckAsyncTask.class.getSimpleName();
      
      
    public RouterCheckAsyncTask(Context context, RouterListener listener) {  
        super();  
   //     this.textView = textView; 
    //    this.scrollView =  scrollView;
        mContext = context;
        mListener = listener;
    }  
  
  
    /**  
     * 这里的Integer参数对应AsyncTask中的第一个参数   
     * 这里的String返回值对应AsyncTask的第三个参数  
     * 该方法并不运行在UI线程当中，主要用于异步操作，所有在该方法中不能对UI当中的空间进行设置和修改  
     * 但是可以调用publishProgress方法触发onProgressUpdate对UI进行操作  
     */  
    @Override  
	protected RouterMeta doInBackground(Object... params) {
    	
    
    	/*network 检测*/
    	
		List<UrlCrawResult> result;
		try {
			if(NetworkCheck.isNetworkAvailable((Context) params[0])){
				result = Transport.connect2Net((Context) params[0]);
				for(UrlCrawResult res:result){
					Utils.saveInteger((Context) params[0],res.url+"_result",res.result);
					Utils.saveInteger((Context) params[0], res.url+"_speed", res.speed);
					Utils.saveInteger((Context) params[0], res.url+"_htmlsize", res.htmlsize);
				}
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		NetPing.Ping((Context)params[0],Utils.getString((Context) params[0], new WifiCompute().NOW_CONNECTED_WIFI_GATEWAY));
		
		NetPing.Ping((Context)params[0], Utils.DNS_hijacking);
		
		
		/*路由检测 */
    	RouterMeta routerMeta = new  RouterMeta();
    	
		String deviceId = ConntectRouter.getDeviceId();
		routerMeta.setDeviceId(deviceId);
		 ConntectRouter.computeSign(Utils.getString(mContext, "adminpwd"));
		 JSONObject wifiinfo = ConntectRouter.getwifiInfo();
		 JSONObject routerinfo = ConntectRouter.getBlinkStatus();
		 JSONObject versioninfo = ConntectRouter.getVersion();
		 JSONObject AttachDeviceList = ConntectRouter.getAttachDeviceList();
		 setAdminPwd(Utils.getString(mContext, "adminpwd"),routerMeta);
		 setWifiStatus(wifiinfo,routerMeta);
		 setWifiType(wifiinfo,routerMeta);
		 setRouterInfo(routerinfo,routerMeta);
		 setVersionInfo(versioninfo,routerMeta);
		 setDevice_Info(AttachDeviceList,routerMeta);
		 
		return routerMeta;
}
//    protected String doInBackground(Integer... params) {  
//        NetOperator netOperator = new NetOperator();  
//        int i = 0;  
//        for (i = 10; i <= 100; i+=10) {  
//            netOperator.operator();  
//            publishProgress(i);  
//        }  
//        return i + params[0].intValue() + "";  
//    }  
  
  
	/**  
     * 这里的String参数对应AsyncTask中的第三个参数（也就是接收doInBackground的返回值）  
     * 在doInBackground方法执行结束之后在运行，并且运行在UI线程当中 可以对UI空间进行设置  
     */  
    @Override  
    protected void onPostExecute(RouterMeta result) {  
  
    	if (null != mListener) {
    		mListener.onSuccess(result);
    	}
    	else
    		mListener.onFailure(0, "can't connect router");
	
    }  
  
  
    //该方法运行在UI线程当中,并且运行在UI线程当中 可以对UI空间进行设置  
    @Override  
    protected void onPreExecute() {  
    //    textView.setText("开始执行异步线程"); 
      
    }  
  
  
    /**  
     * 这里的Intege参数对应AsyncTask中的第二个参数  
     * 在doInBackground方法当中，，每次调用publishProgress方法都会触发onProgressUpdate执行  
     * onProgressUpdate是在UI线程中执行，所有可以对UI空间进行操作  
     */  
    @Override  
    protected void onProgressUpdate(Integer... values) {  
        int vlaue = values[0];  
//        progressBar.setProgress(vlaue);  
    }  


    private void setWifiType(JSONObject wifiinfo,RouterMeta routermeta) {
		// TODO Auto-generated method stub
    	
    	int wifiType = Utils.ERROR_INT_DEFAULT;
    	
    	try {
			if(wifiinfo == null || wifiinfo.getJSONObject("body").getInt("errorCode") != 0)
				wifiType = Utils.ERROR_INT_DEFAULT;
	
	    	JSONArray des = wifiinfo.getJSONObject("body").getJSONArray("description");
	    	for (int i = 0; i < des.length(); i++) {
	    	    JSONObject object = (JSONObject) des.get(i);
	    	    String ssid = object.getString("ssid").trim();
	    	    Log.d(TAG, "ssid is "+ssid);
	    	    String wifiname = Utils.getString(mContext,  new WifiCompute().NOW_CONNECTED_WIFI_NAME).trim();
	    	    Log.d(TAG, "wifiname is "+wifiname);
	    	    if(ssid.equals(wifiname))
	    	    	wifiType = object.getInt("wifiType");
	    	}
    	} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	routermeta.setWifiType(wifiType);
	}
      

    private void setWifiStatus(JSONObject wifiinfo,RouterMeta routermeta) {
		// TODO Auto-generated method stub
    	
    	int status = Utils.ERROR_INT_DEFAULT;
    	
    	try {
			if(wifiinfo == null || wifiinfo.getJSONObject("body").getInt("errorCode") != 0)
				status = Utils.ERROR_INT_DEFAULT;
	
	    	JSONArray des = wifiinfo.getJSONObject("body").getJSONArray("description");
	    	for (int i = 0; i < des.length(); i++) {
	    	    JSONObject object = (JSONObject) des.get(i);
	    	    String ssid = object.getString("ssid").trim();
	    	    Log.d(TAG, "ssid is "+ssid);
	    	    String wifiname = Utils.getString(mContext,  new WifiCompute().NOW_CONNECTED_WIFI_NAME).trim();
	    	    Log.d(TAG, "wifiname is "+wifiname);
	    	    if(ssid.equals(wifiname))
	    	    	status = object.getInt("status");
	    	}
    	} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	routermeta.setStatus(status);
    //	return routermeta;
	}
  
    private void setRouterInfo(JSONObject routerinfo,RouterMeta routermeta) {
		// TODO Auto-generated method stub
    	int deviceNum = Utils.ERROR_INT_DEFAULT;
    	int taskNum = Utils.ERROR_INT_DEFAULT;
    	int cur_speed = Utils.ERROR_INT_DEFAULT;
    	int avg_speed = Utils.ERROR_INT_DEFAULT;
    	int network = Utils.ERROR_INT_DEFAULT;
    	int wan =  Utils.ERROR_INT_DEFAULT;
    //	float diskUseRate = Utils.ERROR_INT_DEFAULT;
    	
    	try {
			if(routerinfo == null || routerinfo.getJSONObject("body").getInt("errorCode") != 0)
				return;
	
	    	 deviceNum = routerinfo.getJSONObject("body").getJSONObject("description").getInt("deviceNum");
	    	 taskNum = routerinfo.getJSONObject("body").getJSONObject("description").getInt("taskNum");
	    	 cur_speed = routerinfo.getJSONObject("body").getJSONObject("description").getInt("currentSpeed");
	    //	 avg_speed = routerinfo.getJSONObject("body").getJSONObject("description").getInt("averageSpeed");
	    	 network = routerinfo.getJSONObject("body").getJSONObject("description").getInt("network");
	    	 wan = routerinfo.getJSONObject("body").getJSONObject("description").getInt("wan");
	    	 int usedSize = routerinfo.getJSONObject("body").getJSONObject("description").getInt("usedSize");
	    	 int totalSize = routerinfo.getJSONObject("body").getJSONObject("description").getInt("totalSize");
	    	 
	    	 routermeta.setDeviceNum(deviceNum);
	    	 routermeta.setTaskNum(taskNum);
	    	 routermeta.setNetwork(network);
	    	 routermeta.setDiskUseRate(usedSize, totalSize);
	    	 routermeta.setWan(wan);
	    	 routermeta.setCur_Speed(cur_speed);  
	    //	 routermeta.setAvg_Speed(avg_speed);
	    	
    	} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
    
    private void setVersionInfo(JSONObject versionInfo,RouterMeta routermeta){
    	
    	RouterVersion rv ;
    	try {
			if(versionInfo == null || versionInfo.getJSONObject("body").getInt("errorCode") != 0)
				return;
			String hwn = versionInfo.getJSONObject("body").getJSONObject("description").getString("hwn");
			String hwv = versionInfo.getJSONObject("body").getJSONObject("description").getString("hwv");
			String FW_version = versionInfo.getJSONObject("body").getJSONObject("description").getString("FW_version");
			String Manager_version = versionInfo.getJSONObject("body").getJSONObject("description").getString("Manager_version");
			RouterVersion routerversion = new RouterVersion (hwn,hwv,FW_version,Manager_version);
			routermeta.setRouterVersion(routerversion);
			
      	} catch (JSONException e) {
    			// TODO Auto-generated catch block
    			e.printStackTrace();
    		}
    	
    }
    
    private void setAdminPwd(String pwd,RouterMeta routermeta){
    	routermeta.setAdminPwd(pwd);
    }
    private void setDevice_Info(JSONObject AttachDeviceList,RouterMeta routermeta){
    	
    	try {
	    	if(AttachDeviceList == null || AttachDeviceList.getJSONObject("body").getInt("errorCode") != 0)
				return;
			JSONArray jsonArray = AttachDeviceList.getJSONObject("body").getJSONArray("description");
			for(int i=0;i<jsonArray.length();i++){
				String ip = jsonArray.getJSONObject(i).getString("ip");
				int speed =  jsonArray.getJSONObject(i).getInt("currentSpeed");
				int accessInternet = jsonArray.getJSONObject(i).getInt("accessInternet");
				int accessRouter = jsonArray.getJSONObject(i).getInt("accessRouter");
				String cur_ip = Utils.getString(mContext, new WifiCompute().NOW_CONNECTED_WIFI_IP);
				if(cur_ip.equals(ip)){
					routermeta.setDeviceCur_Speed(speed);
					routermeta.setAccessInternet(accessInternet);
					routermeta.setAccessRouter(accessRouter);				
				}
					
			}
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    

}  