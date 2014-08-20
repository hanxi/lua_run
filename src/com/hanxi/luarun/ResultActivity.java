package com.hanxi.luarun;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
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

		RelativeLayout mBarView = (RelativeLayout) View.inflate(this,
				R.layout.result_titlebar, null);
		LinearLayout mLinearLayout = (LinearLayout) findViewById(R.id.result_titlebar);
		mLinearLayout.addView(mBarView);

		Bundle bunde = this.getIntent().getExtras();
		String str = bunde.getString("result").toString();
		TextView textView = (TextView) findViewById(R.id.statusText);
		// 改变文本框的文本内容
		textView.setText(str);
		Button btn = (Button) findViewById(R.id.btnBack);

		btn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();// 停止当前的Activity,如果不写,则按返回键会跳转回原来的Activity
			}
		});
	}
}
