package com.zistone.testtool;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.zistone.testtool.faceidcompare.activity.BaseActivity;
import com.zistone.testtool.faceidcompare.activity.FaceAuthActicity;
import com.zistone.testtool.faceidcompare.listener.SdkInitListener;
import com.zistone.testtool.faceidcompare.manager.FaceSDKManager;
import com.zistone.testtool.faceidcompare.utils.ConfigUtils;
import com.zistone.testtool.faceidcompare.utils.ToastUtils;

/**
 * 主功能页面，包含人脸检索入口，认证比对，功能设置，授权激活
 */
public class FaceMenuActivity extends BaseActivity implements View.OnClickListener {

    private Context mContext;

    private Button btnIdentity, _btnReturn;
    private TextView _txt;
    private boolean isInitConfig;
    private boolean isConfigExit;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_facemenu);
        _txt = findViewById(R.id.txt_identity);
        _btnReturn = findViewById(R.id.btn_return_identity);
        _btnReturn.setOnClickListener(this);
        // todo shangrong 增加配置信息初始化操作
        isConfigExit = ConfigUtils.isConfigExit();
        isInitConfig = ConfigUtils.initConfig();
        if (isInitConfig && isConfigExit) {
            Toast.makeText(FaceMenuActivity.this, "初始配置加载成功", Toast.LENGTH_SHORT).show();
            _txt.setText("初始配置加载成功");
        } else {
            Toast.makeText(FaceMenuActivity.this, "初始配置失败,将重置文件内容为默认配置", Toast.LENGTH_SHORT).show();
            _txt.setText("初始配置失败,将重置文件内容为默认配置");
            ConfigUtils.modityJson();
        }
        mContext = this;
        initView();
        initLicense();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    /**
     * UI 相关VIEW 初始化
     */
    private void initView() {
        btnIdentity = findViewById(R.id.btn_identity);
        btnIdentity.setOnClickListener(this);
    }

    /**
     * 启动应用程序，如果之前初始过，自动初始化鉴权和模型（可以添加到Application 中）
     */
    private void initLicense() {
        if (FaceSDKManager.initStatus != FaceSDKManager.SDK_MODEL_LOAD_SUCCESS) {
            FaceSDKManager.getInstance().init(mContext, new SdkInitListener() {
                @Override
                public void initStart() {
                }

                @Override
                public void initLicenseSuccess() {
                }

                @Override
                public void initLicenseFail(int errorCode, String msg) {
                    // 如果授权失败，跳转授权页面
                    ToastUtils.toast(mContext, errorCode + msg);
                    startActivity(new Intent(mContext, FaceAuthActicity.class));
                }

                @Override
                public void initModelSuccess() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            _txt.setText("模型加载成功，欢迎使用");
                        }
                    });
                }

                @Override
                public void initModelFail(int errorCode, String msg) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            _txt.setText("模型加载失败");
                        }
                    });
                }
            });
        }
    }

    /**
     * 点击事件跳转路径
     *
     * @param view
     */
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_return_identity:
                FaceMenuActivity.this.finish();
                break;
            case R.id.btn_identity:
                // 【1：N 人脸搜索】 和 【1：1 人证比对】跳转判断授权和模型初始化状态
                if (FaceSDKManager.getInstance().initStatus == FaceSDKManager.SDK_UNACTIVATION) {
                    Toast.makeText(FaceMenuActivity.this, "SDK还未激活初始化，请先激活初始化", Toast.LENGTH_LONG).show();
                    _txt.setText("SDK还未激活初始化，请先激活初始化");
                    return;
                } else if (FaceSDKManager.getInstance().initStatus == FaceSDKManager.SDK_INIT_FAIL) {
                    Toast.makeText(FaceMenuActivity.this, "SDK初始化失败，请重新激活初始化", Toast.LENGTH_LONG).show();
                    _txt.setText("SDK初始化失败，请重新激活初始化");
                    return;
                } else if (FaceSDKManager.getInstance().initStatus == FaceSDKManager.SDK_INIT_SUCCESS) {
                    Toast.makeText(FaceMenuActivity.this, "SDK正在加载模型，请稍后再试", Toast.LENGTH_LONG).show();
                    _txt.setText("SDK正在加载模型，请稍后再试");
                    return;
                } else if (FaceSDKManager.getInstance().initStatus == FaceSDKManager.SDK_MODEL_LOAD_SUCCESS) {
                    switch (view.getId()) {
                        case R.id.btn_identity:
                            // 【1：1 人证比对】页面跳转
                            startActivity(new Intent(FaceMenuActivity.this, FaceIdCompareActivity.class));
                            _txt.setText("");
                            break;
                    }
                }
                break;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            this.finish();
        }
        return false;
    }

    public void showHint() {
        String message = "以下个别设置项，因需要重新初始化模型。请您再修改所需配置后，重启APP查看效果:" + "\r\n" + "\r\n" + "\r\n" + "镜头及活体检测模式" + "\r\n" + "最小人脸个数" + "\r\n" + "最小人脸大小" + "\r\n" + "模糊" + "\r\n" + "光照" + "\r\n" + "遮挡" + "\r\n" + "姿态角" + "\r\n" + "属性" + "\r\n" + "眼睛闭合" + "\r\n" + "嘴巴闭合" + "\r\n";
        TextView title = new TextView(this);
        title.setText("温馨提示");
        title.setTextColor(Color.BLACK);
        title.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
        title.setGravity(Gravity.CENTER);
        title.setTextSize(30);
        AlertDialog alertDialog1 = new AlertDialog.Builder(this).setCustomTitle(title).setMessage(message).setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        }).create();
        alertDialog1.show();
    }

}
