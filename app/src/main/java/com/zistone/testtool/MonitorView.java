package com.zistone.testtool;

import android.app.ActivityManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;

import java.util.List;

public class MonitorView extends View {
    private static final String TAG = "MonitorView";
    private Context _context;

    public MonitorView(Context context) {
        super(context);
        _context = context;
    }

    /**
     * 判断当前应用是否在前台
     *
     * @param context
     * @return
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    private boolean isAppForeground(Context context) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Service.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> runningAppProcessInfoList = activityManager.getRunningAppProcesses();
        if (runningAppProcessInfoList == null) {
            Log.i(TAG, "runningAppProcessInfoList is null!");
            return false;
        }
        for (ActivityManager.RunningAppProcessInfo processInfo : runningAppProcessInfoList) {
            if (processInfo.processName.equals(context.getPackageName()) && (processInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND)) {
                return true;
            }
        }
        return false;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_F1: {
                Log.i(TAG, "按下了：KeyEvent.KEYCODE_F1");
                Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:13477855544"));
                _context.startActivity(intent);
            }
            break;
            case KeyEvent.KEYCODE_F2: {
                Log.i(TAG, "按下了：KeyEvent.KEYCODE_F2");
                Intent intent = new Intent(_context, ScanTestActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("key", "F2");
                intent.putExtras(bundle);
                _context.startActivity(intent);
            }
            break;
        }
        return super.onKeyDown(keyCode, event);
    }

}