package com.zistone.mylibrary.util;

import android.content.Context;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;

/**
 * 默认以扬声器播放声音
 *
 * @author LiWei
 * @date 2020/7/18 9:33
 * @email 652276536@qq.com
 */
public final class MySoundPlayUtil {

    /**
     * （禁止外部实例化）
     */
    private MySoundPlayUtil() {
    }

    /**
     * 以响铃的方式播放系统通知的声音，无论是否插入耳机扬声器都会播放
     */
    public static void SystemSoundPlay(Context context) {
        Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        if (null == uri)
            return;
        Ringtone ringtone = RingtoneManager.getRingtone(context, uri);
        ringtone.play();
    }

}

