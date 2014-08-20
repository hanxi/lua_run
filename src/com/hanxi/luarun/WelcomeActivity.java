package com.hanxi.luarun;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;
import android.view.WindowManager;

public class WelcomeActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /**全屏设置，隐藏窗口所有装饰**/
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
 //               WindowManager.LayoutParams.FLAG_FULLSCREEN);
        /**标题是属于View的，所以窗口所有的修饰部分被隐藏后标题依然有效,需要去掉标题**/
  //      requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.welcome);
        Handler handler = new Handler();
        //使用pastDelayed方法延时
        handler.postDelayed(new Runnable() {
          @Override
          public void run() {
            Intent intent = new Intent(WelcomeActivity.this, MainTabActivity.class);
            startActivityForResult(intent, 11);

            //添加界面切换效果
            int version = Integer.valueOf(android.os.Build.VERSION.SDK_INT);
            //只有android2.0（版本号为5）以上的版本支持
            if(version  >= 5) {
              //自定义动画效果
               //overridePendingTransition(R.layout.zoom_enter, R.layout.zoom_exit);
               //系统动画效果
//                     overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
               overridePendingTransition(android.R.anim.slide_in_left,android.R.anim.slide_out_right);
            }
            //结束欢迎界面
            WelcomeActivity.this.finish();
          }
        }, 500);
      }
}
