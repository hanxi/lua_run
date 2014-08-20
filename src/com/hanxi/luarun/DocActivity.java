package com.hanxi.luarun;

import android.os.Bundle;

public class DocActivity extends MyWebkitActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SysApplication.getInstance().addActivity(this);

        DOC_URL = "file:///android_asset/www.lua.org/manual/5.2/index.html";
        mWebView.loadUrl(DOC_URL);
    }
}
