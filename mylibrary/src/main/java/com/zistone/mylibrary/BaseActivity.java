package com.zistone.mylibrary;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

/**
 * 所有测试Activity的基类
 *
 * @author LiWei
 * @date 2020/7/18 9:33
 * @email 652276536@qq.com
 */
public class BaseActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "BaseActivity";

    public static final String ROOT_PATH = "/sdcard/FactoryTest0718/";
    public static final String ARG_PARAM1 = "param1";
    public static final String ARG_PARAM2 = "param2";
    public static final String PASS = "PASS";
    public static final String FAIL = "FAIL";
    public static final int SPRING_GREEN = Color.parseColor("#3CB371");

    public String _deviceType = "", _systemVersion = "";
    public Button _btnPass, _btnFail;
    public int _screenHeight, _screenWidth;
    //基类布局
    public LinearLayout _baseLinearLayout;
    public boolean _isInsertHeadset = false;
    public HeadsetReceiver _headsetReceiver;

    /**
     * 用于检测耳机是否插入及监听耳机按键事件
     */
    class HeadsetReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            int state = intent.getIntExtra("state", 0);
            Log.i(TAG, "耳机插拨状态：" + state);
            //没有插入耳机
            if (state == 0) {
                _isInsertHeadset = false;
            }
            //已插入耳机
            else if (state == 1) {
                _isInsertHeadset = true;
            }
        }
    }

    /**
     * 显示键盘
     *
     * @param view
     */
    public void ShowKeyboard(View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (null != inputMethodManager) {
            view.requestFocus();
            inputMethodManager.showSoftInput(view, 0);
        }
    }

    /**
     * 隐藏键盘
     *
     * @param view
     */
    public void HideKeyboard(View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (null != inputMethodManager) {
            inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    /**
     * 到TextView顶部
     *
     * @param txt
     */
    public void TxtToTop(TextView txt) {
        txt.scrollTo(0, 0);
    }

    /**
     * 到TextView底部
     *
     * @param txt
     */
    public void TxtToBottom(TextView txt) {
        int offset = txt.getLineCount() * txt.getLineHeight();
        if (offset > txt.getHeight()) {
            txt.scrollTo(0, offset - txt.getHeight());
        }
    }

    /**
     * 清除TextView的内容
     *
     * @param txt
     */
    public void TxtClear(TextView txt) {
        txt.setText("");
        txt.scrollTo(0, 0);
    }

    /**
     * 测试通过
     */
    public void Pass() {
        Intent intent = new Intent();
        intent.putExtra(ARG_PARAM1, PASS);
        setResult(RESULT_OK, intent);
        finish();
    }

    /**
     * 测试失败
     */
    public void Fail() {
        Intent intent = new Intent();
        intent.putExtra(ARG_PARAM1, FAIL);
        setResult(RESULT_OK, intent);
        finish();
    }

    /**
     * 权限检查
     *
     * @param neededPermissions 需要的权限
     * @return 全部被允许
     */
    public boolean CheckPermissions(String[] neededPermissions) {
        if (neededPermissions == null || neededPermissions.length == 0) {
            return true;
        }
        boolean allGranted = true;
        for (String neededPermission : neededPermissions) {
            allGranted &= ContextCompat.checkSelfPermission(this, neededPermission) == PackageManager.PERMISSION_GRANTED;
        }
        return allGranted;
    }

    /**
     * 将继承自BaseActivity的layout加进来
     *
     * @param layoutId
     */
    public void SetBaseContentView(int layoutId) {
        _baseLinearLayout = findViewById(R.id.ll_base);
        //根据屏幕尺寸设置控件大小，不然基类的控件可能看不到
        ViewGroup.LayoutParams layoutParams = _baseLinearLayout.getLayoutParams();
        layoutParams.height = _screenHeight - 400;
        //将继承自该Activity的布局加进来
        LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = layoutInflater.inflate(layoutId, null);
        _baseLinearLayout.addView(view);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(_headsetReceiver);
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            Intent intent = new Intent();
            intent.putExtra(ARG_PARAM1, FAIL);
            setResult(RESULT_OK, intent);
            finish();
        }
        return false;
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btn_pass_base) {
            Pass();
        } else if (id == R.id.btn_fail_base) {
            Fail();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //去掉系统的TitleBar
        //        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_base);
        //Activity启动后就锁定为启动时的方向
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);
        _btnPass = findViewById(R.id.btn_pass_base);
        _btnPass.setOnClickListener(this::onClick);
        _btnFail = findViewById(R.id.btn_fail_base);
        _btnFail.setOnClickListener(this::onClick);
        Point point = new Point();
        getWindowManager().getDefaultDisplay().getRealSize(point);
        _screenHeight = point.y;
        _screenWidth = point.x;
        //检测耳机是否插入
        _headsetReceiver = new HeadsetReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_HEADSET_PLUG);
        registerReceiver(_headsetReceiver, intentFilter);
    }
}
