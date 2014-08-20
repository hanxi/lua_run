package com.hanxi.luarun;

import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TabHost;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.graphics.Rect;
import android.view.View.OnClickListener;
import android.view.View;
import android.widget.Toast;

import com.wandoujia.ads.sdk.AdListener;
import com.wandoujia.ads.sdk.Ads;
import com.wandoujia.ads.sdk.loader.Fetcher;
import com.wandoujia.ads.sdk.widget.AdBanner;

@SuppressWarnings("deprecation")
public class MainTabActivity extends TabActivity implements
        OnCheckedChangeListener {

    private TabHost mTabHost;
    private Intent mAIntent;
    private Intent mBIntent;
    private Intent mCIntent;

    private static final String ADS_APP_ID = "100010233";
    private static final String ADS_SECRET_KEY = "6f1fa5e237fb4bf0212d398816b581bc";
    private static final String TAG_LIST = "a5d122a5a0d4e68822eb74ba98be5d90";

    private void drawUpdateIndicator(int color, boolean drawLeftOrRight) {
        ShapeDrawable smallerCircle = new ShapeDrawable(new OvalShape());
        smallerCircle.setIntrinsicHeight(60);
        smallerCircle.setIntrinsicWidth(60);
        smallerCircle.setBounds(new Rect(0, 0, 60, 60));
        smallerCircle.getPaint().setColor(color);
        smallerCircle.setPadding(50, 50, 50, 100);

        Drawable drawableleft = null;
        Drawable drawableRight = null;
        if (drawLeftOrRight) {
            drawableleft = smallerCircle;
        } else {
            drawableRight = smallerCircle;
        }
        ((RadioButton) findViewById(R.id.radio_button3))
                .setCompoundDrawables(drawableleft, null, drawableRight, null);
    }


    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.main);
        SysApplication.getInstance().addActivity(this);

        this.mAIntent = new Intent(this, HelpActivity.class);
        this.mBIntent = new Intent(this, MainActivity.class);
        this.mCIntent = new Intent(this, DocActivity.class);

        ((RadioButton) findViewById(R.id.radio_button0))
                .setOnCheckedChangeListener(this);
        ((RadioButton) findViewById(R.id.radio_button1))
                .setOnCheckedChangeListener(this);
        ((RadioButton) findViewById(R.id.radio_button2))
                .setOnCheckedChangeListener(this);
        ((RadioButton) findViewById(R.id.radio_button3))
                .setOnCheckedChangeListener(this);

        setupIntent();
        this.mTabHost.setCurrentTabByTag("B_TAB");

         // Init AdsSdk.
        try {
            Ads.init(this, ADS_APP_ID, ADS_SECRET_KEY);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Ads.preLoad(this, Fetcher.AdFormat.appwall, "APP", TAG_LIST, new AdListener() {
            @Override
            public void onAdLoaded() {
                if (Ads.getUpdateAdCount("APP") > 0) {
                    drawUpdateIndicator(Color.RED, true);
                }
            }
        });

        Ads.preLoad(this, Fetcher.AdFormat.appwall, "GAME", TAG_LIST, new AdListener() {
            @Override
            public void onAdLoaded() {
                if (Ads.getUpdateAdCount("GAME") > 0) {
                    drawUpdateIndicator(Color.GREEN, false);
                }
            }
        });
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (isChecked) {
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
                buttonView.setChecked(false);
                Ads.showAppWall(this, TAG_LIST);
                Toast.makeText(this,
                        this.getString(R.string.thankyou),
                        Toast.LENGTH_LONG).show();
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

        localTabHost.addTab(buildTabSpec("C_TAB", R.string.main_doc,
                R.drawable.icon_3_n, this.mCIntent));

    }

    private TabHost.TabSpec buildTabSpec(String tag, int resLabel, int resIcon,
            final Intent content) {
        return this.mTabHost
                .newTabSpec(tag)
                .setIndicator(getString(resLabel),
                        getResources().getDrawable(resIcon))
                .setContent(content);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
        /*
         * case R.id.action_settings: Intent intent = new Intent();
         * intent.setClass(MainTabActivity.this,SetActivity.class);
         * startActivity(intent); return true;
         */
        case R.id.action_exit:
            SysApplication.getInstance().exit();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
