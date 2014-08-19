package com.baidu.http;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.params.CookiePolicy;
import org.apache.http.client.params.HttpClientParams;

public class HttpRequest {
	
	// the times to re-try
	private final static int Max_Retries = 1;
	
	/**
	 * send http request
	 */
	public static HttpResponse executeHttpRequest(HttpRequestBase request){
	
		HttpResponse ret = null;
		
		if(null != request){
			
			// create client
			HttpClient client = HttpClientFactory.makeHttpClient();
			HttpClientParams.setCookiePolicy(client.getParams(), CookiePolicy.BROWSER_COMPATIBILITY);

			for (int retries = 0; ret == null && retries < Max_Retries; ++retries) {
				/*
				 * The try/catch is a workaround for a bug in the HttpClient libraries. It should be returning null
				 * instead when an error occurs. Fixed in HttpClient 4.1, but we're stuck with this for now. See:
				 * http://code.google.com/p/android/issues/detail?id=5255
				 */
				try {
					
					ret = client.execute(request);
					
				}catch (Exception e) {
					e.printStackTrace();
				}
				
				if(null == ret){
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						//do nothing
					}
				}
			}
		}
		
		return ret;
	}
	
}
