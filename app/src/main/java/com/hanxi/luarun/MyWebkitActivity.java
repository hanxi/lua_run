package com.hanxi.luarun;

import android.app.Activity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.webkit.WebSettings;
import android.webkit.WebSettings.LayoutAlgorithm;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

public class MyWebkitActivity extends Activity {
    public WebView mWebView;
	protected String DOC_URL = "file:///android_asset/www.lua.org/index.html";
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		SysApplication.getInstance().addActivity(this); 
		
        mWebView = new WebView(this);
        WebSettings webSettings = mWebView.getSettings();
        webSettings.setLayoutAlgorithm(LayoutAlgorithm.SINGLE_COLUMN);
        webSettings.setJavaScriptEnabled(true);
        mWebView.requestFocus();
        mWebView.setWebViewClient(new WebViewClient(){
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url){
                    view.loadUrl(url);
                    return false;
            }
        });
		setContentView(mWebView);
	}

	@Override 
    public boolean onKeyDown(int keyCode, KeyEvent event) { 
		if (keyCode == KeyEvent.KEYCODE_BACK) { 
			String url = mWebView.getOriginalUrl();
			if (url.equals(DOC_URL)){
				pressAgainExit();
				return true;
			}
			else {
       		    mWebView.goBack();   //后退 
                return true; 
			}
        } 
        return super.onKeyDown(keyCode, event); 
    } 
	
    private void pressAgainExit() { 
        if (Exit.isExit()) { 
        	SysApplication.getInstance().exit();
        } else { 
            Toast.makeText(getApplicationContext(), R.string.exit_again, 
            		Integer.valueOf(R.string.exit_time)).show(); 
            Exit.doExitInOneSecond(); 
        } 
    }
}
