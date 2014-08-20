package com.hanxi.luarun;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.view.KeyEvent;
import android.widget.Toast;

public class SetActivity extends PreferenceActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SysApplication.getInstance().addActivity(this);

        addPreferencesFromResource(R.xml.preferences);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            pressAgainExit();
            return true;
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
