package com.hanxi.luarun;

import net.youmi.android.AdManager;
import net.youmi.android.diy.DiyManager;
import net.youmi.android.smart.SmartBannerManager;
import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Window;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TabHost;

public class MainTabActivity extends TabActivity implements OnCheckedChangeListener{
	
	private TabHost mTabHost;
	private Intent mAIntent;
	private Intent mBIntent;
	private Intent mCIntent;
	private Intent mDIntent;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.main);
		SysApplication.getInstance().addActivity(this); 
        
        
        this.mAIntent = new Intent(this,HelpActivity.class);
        this.mBIntent = new Intent(this,MainActivity.class);
        this.mCIntent = new Intent(this,DocActivity.class);
        this.mDIntent = new Intent(this,SetActivity.class);
        
		((RadioButton) findViewById(R.id.radio_button0))
		.setOnCheckedChangeListener(this);
        ((RadioButton) findViewById(R.id.radio_button1))
		.setOnCheckedChangeListener(this);
        ((RadioButton) findViewById(R.id.radio_button2))
		.setOnCheckedChangeListener(this);
        ((RadioButton) findViewById(R.id.radio_button3))
		.setOnCheckedChangeListener(this);
        ((RadioButton) findViewById(R.id.radio_button4))
		.setOnCheckedChangeListener(this);
        
        setupIntent();
        this.mTabHost.setCurrentTabByTag("B_TAB");

        // 初始化广告
        AdManager.getInstance(this).init("748ed2ca85f9d618","b37e60ff668dadc7", false);
        SmartBannerManager.init(this);
    }

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		if(isChecked){
			switch (buttonView.getId()) {
			case R.id.radio_button0:
				this.mTabHost.setCurrentTabByTag("A_TAB");
				break;
			case R.id.radio_button1:
				this.mTabHost.setCurrentTabByTag("B_TAB");
				break;
			case R.id.radio_button2:
				this.mTabHost.setCurrentTabByTag("C_TAB");
				break;
			case R.id.radio_button3:
				this.mTabHost.setCurrentTabByTag("D_TAB");
				break;
			case R.id.radio_button4:
				DiyManager.showRecommendGameWall(this);
				break;
			}
		}
		
	}
	
	private void setupIntent() {
		this.mTabHost = getTabHost();
		TabHost localTabHost = this.mTabHost;

		localTabHost.addTab(buildTabSpec("A_TAB", R.string.main_help,
				R.drawable.icon_1_n, this.mAIntent));

		localTabHost.addTab(buildTabSpec("B_TAB", R.string.main_home,
				R.drawable.icon_2_n, this.mBIntent));

		localTabHost.addTab(buildTabSpec("C_TAB",R.string.main_doc,
				R.drawable.icon_3_n, this.mCIntent));

		localTabHost.addTab(buildTabSpec("D_TAB", R.string.main_set,
				R.drawable.icon_4_n, this.mDIntent));
	}
	
	private TabHost.TabSpec buildTabSpec(String tag, int resLabel, int resIcon,
			final Intent content) {
		return this.mTabHost.newTabSpec(tag).setIndicator(getString(resLabel),
				getResources().getDrawable(resIcon)).setContent(content);
	}
}