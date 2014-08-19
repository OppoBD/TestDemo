package com.baidu.lib.router;

import org.json.JSONObject;

public interface RouterListener {

	public void onSuccess(Object obj);
	
	public void onFailure(int errCode, String errMsg);
	
}
