package com.zistone.testtool.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BootCompleteReceiver extends BroadcastReceiver {

    private static final String TAG = "BootCompleteReceiver";
    private static final String ACTION_BOOT = "android.intent.action.BOOT_COMPLETED";
    private Context _context;

    @Override
    public void onReceive(Context context, Intent intent) {
        _context = context;
        if (ACTION_BOOT.equals(intent.getAction())) {
            Log.i(TAG, "已收到开机广播，程序启动......");
        }
    }

}
