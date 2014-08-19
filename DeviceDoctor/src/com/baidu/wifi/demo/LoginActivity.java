package com.baidu.wifi.demo;

import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.SslErrorHandler;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;
import com.baidu.http.*;

/**
 * 登录百度账号初始化Channel的Activity
 */
public class LoginActivity extends Activity {

    /** log tag. */
    private static final String TAG = LoginActivity.class.getSimpleName();

    private WebView mWebView;

    /** redirect uri 值为"oob" */
    private static final String REDIRECT = "oob";

    /** 开发中心 */
    static final String DEV_CENTER = "https://openapi.baidu.com/";
    
    private static String BDUSS="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        is_logined();

        mWebView = new WebView(LoginActivity.this);

        setContentView(mWebView);

        initWebView(mWebView);

//        getAccessToken();
    }

    private void is_logined(){
    	if(Utils.getString(getApplicationContext(), "uid").equals("uid"))
    		return;
    	else
    	{
    	    Intent intent = new Intent();
            intent.setClass(LoginActivity.this, WifiActivity.class);
            startActivity(intent);

    	}
    }
    
    
//    /**
//     * 获取 access token的url
//     */
//    private void getAccessToken() {
//        String url = DEV_CENTER + "oauth/2.0/authorize?"
//                + "response_type=token" + "&client_id="
//                + Utils.getMetaValue(LoginActivity.this, "api_key")
//                + "&redirect_uri=" + REDIRECT + "&display=mobile";
//
//        Log.d(TAG, "GetAccessTokenUrl: " + url);
//
//        mWebView.loadUrl(url);
//    }

    /**
     * 设置Webview的WebviewClient
     * 
     * @param webview
     *            webview
     */
    private void initWebView(WebView webview) {
        WebSettings settings = webview.getSettings();
        settings.setJavaScriptEnabled(true);

        webview.setWebViewClient(new WebViewClient() {

            @Override
            public void onReceivedSslError(WebView view,
                    SslErrorHandler handler, SslError error) {
                handler.proceed();
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return super.shouldOverrideUrlLoading(view, url);
            }

            @Override
            public void onPageFinished(WebView view, String url) {

                if (url.startsWith(REDIRECT) || url.contains("login_success")) {

                    // change # -> ?
                	Log.d("login", url);
                	String cookie_view =   CookieManager.getInstance().getCookie(url);
                    String [] cookie_arr = cookie_view.split(";");
                    for(int i=0;i<cookie_arr.length;i++){
                    	if(cookie_arr[i].indexOf("BDUSS=")>=0)
                    		BDUSS = cookie_arr[i].replace("BDUSS=", "");
                    	    BDUSS = BDUSS.replace(" ", "");
                    }
                    Log.d("cookie_bduss", BDUSS);
                    int fragmentIndex = url.indexOf("#");
                    url = "http://localhost/?"
                            + url.substring(fragmentIndex + 1);

                    // 从URL中获得Access token
                    String accessToken = Uri.parse(url).getQueryParameter(
                            "access_token");
                    Log.d(TAG, ">>> Get Original AccessToken: \r\n"
                            + accessToken);
                    Transport.getLoginedUid("https://openapi.baidu.com/rest/2.0/passport/users/getLoggedInUser?access_token="+accessToken+"&format=json");                  
               
                    Intent intent = new Intent();
                    intent.setClass(LoginActivity.this, WifiActivity.class);
                 
                    intent.putExtra(Utils.BDUSS,BDUSS);
                    startActivity(intent);

                    finish();
                }
            }

        });
    }

//    @Override
//    public boolean onKeyDown(int keyCode, KeyEvent event) {
//        if (keyCode == KeyEvent.KEYCODE_BACK) {
//            if (goBack()) {
//                return true;
//            }
//        }
//        return super.onKeyDown(keyCode, event);
//    }
//
//    private boolean goBack() {
//        WebView webView = mWebView;
//        if (webView != null && webView.canGoBack()) {
//            webView.goBack();
//
//            return true;
//        }
//
//        return false;
//    }
}
