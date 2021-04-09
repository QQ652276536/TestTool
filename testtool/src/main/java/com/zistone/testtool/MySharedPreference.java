package com.zistone.testtool;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * 用于本地存储一键拨号的号码
 *
 * @author LiWei
 * @date 2021/4/8 18:37
 * @email 652276536@qq.com
 */
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