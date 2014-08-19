package com.baidu.wifi.demo;

import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;

import android.R;
import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.SslErrorHandler;
import android.webkit.WebSettings;
import android.webkit.WebSettings.LayoutAlgorithm;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.book.BookList_Construct;
import com.baidu.book.BookList_type0;
import com.baidu.book.BookList_type1;
import com.baidu.book.BookList_type2;
import com.baidu.http.*;


public class SolveActivity extends Activity  implements View.OnClickListener{

    /** log tag. */
    private static final String TAG = SolveActivity.class.getSimpleName();

    private WebView mWebView;

    /** redirect uri 值为"oob" */
    private static final String REDIRECT = "oob";

    /** 开发中心 */
    static final String BOOK_URL = "http://dbl-wise-rc-tf25.vm.baidu.com:8080/test/FAQ.html";
    
    TextView textView_book_res = null;
    int connect_fail_BtnId = 0;
    int connect_slow_BtnId = 0;
    int connect_other_BtnId = 0;
    Button btn_connect_fail = null;
    Button btn_connect_slow = null;
    Button btn_connect_other = null;
    
    String connect_fail_res ;
    String connect_slow_res ;
    String connect_other_res ;
    
    public BookList_Construct booklist_solve;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = this.getIntent(); 
        booklist_solve=(BookList_Construct)intent.getSerializableExtra("booklist");
        Log.d(TAG, "booklist_solve = " + Html.fromHtml(booklist_solve.get_result()));
        
    	Resources resource = this.getResources();
		String pkgName = this.getPackageName();

		setContentView(resource.getIdentifier("solve_activity", "layout", pkgName));
		BookList_type0 bl0 = new BookList_type0();
		BookList_type1 bl1 = new BookList_type1();
		BookList_type2 bl2 = new BookList_type2();
		
		connect_fail_res = bl0.get_result();
		connect_slow_res = bl1.get_result();
		connect_other_res = bl2.get_result();
		
		textView_book_res = (TextView) findViewById(resource
				.getIdentifier("text_book_show", "id", pkgName));
		textView_book_res.setText(Html.fromHtml(booklist_solve.get_result()));
		
//		connect_fail_BtnId = resource
//				.getIdentifier("btn_connect_failed", "id", pkgName);
//		btn_connect_fail =(Button) findViewById(connect_fail_BtnId);
//		btn_connect_fail.setOnClickListener(this);
//		
//		connect_slow_BtnId = resource
//				.getIdentifier("btn_connect_slow", "id", pkgName);
//		btn_connect_slow =(Button) findViewById(connect_slow_BtnId);
//		btn_connect_slow.setOnClickListener(this);
//		
//		connect_other_BtnId = resource
//				.getIdentifier("btn_connect_other", "id", pkgName);
//		btn_connect_other =(Button) findViewById(connect_other_BtnId);
//		btn_connect_other.setOnClickListener(this);
//		
//		textView_book_res.setText(Html.fromHtml(connect_fail_res));

		/*
        mWebView = new WebView(BookActivity.this);

        setContentView(mWebView);

        initWebView(mWebView);
        mWebView.loadUrl(BOOK_URL);
        */
    }


    /**
     * 设置Webview的WebviewClient
     * 
     * @param webview
     *            webview
     */
    private void initWebView(WebView webview) {
        WebSettings settings = webview.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setLayoutAlgorithm(LayoutAlgorithm.SINGLE_COLUMN);

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

            	/*
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
                    intent.setClass(BookActivity.this, WifiActivity.class);
                 
                    intent.putExtra(Utils.BDUSS,BDUSS);
                    startActivity(intent);

                    finish();
                }
                */
            }

        });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (goBack()) {
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    private boolean goBack() {
        WebView webView = mWebView;
        if (webView != null && webView.canGoBack()) {
            webView.goBack();

            return true;
        }

        return false;
    }


	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		 Log.d("book", "start click....");
		 if (v.getId() == connect_fail_BtnId) {
			 connect_fail_click();
		 } else if (v.getId() == connect_slow_BtnId) {
			 Log.d(TAG, "click slow button");
			 connect_slow_click();
		 } 
		 else if (v.getId() == connect_other_BtnId) {
			 connect_other_click();
		 } 
		
	}
	
	
	public void connect_fail_click(){
		textView_book_res.setText(Html.fromHtml(connect_fail_res));
		btn_connect_fail.setBackgroundColor(Color.parseColor("#FFFFFF"));
		btn_connect_fail.setTextColor(Color.parseColor("#006fAE"));
		btn_connect_slow.setBackgroundColor(Color.parseColor("#3B6680"));
		btn_connect_slow.setTextColor(Color.parseColor("#BEBEBE"));
		btn_connect_other.setBackgroundColor(Color.parseColor("#3B6680"));
		btn_connect_other.setTextColor(Color.parseColor("#BEBEBE"));
		
		
		
		
	}
	public void connect_slow_click(){
		textView_book_res.setText(Html.fromHtml(connect_slow_res));
		btn_connect_fail.setBackgroundColor(Color.parseColor("#3B6680"));
		btn_connect_fail.setTextColor(Color.parseColor("#BEBEBE"));
		btn_connect_slow.setBackgroundColor(Color.parseColor("#FFFFFF"));
		btn_connect_slow.setTextColor(Color.parseColor("#006fAE"));
		btn_connect_other.setBackgroundColor(Color.parseColor("#3B6680"));
		btn_connect_other.setTextColor(Color.parseColor("#BEBEBE"));
		
	}
	public void connect_other_click(){
		
		textView_book_res.setText(Html.fromHtml(connect_other_res));
		btn_connect_fail.setBackgroundColor(Color.parseColor("#3B6680"));
		btn_connect_fail.setTextColor(Color.parseColor("#BEBEBE"));
		btn_connect_slow.setBackgroundColor(Color.parseColor("#3B6680"));
		btn_connect_slow.setTextColor(Color.parseColor("#BEBEBE"));
		btn_connect_other.setBackgroundColor(Color.parseColor("#FFFFFF"));
		btn_connect_other.setTextColor(Color.parseColor("#006fAE"));
		
	}
	
}
