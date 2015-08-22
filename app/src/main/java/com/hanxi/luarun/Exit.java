package com.hanxi.luarun;

import android.os.Handler;
import android.os.HandlerThread;

public class Exit {
    private static boolean isExit = false;

    private static Runnable task = new Runnable() {
        @Override
        public void run() {
            isExit = false;
        }
    };

    public static void doExitInOneSecond() {
        isExit = true;
        HandlerThread thread = new HandlerThread("doTask");
            thread.start();
            new Handler(thread.getLooper()).postDelayed(task, Integer.valueOf(R.string.exit_time));
    }

    public static boolean isExit() {
        return isExit;
    }

    public static void setExit(boolean exit) {
        isExit = exit;
    }
}
