package com.baidu.lib.wifi;
import java.io.IOException;
import java.util.List;

import com.baidu.http.NetworkCheck;
import com.baidu.http.Transport;
import com.baidu.http.Transport.UrlCrawResult;
import com.baidu.lib.wifi.NetPing;
import com.baidu.lib.wifi.NetworkListener;
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
public class NetworkCheckAsyncTask extends AsyncTask<Object, Integer, Void> {  
  
 //   private TextView textView;  
 //   private ScrollView scrollView;
    private Context mContext;
    private NetworkListener mListener;
      
      
    public NetworkCheckAsyncTask(Context context, NetworkListener listener) {  
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
	protected Void doInBackground(Object... params) {
		
//		Utils.saveString((Context)params[0], "uid", Utils.UID);
//		Utils.saveString((Context)params[0], "uname", Utils.UNAME);
//		Utils.saveString((Context)params[0], "checktime", Long.toString(System.currentTimeMillis()));
		
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
		
		return null;
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
    protected void onPostExecute(Void result) {  
    //    textView.setText("异步操作执行结束" );  
       // new WifiActivity().updateDisplay();
   /* 	String text = "hi,"
				+ Utils.UNAME
				+ Utils.NEWLINE
				+ new WifiCompute().getWifiInfo_check(context)
				+ Utils.NEWLINE
				+ new NetworkCollect().getNetwork_IsConnected(context)
				+ Utils.NEWLINE
				+ new NetworkCollect().getNetwork_speed(context)
				+ Utils.NEWLINE
				+ new NetPing().getPingDns_LossRate(context)
				+ Utils.NEWLINE
				+new NetPing().getPingDns_RTT_AVG(context)
				+ Utils.NEWLINE
				+new NetPing().getPingDns_stability(context);
				
		
		if (textView != null) {
			textView.setText(Html.fromHtml(text));
			

		}
		if (scrollView != null) {
			scrollView.fullScroll(ScrollView.FOCUS_DOWN);
		}*/
    	if (null != mListener) {
    		mListener.onReceive();
    	}
	
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
  
      
      
      
  
}  