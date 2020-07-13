package com.zistone.testtool.util;

import android.content.Context;
import android.content.SharedPreferences;

public class MySharedPreference {
    public static SharedPreferences Share(Context context) {
        return context.getSharedPreferences("TestTool", Context.MODE_PRIVATE);
    }

    public static String GetPhone(Context context) {
        return Share(context).getString("userPhone", null);
    }

    public static boolean SetPhone(Context context, String image) {
        SharedPreferences.Editor editor = Share(context).edit();
        editor.putString("userPhone", image);
        return editor.commit();
    }

}
