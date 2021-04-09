package com.zistone.factorytest0718;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.view.MotionEvent;
import android.view.WindowManager;
import android.widget.LinearLayout;

import com.zistone.mylibrary.BaseActivity;
import com.zistone.mylibrary.util.MyProgressDialogUtil;

import java.io.InputStream;

/**
 * 屏幕测试
 *
 * @author LiWei
 * @date 2020/7/18 9:33
 * @email 652276536@qq.com
 */
public class ScreenActivity extends BaseActivity {

    private static final String TAG = "ScreenActivity";

    private Handler _handler = new Handler();
    private int _imgSeq = 0;
    private WindowManager.LayoutParams _layoutParams;
    private boolean _isLocked = false;
    private PowerManager.WakeLock _WakeLock;
    private PowerManager _powerManager;
    private LinearLayout _linearLayout;
    private int[] _testImg = {R.drawable.screen_black, R.drawable.screen_white, R.drawable.screen_red,
                              R.drawable.screen_green, R.drawable.screen_blue, R.drawable.screen_stripe,
                              R.drawable.screen_black_white_lump, R.drawable.screen_color_lump};
    private float[] _brightness = new float[3];

    private Runnable _runnable = new Runnable() {
        public void run() {
            try {
                BitmapFactory.Options option = new BitmapFactory.Options();
                option.inPreferredConfig = Config.ARGB_8888;
                option.inPurgeable = true;
                option.inInputShareable = true;
                InputStream mInputSream = getResources().openRawResource(_testImg[_imgSeq]);
                Bitmap bitmap = BitmapFactory.decodeStream(mInputSream, null, option);
                BitmapDrawable bitmapDrawable = new BitmapDrawable(bitmap);
                _linearLayout.setBackgroundDrawable(bitmapDrawable);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    private void WakeLock() {
        if (!_isLocked) {
            _isLocked = true;
            _WakeLock.acquire();
        }
    }

    private void WakeUnlock() {
        if (_isLocked) {
            _WakeLock.release();
            _isLocked = false;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        if ((action == MotionEvent.ACTION_UP) && _imgSeq < _testImg.length) {
            _imgSeq++;
            _handler.postDelayed(_runnable, 0);
            if (_imgSeq >= _testImg.length) {
                _imgSeq = 0;
                MyProgressDialogUtil.ShowConfirm(this, "通过", "失败", "提示", "点击屏幕切换不同颜色，并观察屏幕是否有坏点", false, new MyProgressDialogUtil.ConfirmListener() {
                    @Override
                    public void OnConfirm() {
                        Pass();
                    }

                    @Override
                    public void OnCancel() {
                        Fail();
                    }
                });
            }
        }
        return true;
    }

    @Override
    protected void onResume() {
        WakeLock();
        super.onResume();
    }

    @Override
    protected void onPause() {
        WakeUnlock();
        super.onPause();
        _handler.removeCallbacks(_runnable);
    }

    @SuppressLint("InvalidWakeLockTag")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //全屏
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_screen);
        _layoutParams = getWindow().getAttributes();
        _layoutParams.screenBrightness = 1;
        getWindow().setAttributes(_layoutParams);
        _brightness[0] = 0;
        _brightness[1] = 0.50f;
        _brightness[2] = 1f;
        _linearLayout = findViewById(R.id.ll_screen);
        _powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        _WakeLock = _powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "BackLight");
        MyProgressDialogUtil.ShowWarning(this, "知道了", "提示", "点击屏幕切换不同颜色，并观察屏幕是否有坏点", true, null);
    }

}
