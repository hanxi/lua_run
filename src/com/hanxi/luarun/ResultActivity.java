package com.hanxi.luarun;

import net.youmi.android.banner.AdSize;
import net.youmi.android.banner.AdView;
import android.app.Activity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class ResultActivity extends Activity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.result_activity_main);
		SysApplication.getInstance().addActivity(this); 
		
		
        RelativeLayout mBarView = (RelativeLayout)View.inflate(this, R.layout.result_titlebar, null);
        LinearLayout mLinearLayout = (LinearLayout)findViewById(R.id.result_titlebar);
        mLinearLayout.addView(mBarView);  

		// 广告
//		SmartBannerManager.show(this);
      //实例化LayoutParams(重要)
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(
        FrameLayout.LayoutParams.FILL_PARENT,
        FrameLayout.LayoutParams.WRAP_CONTENT);     
        //设置广告条的悬浮位置
        layoutParams.gravity = Gravity.BOTTOM | Gravity.RIGHT; // 这里示例为右下角  
        //实例化广告条
        AdView adView = new AdView(this, AdSize.FIT_SCREEN);
        //调用Activity的addContentView函数
        this.addContentView(adView, layoutParams);
        
        Bundle bunde = this.getIntent().getExtras();
        String str=bunde.getString("result").toString();
        TextView textView = (TextView)findViewById(R.id.statusText);
        //改变文本框的文本内容
        textView.setText(str);
        Button btn = (Button)findViewById(R.id.btnBack);
        

        btn.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View v) {
				finish();//停止当前的Activity,如果不写,则按返回键会跳转回原来的Activity		
			}
        });
    }
}